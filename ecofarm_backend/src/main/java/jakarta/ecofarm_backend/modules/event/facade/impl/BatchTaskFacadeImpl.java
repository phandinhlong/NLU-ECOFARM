package jakarta.ecofarm_backend.modules.event.facade.impl;

import jakarta.ecofarm_backend.common.dto.request.PageCriteria;
import jakarta.ecofarm_backend.common.dto.response.BaseResponse;
import jakarta.ecofarm_backend.common.dto.response.CursorPageResponse;
import jakarta.ecofarm_backend.common.enums.BatchStatus;
import jakarta.ecofarm_backend.common.enums.ErrorCode;
import jakarta.ecofarm_backend.common.enums.EventReviewStatus;
import jakarta.ecofarm_backend.common.enums.EventType;
import jakarta.ecofarm_backend.common.enums.RoleEnum;
import jakarta.ecofarm_backend.common.enums.TaskStatus;
import jakarta.ecofarm_backend.common.exception.ConflictException;
import jakarta.ecofarm_backend.common.exception.EntityNotFoundException;
import jakarta.ecofarm_backend.common.exception.PermissionDeniedException;
import jakarta.ecofarm_backend.common.exception.UnprocessableEntityException;
import jakarta.ecofarm_backend.common.util.CurrentUserUtil;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Batch;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.BatchTask;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.CultivationEvent;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.User;
import jakarta.ecofarm_backend.infrastructure.security.UserSecurityDetailsPrincipal;
import jakarta.ecofarm_backend.infrastructure.service.BatchService;
import jakarta.ecofarm_backend.infrastructure.service.BatchTaskService;
import jakarta.ecofarm_backend.infrastructure.service.CultivationEventService;
import jakarta.ecofarm_backend.infrastructure.service.UserService;
import jakarta.ecofarm_backend.common.dto.response.BatchTaskResponse;
import jakarta.ecofarm_backend.common.dto.request.CompleteTaskRequest;
import jakarta.ecofarm_backend.common.dto.request.CreateTaskRequest;
import jakarta.ecofarm_backend.common.dto.request.EventRecordCommand;
import jakarta.ecofarm_backend.modules.event.facade.BatchTaskFacade;
import jakarta.ecofarm_backend.modules.event.facade.EventFacade;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@ApplicationScoped
public class BatchTaskFacadeImpl implements BatchTaskFacade {

    @Inject
    private BatchTaskService batchTaskService;

    @Inject
    private BatchService batchService;

    @Inject
    private UserService userService;

    @Inject
    private CultivationEventService cultivationEventService;

    @Inject
    private EventFacade eventFacade;

    @Inject
    private CurrentUserUtil currentUser;

