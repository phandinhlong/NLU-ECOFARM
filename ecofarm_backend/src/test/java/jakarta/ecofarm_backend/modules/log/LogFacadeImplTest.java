package jakarta.ecofarm_backend.modules.log;

import jakarta.ecofarm_backend.infrastructure.persistence.entity.Batch;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.EventAttachment;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.BatchRepository;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.CultivationEventRepository;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.EventAttachmentRepository;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.OutboxEventRepository;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.UserRepository;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.User;
import jakarta.ecofarm_backend.common.exception.BadRequestException;
import jakarta.ecofarm_backend.modules.log.EventRequests.CreateEventRequest;
import jakarta.ecofarm_backend.modules.log.facade.LogFacade.UploadInput;
import jakarta.ecofarm_backend.modules.log.facade.impl.LogFacadeImpl;
import jakarta.ecofarm_backend.modules.log.service.AttachmentStorageService;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit test cho luồng tải tệp lên của LogFacadeImpl: nếu lưu DB thất bại sau khi tệp đã ghi ra đĩa,
 * facade phải dọn (xóa) mọi tệp mồ côi để không còn tệp không được tham chiếu. Dùng constructor
 * injection với service/repository giả tự viết tay (không có Mockito trong classpath).
 */
class LogFacadeImplTest {

    /** Lưu lại các tệp đã "ghi" và ghi nhận các khóa đã bị xóa. */
    static class RecordingStorage extends AttachmentStorageService {
        final List<String> deleted = new ArrayList<>();
        int counter = 0;
        @Override public LocalImageStore.StoredImage save(java.io.InputStream input, String mimeType) {
            return new LocalImageStore.StoredImage("key-" + (counter++), "sha", mimeType);
        }
        @Override public void deleteQuietly(String fileKey) { deleted.add(fileKey); }
    }

    /** Batch ACTIVE (không khóa ghi) để luồng upload vượt qua các kiểm tra trước khi lưu DB. */
    static class FakeBatchRepo extends BatchRepository {
        @Override public Batch findByIdForUpdate(Long id) { return Batch.builder().id(id).status("ACTIVE").build(); }
        @Override public long countActiveAssignment(Long batchId, Long userId) { return 1; }
    }

    /** Luôn ném lỗi khi lưu bản ghi đính kèm, mô phỏng transaction DB thất bại sau khi đã ghi tệp. */
    static class FailingAttachmentRepo implements EventAttachmentRepository {
        @Override public void persist(EventAttachment attachment) { throw new IllegalStateException("db down"); }
        @Override public EventAttachment merge(EventAttachment attachment) { return attachment; }
        @Override public List<EventAttachment> findActiveByFileKey(String fileKey) { return List.of(); }
        @Override public List<EventAttachment> findUnlinkedByFileKey(String fileKey) { return List.of(); }
        @Override public List<String> findSha256ByFileKeys(List<String> fileKeys) { return List.of(); }
        @Override public List<EventAttachment> findActiveByEventIdOrderById(Long eventId) { return List.of(); }
        @Override public List<EventAttachment> findActiveByEventIdsOrderById(List<Long> eventIds) { return List.of(); }
    }

    static class StubUserRepo implements UserRepository {
        @Override public Optional<User> findByEmailAndFetchRoles(String email) { return Optional.empty(); }
        @Override public Optional<User> findById(Long id) { return Optional.of(User.builder().id(id).build()); }
        @Override public Boolean hasUserByEmail(String email) { return false; }
        @Override public User save(User user) { return user; }
        @Override public Optional<User> findByEmail(String email) { return Optional.empty(); }
    }

    @Test
    void uploadDeletesStoredFilesWhenDbPersistFails() throws Exception {
        RecordingStorage storage = new RecordingStorage();
        LogFacadeImpl facade = new LogFacadeImpl(
                new FakeBatchRepo(),
                new CultivationEventRepository(),
                new FailingAttachmentRepo(),
                new OutboxEventRepository(),
                new StubUserRepo(),
                storage);

        List<UploadInput> files = List.of(
                new UploadInput("image/png", new byte[]{1}),
                new UploadInput("image/png", new byte[]{2}));

        assertThrows(IllegalStateException.class,
                () -> facade.upload(1L, files, 99L, true));
        // Cả hai tệp đã ghi phải được dọn vì DB không commit.
        assertEquals(List.of("key-0", "key-1"), storage.deleted);
    }

    @Test
    void createWithNullRequestIsBadRequest() {
        // Body null/thiếu trường bắt buộc phải thành lỗi đầu vào 400, không phải NPE/500.
        LogFacadeImpl facade = new LogFacadeImpl(
                new FakeBatchRepo(),
                new CultivationEventRepository(),
                new FailingAttachmentRepo(),
                new OutboxEventRepository(),
                new StubUserRepo(),
                new RecordingStorage());
        assertThrows(BadRequestException.class,
                () -> facade.create(1L, (CreateEventRequest) null, 99L, true, 300000L));
    }
}
