package jakarta.ecofarm_backend.common.anotation;

import jakarta.interceptor.InterceptorBinding;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import jakarta.enterprise.util.Nonbinding;

/**
 * Yêu cầu người dùng đã đăng nhập phải có ít nhất một trong các role liệt kê.
 */
@InterceptorBinding
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRoles {
    @Nonbinding
    String[] value();

    @Nonbinding
    boolean requireAll() default false;
}
