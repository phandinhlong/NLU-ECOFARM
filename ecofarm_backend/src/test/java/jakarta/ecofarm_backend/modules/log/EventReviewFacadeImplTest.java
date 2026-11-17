package jakarta.ecofarm_backend.modules.log;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ecofarm_backend.common.exception.PermissionDeniedException;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Batch;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.CultivationEvent;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.OutboxEvent;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.User;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.BatchRepository;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.CultivationEventRepository;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.OutboxEventRepository;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.StubBatchRepository;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.StubOutboxEventRepository;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.UserRepository;
import jakarta.ecofarm_backend.modules.log.facade.impl.EventReviewFacadeImpl;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit test cho EventReviewFacadeImpl: duyệt là idempotent (duyệt lần hai không tạo thêm sự kiện
 * EVENT_APPROVED và không báo xung đột), sự kiện duyệt có hash khớp payload đã lưu, đính chính chỉ
 * cho người được phân công và đánh dấu sự kiện cũ là SUPERSEDED. Dùng constructor injection với
 * repository giả tự viết tay.
 */
class EventReviewFacadeImplTest {

    /** Đếm approval phản ánh số EVENT_APPROVED đã persist, nên lần duyệt thứ hai thấy đã có sẵn. */
    static class FakeEventRepo extends CultivationEventRepository {
        final List<CultivationEvent> persisted = new ArrayList<>();
        CultivationEvent locked;
        FakeEventRepo(CultivationEvent locked) { this.locked = locked; }
        @Override public CultivationEvent findById(Long id) { return locked; }
        @Override public CultivationEvent findByIdForUpdate(Long id) { return locked; }
        @Override public int nextSeq(Long batchId) { return 2; }
        @Override public long countApprovalsByAmendsEventId(Long eventId, String t) {
            return persisted.stream().filter(e -> t.equals(e.getEventType())).count();
        }
        @Override public void persist(CultivationEvent event) { persisted.add(event); }
    }

    /** Khóa batch không có tác dụng phụ trong test; mặc định là đã được phân công (true). */
    static class FakeBatchRepo extends StubBatchRepository {
        final Batch batch;
        final boolean assigned;
        FakeBatchRepo(Batch batch) { this(batch, true); }
        FakeBatchRepo(Batch batch, boolean assigned) { this.batch = batch; this.assigned = assigned; }
        @Override public Batch findByIdForUpdate(Long id) { return batch; }
        @Override public Boolean isAssigned(Long batchId, Long userId) { return assigned; }
    }

    static class NoopOutbox extends StubOutboxEventRepository {
        @Override public void persist(OutboxEvent event) { /* không làm gì trong test */ }
    }

    static class FakeUserRepo implements UserRepository {
        @Override public Optional<User> findByEmailAndFetchRoles(String email) { return Optional.empty(); }
        @Override public Optional<User> findById(Long id) { return Optional.of(User.builder().id(id).build()); }
        @Override public Boolean hasUserByEmail(String email) { return false; }
        @Override public User save(User user) { return user; }
        @Override public Optional<User> findByEmail(String email) { return Optional.empty(); }
        @Override public List<User> findActiveWorkerPage(Long lastIndex, int limit) { return List.of(); }
    }

    private static Batch harvestedBatch() {
        return Batch.builder().id(1L).status("HARVESTED").saltEnc(123L).build();
    }

    private static CultivationEvent confirmedEvent(Batch batch) {
        return CultivationEvent.builder()
                .id(10L).batch(batch).seq(1).eventType("TUOI_NUOC")
                .eventHash("h0").status(EventStatus.CONFIRMED.name()).build();
    }

    private EventReviewFacadeImpl facade(FakeBatchRepo batchRepo, FakeEventRepo repo) {
        return new EventReviewFacadeImpl(batchRepo, repo, new NoopOutbox(), new FakeUserRepo());
    }

    @Test
    void firstApprovePersistsExactlyOneApprovalEvent() {
        Batch batch = harvestedBatch();
        CultivationEvent event = confirmedEvent(batch);
        FakeEventRepo repo = new FakeEventRepo(event);
        facade(new FakeBatchRepo(batch), repo).approve(10L, true);
        assertEquals(1, repo.persisted.size());
        assertEquals(EventType.EVENT_APPROVED.code(), repo.persisted.get(0).getEventType());
        assertEquals(EventStatus.APPROVED.name(), event.getStatus());
    }

    @Test
    void approveTwiceIsIdempotent() {
        // Gọi approve hai lần liên tiếp: lần hai (sự kiện đã APPROVED) không được ném xung đột và
        // không persist thêm EVENT_APPROVED; trạng thái vẫn là APPROVED.
        Batch batch = harvestedBatch();
        CultivationEvent event = confirmedEvent(batch);
        FakeEventRepo repo = new FakeEventRepo(event);
        EventReviewFacadeImpl facade = facade(new FakeBatchRepo(batch), repo);
        facade.approve(10L, true);
        facade.approve(10L, true);
        assertEquals(1, repo.persisted.size());
        assertEquals(EventStatus.APPROVED.name(), event.getStatus());
    }

