package jakarta.ecofarm_backend.common.anotation.interceptor;

import jakarta.annotation.Priority;
import jakarta.ecofarm_backend.common.anotation.DatabaseTransactional;
import jakarta.ecofarm_backend.infrastructure.logging.LoggingFilter;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@DatabaseTransactional
@Interceptor
@Priority(Interceptor.Priority.APPLICATION)
public class DatabaseTransactionalInterceptor {
    private static final Logger log = LogManager.getLogger(DatabaseTransactionalInterceptor.class);

    @Inject
    private EntityManager em;

    @AroundInvoke
    public Object manage(InvocationContext ctx) throws Exception {
        log.info("Run create transaction");
        EntityTransaction tx = em.getTransaction();

        if (tx.isActive()) {
            return ctx.proceed();
        }

        tx.begin();
        try {
            Object result = ctx.proceed();
            tx.commit();
            log.info("Run end transaction");
            return result;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            log.info("Run end transaction");
            throw e;
        }

    }
}
