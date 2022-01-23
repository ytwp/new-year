package wang.yeting.newyear.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author : weipeng
 * @date : 2020-08-20 19:10
 */

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Permission {

    String[] permissions() default {};

    boolean cms() default false;

    boolean login() default true;
}
