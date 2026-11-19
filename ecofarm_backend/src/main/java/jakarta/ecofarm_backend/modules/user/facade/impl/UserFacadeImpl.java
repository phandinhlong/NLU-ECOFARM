package jakarta.ecofarm_backend.modules.user.facade.impl;

import jakarta.ecofarm_backend.common.dto.request.BaseCriteria;
import jakarta.ecofarm_backend.common.dto.request.UserCriteria;
import jakarta.ecofarm_backend.common.dto.response.BaseResponse;
import jakarta.ecofarm_backend.common.dto.response.CursorPageResponse;
import jakarta.ecofarm_backend.common.dto.response.WorkerSummaryResponse;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.User;
import jakarta.ecofarm_backend.infrastructure.service.UserService;
import jakarta.ecofarm_backend.modules.user.facade.UserFacade;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class UserFacadeImpl implements UserFacade {

    @Inject
    private UserService userService;

    @Override
    public BaseResponse<CursorPageResponse<WorkerSummaryResponse>> findUsers(UserCriteria criteria) {
        List<User> users = userService.findPage(criteria);
        boolean hasMore = users.size() > BaseCriteria.LENGTH;
        List<User> page = hasMore ? users.subList(0, BaseCriteria.LENGTH) : users;

        Long nextIndex = page.isEmpty() ? null : page.get(page.size() - 1).getId();
        List<WorkerSummaryResponse> content = page.stream().map(this::toSummary).toList();
        return BaseResponse.build(new CursorPageResponse<>(content, nextIndex, hasMore));
    }

    private WorkerSummaryResponse toSummary(User user) {
        return WorkerSummaryResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .roles(user.getRoles() == null ? List.of()
                        : user.getRoles().stream().map(role -> role.getRoleName().name()).toList())
                .build();
    }
}
