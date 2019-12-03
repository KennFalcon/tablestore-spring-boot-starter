package org.springframework.boot.autoconfigure.tablestore.annotation;

import java.lang.annotation.*;

/**
 * @project: tablestore-spring-boot-starter
 * @description: 字段注解
 * @author: Kenn
 * @create: 2018-11-24 14:27
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface FieldMapper {
    /**
     *
     */
    boolean primaryKey() default false;

    /**
     * 映射的字段名
     *
     * @return field name
     */
    String name() default "";

    /**
     * 字段类型
     *
     * @return field class type
     */
    Class<?> className() default void.class;

    /**
     * 数组子类类型
     *
     * @return element class type
     */
    Class<?> elementClassName() default void.class;
}
