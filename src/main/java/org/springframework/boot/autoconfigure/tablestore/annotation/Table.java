package org.springframework.boot.autoconfigure.tablestore.annotation;

import java.lang.annotation.*;

/**
 * Created on 2020/10/09
 *
 * @author Kenn
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