    @Test
    void approvalPayloadHashMatchesStoredPayload() throws Exception {
        // Hash của sự kiện duyệt phải tính lại được từ payloadJson đã lưu (payload và hash cùng nguồn).
        Batch batch = harvestedBatch();
        CultivationEvent event = confirmedEvent(batch);
        FakeEventRepo repo = new FakeEventRepo(event);
        facade(new FakeBatchRepo(batch), repo).approve(10L, true);

        CultivationEvent approval = repo.persisted.get(0);
        JsonNode storedPayload = new ObjectMapper().readTree(approval.getPayloadJson());
        String rehashed = EventHasher.eventHash(storedPayload, List.of());
        assertEquals(approval.getEventHash(), rehashed);
    }

    @Test
    void amendMarksPriorSuperseded() {
        ObjectMapper m = new ObjectMapper();
        Batch batch = harvestedBatch();
        CultivationEvent prior = confirmedEvent(batch);
        FakeEventRepo repo = new FakeEventRepo(prior);
        var payload = m.createObjectNode().put("schemaVersion", 1);
        var req = new jakarta.ecofarm_backend.modules.log.EventRequests.AmendRequest(
                "cid-amend-1", 123L, payload, "sửa sai số liệu", null);
        facade(new FakeBatchRepo(batch), repo).amend(10L, req, 99L, true);
        assertEquals(EventStatus.SUPERSEDED.name(), prior.getStatus());
        assertEquals(1, repo.persisted.size());
    }

    @Test
    void amendRejectedForNonEngineer() {
        // Amend chỉ dành cho ENGINEER: WORKER/FARMER (engineerOrAdmin=false) bị từ chối dù được phân công.
        ObjectMapper m = new ObjectMapper();
        Batch batch = harvestedBatch();
        CultivationEvent prior = confirmedEvent(batch);
        FakeEventRepo repo = new FakeEventRepo(prior);
        var payload = m.createObjectNode().put("schemaVersion", 1);
        var req = new jakarta.ecofarm_backend.modules.log.EventRequests.AmendRequest(
                "cid-amend-3", 123L, payload, "sửa sai số liệu", null);
        EventReviewFacadeImpl facade = facade(new FakeBatchRepo(batch), repo);
        assertThrows(PermissionDeniedException.class, () -> facade.amend(10L, req, 99L, false));
        assertEquals(0, repo.persisted.size());
        assertEquals(EventStatus.CONFIRMED.name(), prior.getStatus());
    }

    @Test
    void approveCannotRevivePriorAfterAmend() {
        // approve -> amend (gốc thành SUPERSEDED) -> approve lại gốc: phải bị từ chối, không ép về APPROVED.
        ObjectMapper m = new ObjectMapper();
        Batch batch = harvestedBatch();
        CultivationEvent prior = confirmedEvent(batch);
        FakeEventRepo repo = new FakeEventRepo(prior);
        EventReviewFacadeImpl facade = facade(new FakeBatchRepo(batch), repo);
        facade.approve(10L, true);
        var payload = m.createObjectNode().put("schemaVersion", 1);
        var req = new jakarta.ecofarm_backend.modules.log.EventRequests.AmendRequest(
                "cid-amend-4", 123L, payload, "sửa sai số liệu", null);
        facade.amend(10L, req, 99L, true);
        assertEquals(EventStatus.SUPERSEDED.name(), prior.getStatus());
        assertThrows(EventWriteGuard.ConflictException.class, () -> facade.approve(10L, true));
        assertEquals(EventStatus.SUPERSEDED.name(), prior.getStatus());
    }

    @Test
    void amendRejectedWhenActorNotAssignedToBatch() {
        // Actor không được phân công cho lô -> đính chính bị từ chối, không persist gì.
        ObjectMapper m = new ObjectMapper();
        Batch batch = harvestedBatch();
        CultivationEvent prior = confirmedEvent(batch);
        FakeEventRepo repo = new FakeEventRepo(prior);
        var payload = m.createObjectNode().put("schemaVersion", 1);
        var req = new jakarta.ecofarm_backend.modules.log.EventRequests.AmendRequest(
                "cid-amend-2", 123L, payload, "sửa sai số liệu", null);
        EventReviewFacadeImpl facade = facade(new FakeBatchRepo(batch, false), repo);
        assertThrows(PermissionDeniedException.class, () -> facade.amend(10L, req, 99L, true));
        assertEquals(0, repo.persisted.size());
        assertEquals(EventStatus.CONFIRMED.name(), prior.getStatus());
    }
}
