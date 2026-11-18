package com.EnSe.Movienatic.security;

import java.time.temporal.ChronoUnit;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimited {
    long capacity() default 60;
    long duration() default 1;
    ChronoUnit unit() default ChronoUnit.MINUTES;
}
