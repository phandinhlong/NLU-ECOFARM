package jakarta.ecofarm_backend.modules.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ecofarm_backend.common.anotation.RequireRoles;
import jakarta.ecofarm_backend.common.dto.request.AuditLogCriteria;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.AuditLog;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.BaseEntity;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.User;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.AuditLogRepository;
import jakarta.ecofarm_backend.infrastructure.service.AuditLogService;
import jakarta.ecofarm_backend.infrastructure.service.impl.AuditLogServiceImpl;
import jakarta.ecofarm_backend.modules.audit.facade.AuditLogFacade;
import jakarta.ecofarm_backend.modules.audit.facade.impl.AuditLogFacadeImpl;
import jakarta.ecofarm_backend.modules.audit.rest.AuditLogResource;
import jakarta.persistence.PreRemove;
import jakarta.persistence.PreUpdate;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class AuditLogReadTest {
    private final AuditLogCriteria criteria = new AuditLogCriteria();
    private final AuditLogServiceImpl service = new AuditLogServiceImpl();
    private final AuditLogFacadeImpl facade = new AuditLogFacadeImpl();
    private List<AuditLog> logs = List.of();
    private AuditLog persisted;
    private int pageCalls;
    private int countCalls;

    AuditLogReadTest() throws Exception {
        AuditLogRepository repository = proxy(AuditLogRepository.class, (p, method, args) -> switch (method.getName()) {
            case "findPage" -> {
                assertSame(criteria, args[0]);
                pageCalls++;
                yield logs;
            }
            case "count" -> {
                assertSame(criteria, args[0]);
                countCalls++;
                yield 42L;
            }
            case "persist" -> { persisted = (AuditLog) args[0]; yield null; }
            case "findById" -> persisted != null && persisted.getId().equals(args[0]) ? persisted : null;
            default -> throw new AssertionError("Unexpected repository call: " + method.getName());
        });
        inject(service, "auditLogRepository", repository);
        inject(facade, "auditLogService", service);
    }

    @Test
    void paginationHandlesEmptyPartialFullAndLookaheadPages() {
        for (int size : new int[]{0, 1, 19, 20, 21}) {
            logs = IntStream.range(0, size).mapToObj(index -> log(100L - index, null)).toList();
            var response = facade.findAuditLogs(criteria);
            var page = response.getData();
            int expectedSize = Math.min(size, 20);
            assertTrue(response.getIsSuccess());
            assertEquals(expectedSize, page.getContent().size());
            assertEquals(size > 20, page.isHasMore());
            assertEquals(size == 0 ? null : 101L - expectedSize, page.getLastIndex());
            assertEquals(logs.subList(0, expectedSize).stream().map(AuditLog::getId).toList(),
                    page.getContent().stream().map(item -> item.getId()).toList());
        }
        assertEquals(5, pageCalls);
        assertEquals(0, countCalls);
    }

    @Test
    void dtoMapsAllFieldsAndNullActorWithoutExposingUserOrBaseEntityInternals() {
        User actor = User.builder().id(7L).fullName("Audit Admin").email("admin@example.test")
                .password("not-for-the-response").build();
        logs = List.of(log(101L, actor), log(100L, null));
        var content = facade.findAuditLogs(criteria).getData().getContent();
        var dto = content.get(0);
        assertEquals(101L, dto.getId());
        assertEquals(7L, dto.getActorId());
        assertEquals("Audit Admin", dto.getActorName());
        assertEquals("admin@example.test", dto.getActorEmail());
        assertEquals("READ", dto.getAction());
        assertEquals("GREENHOUSE", dto.getEntity());
        assertEquals(5L, dto.getEntityId());
        assertEquals("Viewed greenhouse", dto.getDetail());
        assertEquals("127.0.0.1", dto.getIpAddr());
        assertEquals(1_700_000_000_123L, dto.getCreatedAt());
        assertNull(content.get(1).getActorId());
        assertNull(content.get(1).getActorName());
        assertNull(content.get(1).getActorEmail());
        var json = new ObjectMapper().valueToTree(dto);
        assertEquals(10, json.size());
        Set.of("id", "actorId", "actorName", "actorEmail", "action", "entity", "entityId", "detail", "ipAddr", "createdAt")
                .forEach(field -> assertTrue(json.has(field), field));
        assertFalse(json.has("actor"));
        assertFalse(json.has("password"));
        assertFalse(json.has("version"));
        assertFalse(json.has("active"));
    }

    @Test
    void countDelegatesWithoutFetchingAPageAndPreservesCursorCriteria() {
        criteria.setLastIndex(50L);
        var response = facade.countAuditLogs(criteria);
        assertTrue(response.getIsSuccess());
        assertEquals(42L, response.getData());
        assertEquals(50L, criteria.getLastIndex());
        assertEquals(1, countCalls);
        assertEquals(0, pageCalls);
    }

    @Test
    void existingServicePersistAndFindByIdRemainAvailable() {
        AuditLog log = log(1L, null);
        service.persist(log);
        assertSame(log, persisted);
        assertSame(log, service.findById(1L));
        assertNull(service.findById(2L));
    }

    @Test
    void resourceHasOnlyTwoValidatedAdminGetEndpointsAndUsesFacade() throws Exception {
        assertEquals("/v1/audit-logs", AuditLogResource.class.getAnnotation(Path.class).value());
        assertArrayEquals(new String[]{"ADMIN"}, AuditLogResource.class.getAnnotation(RequireRoles.class).value());
        assertEquals(2, AuditLogResource.class.getDeclaredMethods().length);
        for (Method method : AuditLogResource.class.getDeclaredMethods()) {
            assertNotNull(method.getAnnotation(GET.class));
            assertEquals(1, method.getParameterCount());
            assertTrue(method.getParameters()[0].isAnnotationPresent(Valid.class));
            assertTrue(method.getParameters()[0].isAnnotationPresent(BeanParam.class));
        }
        assertEquals("/statistics", AuditLogResource.class.getMethod("countAuditLogs", AuditLogCriteria.class)
                .getAnnotation(Path.class).value());
        assertEquals(AuditLogFacade.class, AuditLogResource.class.getDeclaredField("auditLogFacade").getType());
        assertEquals(AuditLogService.class, AuditLogFacadeImpl.class.getDeclaredField("auditLogService").getType());
        AuditLogResource resource = new AuditLogResource();
        inject(resource, "auditLogFacade", facade);
        assertTrue(resource.findAuditLogs(criteria).getData().getContent().isEmpty());
        assertEquals(42L, resource.countAuditLogs(criteria).getData());
    }

    @Test
    void criteriaAcceptOptionalTrimmedExactFiltersAndInclusiveEpochBounds() throws Exception {
        // No Bean Validation provider is required: exercise the @AssertTrue method directly.
        assertNotNull(AuditLogCriteria.class.getMethod("isValid").getAnnotation(AssertTrue.class));
        assertEquals(20, AuditLogCriteria.LENGTH);
        assertTrue(criteria.isValid());
        criteria.setAction("  READ  ");
        criteria.setEntity("  GREENHOUSE  ");
        assertEquals("READ", criteria.getAction());
        assertEquals("GREENHOUSE", criteria.getEntity());
        criteria.setAction(" \t ");
        criteria.setEntity("");
        assertNull(criteria.getAction());
        assertNull(criteria.getEntity());
        assertTrue(criteria.isValid());
        assertTrue(AuditLogCriteria.builder().lastIndex(1L).actorId(1L).entityId(1L)
                .action(" " + "a".repeat(100) + " ").entity(" " + "e".repeat(100) + " ")
                .from(0L).to(0L).build().isValid());
        assertTrue(AuditLogCriteria.builder().from(0L).build().isValid());
        assertTrue(AuditLogCriteria.builder().to(Long.MAX_VALUE).build().isValid());
        assertTrue(AuditLogCriteria.builder().from(Long.MAX_VALUE).to(Long.MAX_VALUE).build().isValid());
    }

    @Test
    void criteriaRejectInvalidIdsLengthsAndTimeWindows() {
        for (long invalidId : new long[]{0L, -1L, Long.MIN_VALUE}) {
            assertFalse(AuditLogCriteria.builder().lastIndex(invalidId).build().isValid());
            assertFalse(AuditLogCriteria.builder().actorId(invalidId).build().isValid());
            assertFalse(AuditLogCriteria.builder().entityId(invalidId).build().isValid());
        }
        assertFalse(AuditLogCriteria.builder().action("a".repeat(101)).build().isValid());
        assertFalse(AuditLogCriteria.builder().entity("e".repeat(101)).build().isValid());
        assertFalse(AuditLogCriteria.builder().from(-1L).build().isValid());
        assertFalse(AuditLogCriteria.builder().to(-1L).build().isValid());
        assertFalse(AuditLogCriteria.builder().from(2L).to(1L).build().isValid());
    }

    @Test
    void lifecycleCallbacksRejectUpdateAndRemovalButAllowCreation() throws Exception {
        AuditLog log = new AuditLog();
        log.setAction("READ");
        log.setEntity("GREENHOUSE");
        log.setEntityId(5L);
        log.setActor(null);
        log.setDetail("Created through existing setters");
        log.setIpAddr("127.0.0.1");
        Method prePersist = BaseEntity.class.getDeclaredMethod("prePersist");
        prePersist.setAccessible(true);
        prePersist.invoke(log);
        assertNotNull(log.getCreatedAt());
        Long updatedAt = log.getUpdatedAt();
        // BaseEntity soft deletion is also an update, never an escape hatch.
        log.setActive(false);
        Method update = AuditLog.class.getDeclaredMethod("preUpdate");
        Method remove = AuditLog.class.getDeclaredMethod("preRemove");
        assertNotNull(update.getAnnotation(PreUpdate.class));
        assertNotNull(remove.getAnnotation(PreRemove.class));
        for (Method callback : new Method[]{update, remove}) {
            callback.setAccessible(true);
            var failure = assertThrows(InvocationTargetException.class, () -> callback.invoke(log));
            assertInstanceOf(IllegalStateException.class, failure.getCause());
            assertTrue(failure.getCause().getMessage().contains("append-only"));
        }
        assertEquals(updatedAt, log.getUpdatedAt());
    }

    private static AuditLog log(Long id, User actor) {
        return AuditLog.builder().id(id).actor(actor).action("READ").entity("GREENHOUSE").entityId(5L)
                .detail("Viewed greenhouse").ipAddr("127.0.0.1").createdAt(1_700_000_000_123L).build();
    }

    private static <T> T proxy(Class<T> type, InvocationHandler handler) {
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler));
    }

    private static void inject(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
