package org.springframework.boot.autoconfigure.tablestore.annotation;

import org.springframework.boot.autoconfigure.tablestore.enums.OtsColumnType;
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
     * @return true of false
     */
    boolean primaryKey() default false;

    /**
     * Auto increase primary key or not (default false)
     *
     * @return true of false
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
     * @return true of false
     */
    boolean writable() default true;

    /**
     * Readable or not
     *
     * @return true of false
     */
    boolean readable() default true;

    /**
     * Ots store type
     *
     * @return store type
     */
    OtsColumnType type() default OtsColumnType.NONE;

    /**
     * Compress class (default null)
     *
     * @return compress
     */
    Class<?> compress() default NoCompress.class;
}
