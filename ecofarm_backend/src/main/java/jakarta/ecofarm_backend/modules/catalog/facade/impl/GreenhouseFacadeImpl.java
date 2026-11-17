package jakarta.ecofarm_backend.modules.catalog.facade.impl;

import jakarta.ecofarm_backend.common.dto.request.GreenhouseCriteria;
import jakarta.ecofarm_backend.common.dto.response.BaseResponse;
import jakarta.ecofarm_backend.common.dto.response.CursorPageResponse;
import jakarta.ecofarm_backend.common.enums.ErrorCode;
import jakarta.ecofarm_backend.common.exception.ConflictException;
import jakarta.ecofarm_backend.common.exception.EntityNotFoundException;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Greenhouse;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.GreenhouseRepository;
import jakarta.ecofarm_backend.modules.catalog.dto.GreenhouseRequest;
import jakarta.ecofarm_backend.modules.catalog.dto.GreenhouseResponse;
import jakarta.ecofarm_backend.modules.catalog.facade.GreenhouseFacade;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Map;

@ApplicationScoped
public class GreenhouseFacadeImpl implements GreenhouseFacade {

    @Inject
    private GreenhouseRepository greenhouseRepository;

    @Override
    public BaseResponse<CursorPageResponse<GreenhouseResponse>> findGreenhouses(GreenhouseCriteria criteria) {
        List<Greenhouse> greenhouses = greenhouseRepository.findPage(criteria);
        boolean hasMore = greenhouses.size() > GreenhouseCriteria.LENGTH;
        List<Greenhouse> page = hasMore ? greenhouses.subList(0, GreenhouseCriteria.LENGTH) : greenhouses;

        List<Long> ids = page.stream().map(Greenhouse::getId).toList();
        Map<Long, Long> batchCounts = greenhouseRepository.countBatchesByGreenhouseIds(ids);

        List<GreenhouseResponse> content = page.stream()
                .map(g -> toResponse(g, batchCounts.getOrDefault(g.getId(), 0L)))
                .toList();

        Long lastIndex = page.isEmpty() ? null : page.get(page.size() - 1).getId();
        return BaseResponse.build(new CursorPageResponse<>(content, lastIndex, hasMore));
    }

    @Override
    public BaseResponse<Long> countGreenhouses(GreenhouseCriteria criteria) {
        return BaseResponse.build(greenhouseRepository.countActive(criteria));
    }

    @Override
    public BaseResponse<GreenhouseResponse> findGreenhouse(Long id) {
        Greenhouse greenhouse = greenhouseRepository.findById(id);
        if (greenhouse == null) {
            throw new EntityNotFoundException(ErrorCode.GREENHOUSE_NOT_FOUND);
        }
        return BaseResponse.build(toResponse(greenhouse, greenhouseRepository.countBatches(id)));
    }

    @Override
    @Transactional
    public BaseResponse<GreenhouseResponse> createGreenhouse(GreenhouseRequest request) {
        if (greenhouseRepository.existsByCode(request.getCode())) {
            throw new ConflictException(ErrorCode.CODE_EXISTS);
        }

        Greenhouse greenhouse = new Greenhouse();
        applyRequest(greenhouse, request);
        greenhouseRepository.save(greenhouse);

        return BaseResponse.build(toResponse(greenhouse, 0L));
    }

    @Override
    @Transactional
    public BaseResponse<GreenhouseResponse> updateGreenhouse(Long id, GreenhouseRequest request) {
        Greenhouse greenhouse = greenhouseRepository.findById(id);
        if (greenhouse == null) {
            throw new EntityNotFoundException(ErrorCode.GREENHOUSE_NOT_FOUND);
        }

        if (request.getVersion() != null && !request.getVersion().equals(greenhouse.getVersion())) {
            throw new ConflictException(ErrorCode.VERSION_CONFLICT);
        }

        boolean codeChanged = !greenhouse.getCode().equals(request.getCode());
        if (codeChanged && greenhouseRepository.countBatches(id) > 0) {
            throw new ConflictException(ErrorCode.CATALOG_CODE_LOCKED);
        }
        if (codeChanged && greenhouseRepository.existsByCodeAndIdNot(request.getCode(), id)) {
            throw new ConflictException(ErrorCode.CODE_EXISTS);
        }

        applyRequest(greenhouse, request);
        Greenhouse updated = greenhouseRepository.save(greenhouse);
        return BaseResponse.build(toResponse(updated, greenhouseRepository.countBatches(id)));
    }

    private void applyRequest(Greenhouse greenhouse, GreenhouseRequest request) {
        greenhouse.setCode(request.getCode().trim());
        greenhouse.setName(request.getName().trim());
        greenhouse.setGpsLat(request.getGpsLat() == null ? null : request.getGpsLat().doubleValue());
        greenhouse.setGpsLng(request.getGpsLng() == null ? null : request.getGpsLng().doubleValue());
        greenhouse.setAreaM2(request.getAreaM2() == null ? null : request.getAreaM2().doubleValue());
        greenhouse.setStatus(request.getStatus() == null ? "ACTIVE" : request.getStatus());
    }

    private GreenhouseResponse toResponse(Greenhouse greenhouse, Long batchCount) {
        return GreenhouseResponse.builder()
                .id(greenhouse.getId())
                .code(greenhouse.getCode())
                .name(greenhouse.getName())
                .gpsLat(greenhouse.getGpsLat())
                .gpsLng(greenhouse.getGpsLng())
                .areaM2(greenhouse.getAreaM2())
                .status(greenhouse.getStatus())
                .batchCount(batchCount)
                .active(greenhouse.isActive())
                .version(greenhouse.getVersion())
                .createdAt(greenhouse.getCreatedAt())
                .updatedAt(greenhouse.getUpdatedAt())
                .build();
    }
}
