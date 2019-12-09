package org.springframework.boot.autoconfigure.tablestore.annotation;

import org.springframework.boot.autoconfigure.tablestore.utils.compress.NoCompress;

import java.lang.annotation.*;

/**
 * @project: tablestore-spring-boot-starter
 * @description: TableStore Column Annotation
 * @author: Kenn
 * @create: 2018-11-24 14:27
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface OtsColumn {
    /**
     * Primary key or not (default false)
     *
     * @return
     */
    boolean primaryKey() default false;

    /**
     * Auto increase primary key or not (default false)
     *
     * @return
     */
    boolean autoIncrease() default false;

    /**
     * Name of table store will store (default self name)
     *
     * @return field name
     */
    String name() default "";

    /**
     * Writable or not
     *
     * @return
     */
    boolean writable() default true;

    /**
     * Readable or not
     *
     * @return
     */
    boolean readable() default true;

    /**
     * Field class (default self class)
     *
     * @return field class type
     */
    Class<?> clazz() default void.class;

    /**
     * Array child element class
     *
     * @return element class type
     */
    Class<?> elementClazz() default void.class;

    /**
     * Charset (default utf-8）
     *
     * @return element charset
     */
    String charset() default "utf-8";

    /**
     * Compress class (default null)
     *
     * @return
     */
    Class<?> compress() default NoCompress.class;
}
