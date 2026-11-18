package jakarta.ecofarm_backend.modules.audit;

import jakarta.ecofarm_backend.common.dto.request.AuditLogCriteria;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.AuditLog;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.impl.AuditLogRepositoryImpl;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AuditLogRepositoryTest {
    private final AuditLogRepositoryImpl repository = new AuditLogRepositoryImpl();
    private final List<AuditLog> rows = List.of(AuditLog.builder().id(9L).isActive(false).build());
    private final Map<String, Object> parameters = new HashMap<>();
    private String jpql;
    private Class<?> resultType;
    private Integer maxResults;
    private AuditLog persisted;

    AuditLogRepositoryTest() throws Exception {
        TypedQuery<?> query = proxy(TypedQuery.class, (p, method, args) -> switch (method.getName()) {
            case "setParameter" -> { parameters.put((String) args[0], args[1]); yield p; }
            case "setMaxResults" -> { maxResults = (Integer) args[0]; yield p; }
            case "getResultList" -> rows;
            case "getSingleResult" -> 42L;
            default -> throw new AssertionError("Unexpected query call: " + method.getName());
        });
        EntityManager em = proxy(EntityManager.class, (p, method, args) -> switch (method.getName()) {
            case "createQuery" -> {
                jpql = (String) args[0];
                resultType = (Class<?>) args[1];
                parameters.clear();
                maxResults = null;
                yield query;
            }
            case "persist" -> { persisted = (AuditLog) args[0]; yield null; }
            case "find" -> { assertEquals(AuditLog.class, args[0]); yield persisted; }
            default -> throw new AssertionError("Unexpected EntityManager call: " + method.getName());
        });
        Field field = AuditLogRepositoryImpl.class.getDeclaredField("em");
        field.setAccessible(true);
        field.set(repository, em);
    }

    @Test
    void pageFetchesNullableActorWithDescendingCursorAndBoundExactFilters() {
        String action = "READ' OR 1=1 --";
        AuditLogCriteria criteria = AuditLogCriteria.builder().lastIndex(99L).actorId(7L)
                .action("  " + action + "  ").entity(" GREENHOUSE ").entityId(5L).from(0L).to(1000L).build();
        assertSame(rows, repository.findPage(criteria));
        assertEquals(AuditLog.class, resultType);
        assertEquals(21, maxResults);
        assertTrue(jpql.contains("LEFT JOIN FETCH a.actor"));
        assertTrue(jpql.contains("a.id < :lastIndex"));
        assertTrue(jpql.endsWith("ORDER BY a.id DESC"));
        assertTrue(jpql.contains("a.actor.id = :actorId"));
        assertTrue(jpql.contains("a.action = :action"));
        assertTrue(jpql.contains("a.entity = :entity"));
        assertTrue(jpql.contains("a.entityId = :entityId"));
        assertTrue(jpql.contains("a.createdAt >= :from"));
        assertTrue(jpql.contains("a.createdAt <= :to"));
        assertFalse(jpql.contains(action));
        assertFalse(jpql.contains("GREENHOUSE"));
        assertFalse(jpql.contains("LIKE"));
        assertFalse(jpql.contains("isActive"));
        assertEquals(Map.of("lastIndex", 99L, "actorId", 7L, "action", action,
                "entity", "GREENHOUSE", "entityId", 5L, "from", 0L, "to", 1000L), parameters);
    }

    @Test
    void countAppliesIdenticalFiltersButIgnoresCursorAndPagination() {
        AuditLogCriteria criteria = AuditLogCriteria.builder().lastIndex(99L).actorId(7L)
                .action(" READ ").entity(" GREENHOUSE ").entityId(5L).from(0L).to(1000L).build();
        repository.findPage(criteria);
        Map<String, Object> expected = new HashMap<>(parameters);
        expected.remove("lastIndex");
        String commonFilters = jpql.substring(jpql.indexOf(" WHERE"), jpql.indexOf(" AND a.id < :lastIndex"));
        assertEquals(42L, repository.count(criteria));
        assertEquals(Long.class, resultType);
        assertEquals("SELECT COUNT(a) FROM AuditLog a" + commonFilters, jpql);
        assertEquals(expected, parameters);
        assertNull(maxResults);
        assertEquals(99L, criteria.getLastIndex());
    }

    @Test
    void absentAndBlankFiltersDoNotHideAnonymousOrInactiveHistory() {
        AuditLogCriteria criteria = AuditLogCriteria.builder().action("  ").entity("\t").build();
        repository.findPage(criteria);
        assertEquals("SELECT a FROM AuditLog a LEFT JOIN FETCH a.actor WHERE 1 = 1 ORDER BY a.id DESC", jpql);
        assertTrue(parameters.isEmpty());
        repository.count(criteria);
        assertEquals("SELECT COUNT(a) FROM AuditLog a WHERE 1 = 1", jpql);
        assertTrue(parameters.isEmpty());
        assertNull(maxResults);
    }

    @Test
    void epochBoundsCanBeUsedIndependently() {
        repository.findPage(AuditLogCriteria.builder().from(0L).build());
        assertEquals(Map.of("from", 0L), parameters);
        assertTrue(jpql.contains("a.createdAt >= :from"));
        assertFalse(jpql.contains(":to"));
        repository.count(AuditLogCriteria.builder().to(Long.MAX_VALUE).build());
        assertEquals(Map.of("to", Long.MAX_VALUE), parameters);
        assertTrue(jpql.contains("a.createdAt <= :to"));
        assertFalse(jpql.contains(":from"));
    }

    @Test
    void persistAndFindByIdRetainTheirEntityManagerBehavior() {
        AuditLog log = rows.get(0);
        repository.persist(log);
        assertSame(log, persisted);
        assertSame(log, repository.findById(log.getId()));
    }

    private static <T> T proxy(Class<T> type, InvocationHandler handler) {
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler));
    }
}
