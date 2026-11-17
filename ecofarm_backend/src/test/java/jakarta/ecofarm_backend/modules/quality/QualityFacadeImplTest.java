package jakarta.ecofarm_backend.modules.quality;

import jakarta.ecofarm_backend.common.dto.response.BaseResponse;
import jakarta.ecofarm_backend.common.dto.response.QcResponse;
import jakarta.ecofarm_backend.common.enums.ErrorCode;
import jakarta.ecofarm_backend.common.exception.ConflictException;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Batch;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.CultivationEvent;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.QcRecord;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.User;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Variety;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.BatchRepository;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.CultivationEventRepository;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.OutboxEventRepository;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.QcRecordRepository;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.StubBatchRepository;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.StubOutboxEventRepository;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.UserRepository;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.VarietyRepository;
import jakarta.ecofarm_backend.modules.quality.QualityRequests.QcRequest;
import jakarta.ecofarm_backend.modules.quality.facade.impl.QualityFacadeImpl;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit test cho nhánh QC của QualityFacadeImpl, dùng repository giả tự viết tay (không có Mockito
 * trong classpath) và constructor injection để khởi tạo facade. Các test cố định quy tắc phía server:
 * phải có ngưỡng, kỹ sư không thể xác nhận khi cửa sổ cách ly chưa đủ, và chỉ ghi là đạt khi cả Brix,
 * cách ly và có xác nhận đều thỏa.
 */
class QualityFacadeImplTest {

    private static final long HARVESTED_AT = 1_000_000_000_000L;

    static class FakeBatchRepo extends StubBatchRepository {
        Batch batch;
        boolean failOnUnlockedRead;
        FakeBatchRepo(Batch b) { this.batch = b; }
        @Override public Batch findByIdForUpdate(Long id) { return batch; }
        @Override public Batch findById(Long id) {
            // Dùng để chứng minh luồng ghi (listQc) đi qua đường có khóa, không phải findById.
            if (failOnUnlockedRead) throw new AssertionError("phải dùng findByIdForUpdate");
            return batch;
        }
        @Override public Batch merge(Batch b) { return b; }
    }

    static class FakeEventRepo extends CultivationEventRepository {
        final List<CultivationEvent> active;
        FakeEventRepo(List<CultivationEvent> active) { this.active = active; }
        @Override public List<CultivationEvent> findActiveByBatchId(Long batchId) { return active; }
    }

    static class FakeVarietyRepo extends VarietyRepository {
        final Variety variety;
        FakeVarietyRepo(Variety v) { this.variety = v; }
        @Override public Variety findById(Long id) { return variety; }
    }

    static class FakeQcRepo implements QcRecordRepository {
        final List<QcRecord> persisted = new ArrayList<>();
        @Override public void persist(QcRecord record) { persisted.add(record); }
        @Override public QcRecord merge(QcRecord record) { return record; }
        @Override public List<QcRecord> findActiveByBatchIdOrderByIdDesc(Long batchId) { return persisted; }
    }

    static class FakeUserRepo implements UserRepository {
        @Override public Optional<User> findByEmailAndFetchRoles(String email) { return Optional.empty(); }
        @Override public Optional<User> findById(Long id) { return Optional.of(User.builder().id(id).build()); }
        @Override public Boolean hasUserByEmail(String email) { return false; }
        @Override public User save(User user) { return user; }
        @Override public Optional<User> findByEmail(String email) { return Optional.empty(); }
        @Override public List<User> findActiveWorkerPage(Long lastIndex, int limit) { return List.of(); }
    }

    private QualityFacadeImpl facade(Batch batch, Variety variety, List<CultivationEvent> events, FakeQcRepo qc) {
        return new QualityFacadeImpl(
                new FakeBatchRepo(batch),
                new FakeEventRepo(events),
                qc,
                new FakeVarietyRepo(variety),
                new StubOutboxEventRepository() {},
                new FakeUserRepo());
    }

    private static Batch harvestedBatch() {
        Variety v = Variety.builder().id(7L).build();
        return Batch.builder()
                .id(1L)
                .status("HARVESTED")
                .harvestedAt(HARVESTED_AT)
                .variety(v)
                .build();
    }

    private static Variety variety(Double minBrix, Integer minDays) {
        return Variety.builder()
                .id(7L)
                .minBrix(minBrix)
                .minIsolationDays(minDays)
                .build();
    }

    @Test
    void missingThresholdsNeverPass() {
        FakeQcRepo qc = new FakeQcRepo();
        QualityFacadeImpl f = facade(harvestedBatch(), variety(null, null), List.of(), qc);
        ConflictException ex = assertThrows(ConflictException.class,
                () -> f.recordQc(1L, new QcRequest(12.0, 1.0, true, null, null, null), 99L));
        assertEquals(ErrorCode.QC_MISSING_THRESHOLD.getCode(), ex.getCode());
        assertTrue(qc.persisted.isEmpty());
    }

    @Test
    void engineerCannotConfirmPastUnmetIsolation() {
        // Không có sự kiện liên quan cách ly -> cửa sổ cách ly chưa đủ; confirmPassed phải bị từ chối.
        FakeQcRepo qc = new FakeQcRepo();
        QualityFacadeImpl f = facade(harvestedBatch(), variety(10.0, 7), List.of(), qc);
        ConflictException ex = assertThrows(ConflictException.class,
                () -> f.recordQc(1L, new QcRequest(12.0, 1.0, true, null, null, null), 99L));
        assertEquals(ErrorCode.QC_ISOLATION_NOT_ENOUGH.getCode(), ex.getCode());
        assertTrue(qc.persisted.isEmpty());
    }

    @Test
    void listQcUsesBatchLock() {
        // listQc revalidate có thể ghi DB -> phải lấy batch qua findByIdForUpdate, không phải findById.
        FakeQcRepo qc = new FakeQcRepo();
        Batch batch = harvestedBatch();
        FakeBatchRepo batchRepo = new FakeBatchRepo(batch);
        batchRepo.failOnUnlockedRead = true;
        QualityFacadeImpl f = new QualityFacadeImpl(
                batchRepo, new FakeEventRepo(List.of()), qc,
                new FakeVarietyRepo(variety(10.0, 7)), new StubOutboxEventRepository() {}, new FakeUserRepo());
        BaseResponse<List<QcResponse>> res = f.listQc(1L);
        assertNotNull(res.getData());
    }

    @Test
    void lowBrixWithoutConfirmIsRecordedAsRejected() {
        // Không xác nhận và brix dưới ngưỡng -> không vi phạm, ghi là REJECTED (không đạt).
        FakeQcRepo qc = new FakeQcRepo();
        QualityFacadeImpl f = facade(harvestedBatch(), variety(10.0, 7), List.of(), qc);
        BaseResponse<QcResponse> res = f.recordQc(1L, new QcRequest(5.0, 1.0, false, "M", "note", null), 99L);
        QcResponse view = res.getData();
        assertNotNull(view);
        assertEquals("REJECTED", view.status());
        assertFalse(Boolean.TRUE.equals(view.passed()));
        assertEquals(1, qc.persisted.size());
    }
}
