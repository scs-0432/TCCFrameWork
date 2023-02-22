package com.scs.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface TCC {

    /**
     * 指明TCC的confirm方法，具备幂等性
     * @return string
     */
    String confirmMethod() default "";

    /**
     * 指明TCC的cancel方法，具备幂等性
     * @return
     */
    String cancelMethod() default "";

}
