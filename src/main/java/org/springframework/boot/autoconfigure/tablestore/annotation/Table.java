package org.springframework.boot.autoconfigure.tablestore.annotation;

import java.lang.annotation.*;

/**
 * @project: common-data-utils
 * @description: 表注解
 * @author: Kenn
 * @create: 2018-11-24 14:27
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Table {

    /**
     * 标注该对象映射的表名
     *
     * @return 该对象对应映射的表名
     */
    String name();

    /**
     * 标准该对象映射的索引名
     *
     * @return 该对象映射的索引名
     */
    String index() default "";
}
