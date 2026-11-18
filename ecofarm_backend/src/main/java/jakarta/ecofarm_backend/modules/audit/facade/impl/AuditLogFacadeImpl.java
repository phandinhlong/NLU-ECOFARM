package jakarta.ecofarm_backend.modules.audit.facade.impl;

import jakarta.ecofarm_backend.common.dto.request.AuditLogCriteria;
import jakarta.ecofarm_backend.common.dto.response.BaseResponse;
import jakarta.ecofarm_backend.common.dto.response.CursorPageResponse;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.AuditLog;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.User;
import jakarta.ecofarm_backend.infrastructure.service.AuditLogService;
import jakarta.ecofarm_backend.common.dto.response.AuditLogResponse;
import jakarta.ecofarm_backend.modules.audit.facade.AuditLogFacade;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@ApplicationScoped
public class AuditLogFacadeImpl implements AuditLogFacade {

    @Inject
    private AuditLogService auditLogService;

    @Override
    public BaseResponse<CursorPageResponse<AuditLogResponse>> findAuditLogs(AuditLogCriteria criteria) {
        List<AuditLog> logs = auditLogService.findPage(criteria);
        boolean hasMore = logs.size() > AuditLogCriteria.LENGTH;
        List<AuditLog> page = hasMore ? logs.subList(0, AuditLogCriteria.LENGTH) : logs;
        List<AuditLogResponse> content = page.stream().map(this::toResponse).toList();
        Long lastIndex = page.isEmpty() ? null : page.get(page.size() - 1).getId();
        return BaseResponse.build(new CursorPageResponse<>(content, lastIndex, hasMore));
    }

    @Override
    public BaseResponse<Long> countAuditLogs(AuditLogCriteria criteria) {
        return BaseResponse.build(auditLogService.count(criteria));
    }

    private AuditLogResponse toResponse(AuditLog log) {
        User actor = log.getActor();
        return AuditLogResponse.builder()
                .id(log.getId())
                .actorId(actor == null ? null : actor.getId())
                .actorName(actor == null ? null : actor.getFullName())
                .actorEmail(actor == null ? null : actor.getEmail())
                .action(log.getAction())
                .entity(log.getEntity())
                .entityId(log.getEntityId())
                .detail(log.getDetail())
                .ipAddr(log.getIpAddr())
                .createdAt(toLocalDateTime(log.getCreatedAt()))
                .build();
    }

    private static LocalDateTime toLocalDateTime(Long epochMilli) {
        if (epochMilli == null) return null;
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMilli), ZoneId.systemDefault());
    }
}