    @Override
    @Transactional
    public BaseResponse<BatchTaskResponse> createTask(Long batchId, CreateTaskRequest request) {
        UserSecurityDetailsPrincipal principal = requirePrincipal();
        User actor = requireUser(principal.getId());
        requireEngineer(principal);

        Batch batch = batchService.findById(batchId);
        if (batch == null) {
            throw new EntityNotFoundException(ErrorCode.BATCH_NOT_FOUND);
        }
        if (isLocked(batch)) {
            throw new ConflictException(ErrorCode.BATCH_LOCKED);
        }

        User worker = userService.findById(request.getWorkerId())
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.WORKER_NOT_ASSIGNED));
        if (!worker.isActive() || worker.getRoles().stream().noneMatch(r -> r.getRoleName() == RoleEnum.FARMER)) {
            throw new ConflictException(ErrorCode.WORKER_INVALID_ROLE);
        }
        if (!Boolean.TRUE.equals(batchService.isAssigned(batchId, worker.getId()))) {
            throw new ConflictException(ErrorCode.WORKER_NOT_ASSIGNED);
        }

        long now = System.currentTimeMillis();
        BatchTask task = BatchTask.builder()
                .batch(batch)
                .worker(worker)
                .taskType(request.getTaskType())
                .note(request.getNote())
                .assignedBy(actor)
                .assignedAt(now)
                .status(TaskStatus.ASSIGNED)
                .build();
        batchTaskService.save(task);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("taskId", task.getId());
        data.put("taskType", task.getTaskType().name());
        data.put("workerId", worker.getId());
        data.put("assignedAt", now);
        data.put("note", request.getNote());

        CultivationEvent event = eventFacade.record(EventRecordCommand.builder()
                .batchId(batchId)
                .eventType(EventType.TASK_ASSIGNED)
                .clientEventId(UUID.randomUUID().toString())
                .occurredAt(now)
                .data(data)
                .actorId(principal.getId())
                .build());
        event.setReviewStatus(EventReviewStatus.APPROVED);
        event.setReviewedBy(actor);
        event.setReviewedAt(now);
        cultivationEventService.save(event);

        task.setEvent(event);
        batchTaskService.save(task);

        return BaseResponse.build(toResponse(task));
    }

    @Override
    public BaseResponse<CursorPageResponse<BatchTaskResponse>> findTasks(Long batchId, Long lastIndex, TaskStatus status) {
        UserSecurityDetailsPrincipal principal = requirePrincipal();
        Batch batch = batchService.findById(batchId);
        if (batch == null) {
            throw new EntityNotFoundException(ErrorCode.BATCH_NOT_FOUND);
        }
        if (principal.hasRole(RoleEnum.FARMER.name())
                && !Boolean.TRUE.equals(batchService.isAssigned(batchId, principal.getId()))) {
            throw new PermissionDeniedException(ErrorCode.BATCH_NOT_ASSIGNED);
        }

        Long cursor = lastIndex == null || lastIndex <= 0 ? null : lastIndex;
        List<BatchTask> tasks = batchTaskService.findPage(batchId, cursor, status, PageCriteria.LENGTH + 1);
        boolean hasMore = tasks.size() > PageCriteria.LENGTH;
        List<BatchTask> page = hasMore ? tasks.subList(0, PageCriteria.LENGTH) : tasks;
        Long nextIndex = page.isEmpty() ? null : page.get(page.size() - 1).getId();
        List<BatchTaskResponse> content = page.stream().map(this::toResponse).toList();
        return BaseResponse.build(new CursorPageResponse<>(content, nextIndex, hasMore));
    }

    @Override
    @Transactional
    public BaseResponse<BatchTaskResponse> completeTask(Long taskId, CompleteTaskRequest request) {
        UserSecurityDetailsPrincipal principal = requirePrincipal();
        BatchTask task = batchTaskService.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.TASK_NOT_FOUND));
        if (task.getWorker() == null || !task.getWorker().getId().equals(principal.getId())) {
            throw new PermissionDeniedException(ErrorCode.PERMISSION_DENIED);
        }
        if (task.getStatus() != TaskStatus.ASSIGNED) {
            throw new ConflictException(ErrorCode.TASK_ALREADY_COMPLETED);
        }

        long occurredAt;
        try {
            occurredAt = OffsetDateTime.parse(request.getOccurredAt()).toInstant().toEpochMilli();
        } catch (DateTimeParseException e) {
            throw new UnprocessableEntityException(ErrorCode.EVENT_SCHEMA_INVALID, List.of("occurredAt không hợp lệ"));
        }

        CultivationEvent event = eventFacade.record(EventRecordCommand.builder()
                .batchId(task.getBatch().getId())
                .eventType(task.getTaskType())
                .clientEventId(request.getClientEventId())
                .occurredAt(occurredAt)
                .data(request.getData())
                .actorId(principal.getId())
                .build());

        long now = System.currentTimeMillis();
        task.setCompletedAt(now);
        task.setStatus(TaskStatus.COMPLETED);
        task.setCompletionEvent(event);
        batchTaskService.save(task);

        return BaseResponse.build(toResponse(task));
    }

    @Override
    @Transactional
    public BaseResponse<Boolean> cancelTask(Long taskId, String reason) {
        UserSecurityDetailsPrincipal principal = requirePrincipal();
        requireEngineer(principal);
        BatchTask task = batchTaskService.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.TASK_NOT_FOUND));
        if (task.getStatus() != TaskStatus.ASSIGNED) {
            throw new ConflictException(ErrorCode.TASK_ALREADY_COMPLETED);
        }
        task.setStatus(TaskStatus.CANCELLED);
        batchTaskService.save(task);
        return BaseResponse.build(Boolean.TRUE);
    }

    private BatchTaskResponse toResponse(BatchTask task) {
        return BatchTaskResponse.builder()
                .id(task.getId())
                .batchId(task.getBatch() == null ? null : task.getBatch().getId())
                .workerId(task.getWorker() == null ? null : task.getWorker().getId())
                .workerName(task.getWorker() == null ? null : task.getWorker().getFullName())
                .taskType(task.getTaskType() == null ? null : task.getTaskType().name())
                .note(task.getNote())
                .assignedById(task.getAssignedBy() == null ? null : task.getAssignedBy().getId())
                .assignedAt(toLocalDateTime(task.getAssignedAt()))
                .completedAt(toLocalDateTime(task.getCompletedAt()))
                .status(task.getStatus() == null ? null : task.getStatus().name())
                .eventId(task.getEvent() == null ? null : task.getEvent().getId())
                .completionEventId(task.getCompletionEvent() == null ? null : task.getCompletionEvent().getId())
                .build();
    }

    private UserSecurityDetailsPrincipal requirePrincipal() {
        UserSecurityDetailsPrincipal principal = currentUser.get();
        if (principal == null || principal.getId() == null) {
            throw new PermissionDeniedException(ErrorCode.PERMISSION_DENIED);
        }
        return principal;
    }

    private User requireUser(Long id) {
        return userService.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND));
    }

    private void requireEngineer(UserSecurityDetailsPrincipal principal) {
        if (!principal.hasRole(RoleEnum.ENGINEER.name())) {
            throw new PermissionDeniedException(ErrorCode.PERMISSION_DENIED);
        }
    }

    private boolean isLocked(Batch batch) {
        BatchStatus status = batch.getStatus() == null ? BatchStatus.DRAFT : BatchStatus.valueOf(batch.getStatus());
        return status.isLocked();
    }

    private static LocalDateTime toLocalDateTime(Long epochMillis) {
        return epochMillis == null ? null
                : LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), ZoneId.systemDefault());
    }
}
