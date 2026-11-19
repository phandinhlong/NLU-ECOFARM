package jakarta.ecofarm_backend.infrastructure.service.impl;

import jakarta.ecofarm_backend.common.dto.response.PageResponse;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Anchor;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.AnchorRepository;
import jakarta.ecofarm_backend.infrastructure.service.AnchorService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class AnchorServiceImpl implements AnchorService {

    @Inject
    private AnchorRepository anchorRepository;

    @Override
    public void persist(Anchor anchor) {
        this.anchorRepository.persist(anchor);
    }

    @Override
    public Anchor findById(Long id) {
        return this.anchorRepository.findById(id);
    }

    @Override
    public List<Anchor> findByStatus(String status) {
        return this.anchorRepository.findByStatus(status);
    }

    @Override
    public PageResponse<Anchor> findPage(int page, int size) {
        List<Anchor> content = this.anchorRepository.findPage(page, size);
        long total = this.anchorRepository.count();
        return new PageResponse<>(content, page, size, total);
    }

    @Override
    public List<Anchor> findBySnapshotId(Long snapshotId) {
        return this.anchorRepository.findBySnapshotId(snapshotId);
    }
}
