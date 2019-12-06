package org.springframework.boot.autoconfigure.tablestore;

import java.util.List;

/**
 * @project: tablestore-spring-boot-starter
 * @description:
 * @author: Kenn
 * @create: 2019-12-05 19:31
 */
public class TableStoreMultiProperties {
    /**
     * 阿里云访问AK
     */
    private String ak;
    /**
     * 阿里云访问SK
     */
    private String sk;

    List<TableStoreProperties> multi;
}
