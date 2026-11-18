package jakarta.ecofarm_backend.modules.catalog.facade.impl;

import jakarta.ecofarm_backend.common.dto.request.VarietyCriteria;
import jakarta.ecofarm_backend.common.dto.response.BaseResponse;
import jakarta.ecofarm_backend.common.dto.response.CursorPageResponse;
import jakarta.ecofarm_backend.common.enums.ErrorCode;
import jakarta.ecofarm_backend.common.exception.ConflictException;
import jakarta.ecofarm_backend.common.exception.EntityNotFoundException;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Variety;
import jakarta.ecofarm_backend.infrastructure.service.VarietyService;
import jakarta.ecofarm_backend.modules.catalog.dto.VarietyRequest;
import jakarta.ecofarm_backend.modules.catalog.dto.VarietyResponse;
import jakarta.ecofarm_backend.modules.catalog.facade.VarietyFacade;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Map;

@ApplicationScoped
public class VarietyFacadeImpl implements VarietyFacade {

    @Inject
    private VarietyService varietyService;

    @Override
    public BaseResponse<CursorPageResponse<VarietyResponse>> findVarieties(VarietyCriteria criteria) {
        List<Variety> varieties = varietyService.findPage(criteria);
        boolean hasMore = varieties.size() > VarietyCriteria.LENGTH;
        List<Variety> page = hasMore ? varieties.subList(0, VarietyCriteria.LENGTH) : varieties;

        List<Long> ids = page.stream().map(Variety::getId).toList();
        Map<Long, Long> batchCounts = varietyService.countBatchesByVarietyIds(ids);

        List<VarietyResponse> content = page.stream()
                .map(v -> toResponse(v, batchCounts.getOrDefault(v.getId(), 0L)))
                .toList();

        Long lastId = page.isEmpty() ? null : page.get(page.size() - 1).getId();
        return BaseResponse.build(new CursorPageResponse<>(content, lastId, hasMore));
    }

    @Override
    public BaseResponse<Long> countVarieties(VarietyCriteria criteria) {
        return BaseResponse.build(varietyService.countActive(criteria));
    }

    @Override
    public BaseResponse<VarietyResponse> findVariety(Long id) {
        Variety variety = varietyService.findById(id);
        if (variety == null) {
            throw new EntityNotFoundException(ErrorCode.VARIETY_NOT_FOUND);
        }
        return BaseResponse.build(toResponse(variety, varietyService.countBatches(id)));
    }

    @Override
    @Transactional
    public BaseResponse<VarietyResponse> createVariety(VarietyRequest request) {
        if (varietyService.existsByCode(request.getCode())) {
            throw new ConflictException(ErrorCode.CODE_EXISTS);
        }

        Variety variety = Variety.builder()
                .code(request.getCode().trim())
                .name(request.getName().trim())
                .daysPollinationToHarvest(request.getDaysPollinationToHarvest())
                .minBrix(request.getMinBrix() == null ? null : request.getMinBrix().doubleValue())
                .minIsolationDays(request.getMinIsolationDays())
                .build();
        if (request.getActive() != null) {
            variety.setActive(request.getActive());
        }

        varietyService.save(variety);

        return BaseResponse.build(toResponse(variety, 0L));
    }

    @Override
    @Transactional
    public BaseResponse<VarietyResponse> updateVariety(Long id, VarietyRequest request) {
        Variety variety = varietyService.findById(id);
        if (variety == null) {
            throw new EntityNotFoundException(ErrorCode.VARIETY_NOT_FOUND);
        }

        if (request.getVersion() != null && !request.getVersion().equals(variety.getVersion())) {
            throw new ConflictException(ErrorCode.VERSION_CONFLICT);
        }

        boolean codeChanged = !variety.getCode().equals(request.getCode());
        if (codeChanged && varietyService.countBatches(id) > 0) {
            throw new ConflictException(ErrorCode.CATALOG_CODE_LOCKED);
        }
        if (codeChanged && varietyService.existsByCodeAndIdNot(request.getCode(), id)) {
            throw new ConflictException(ErrorCode.CODE_EXISTS);
        }

        applyRequest(variety, request);
        Variety updated = varietyService.save(variety);
        return BaseResponse.build(toResponse(updated, varietyService.countBatches(id)));
    }

    @Override
    @Transactional
    public BaseResponse<Boolean> deleteVariety(Long id) {
        Variety variety = varietyService.findById(id);
        if (variety == null) {
            throw new EntityNotFoundException(ErrorCode.VARIETY_NOT_FOUND);
        }

        if (varietyService.countBatches(id) > 0) {
            throw new ConflictException(ErrorCode.CATALOG_DELETE_LOCKED);
        }

        variety.setActive(false);
        varietyService.save(variety);
        return BaseResponse.build(true);
    }

    private void applyRequest(Variety variety, VarietyRequest request) {
        variety.setCode(request.getCode().trim());
        variety.setName(request.getName().trim());
        variety.setDaysPollinationToHarvest(request.getDaysPollinationToHarvest());
        variety.setMinBrix(request.getMinBrix() == null ? null : request.getMinBrix().doubleValue());
        variety.setMinIsolationDays(request.getMinIsolationDays());
        if (request.getActive() != null) {
            variety.setActive(request.getActive());
        }
    }

    private VarietyResponse toResponse(Variety variety, Long batchCount) {
        return VarietyResponse.builder()
                .id(variety.getId())
                .code(variety.getCode())
                .name(variety.getName())
                .daysPollinationToHarvest(variety.getDaysPollinationToHarvest())
                .minBrix(variety.getMinBrix())
                .minIsolationDays(variety.getMinIsolationDays())
                .batchCount(batchCount)
                .active(variety.isActive())
                .version(variety.getVersion())
                .createdAt(variety.getCreatedAt())
                .updatedAt(variety.getUpdatedAt())
                .build();
    }
}
