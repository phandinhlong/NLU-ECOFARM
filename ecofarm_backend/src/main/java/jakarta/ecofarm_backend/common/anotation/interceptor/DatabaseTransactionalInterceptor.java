package jakarta.ecofarm_backend.common.anotation.interceptor;

import jakarta.annotation.Priority;
import jakarta.annotation.Resource;
import jakarta.ecofarm_backend.common.anotation.DatabaseTransactional;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import jakarta.transaction.Status;
import jakarta.transaction.UserTransaction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@DatabaseTransactional
@Interceptor
@Priority(Interceptor.Priority.APPLICATION)
public class DatabaseTransactionalInterceptor {
    private static final Logger log = LogManager.getLogger(DatabaseTransactionalInterceptor.class);

    @Resource
    private UserTransaction utx;

    @AroundInvoke
    public Object manage(InvocationContext ctx) throws Exception {
        if (utx == null || utx.getStatus() != Status.STATUS_NO_TRANSACTION) {
            return ctx.proceed();
        }

        log.info("Run create transaction");
        utx.begin();
        try {
            Object result = ctx.proceed();
            utx.commit();
            log.info("Run end transaction");
            return result;
        } catch (Exception e) {
            if (utx.getStatus() == Status.STATUS_ACTIVE || utx.getStatus() == Status.STATUS_MARKED_ROLLBACK) {
                utx.rollback();
            }
            log.info("Run end transaction");
            throw e;
        }
    }
}
