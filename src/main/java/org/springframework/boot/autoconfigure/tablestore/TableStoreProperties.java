package org.springframework.boot.autoconfigure.tablestore;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @project: tablestore-spring-boot-starter
 * @description: TableStore连接配置
 * @author: Kenn
 * @create: 2019-02-28 10:53
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "tablestore")
public class TableStoreProperties {
    /**
     * 阿里云访问AK
     */
    private String ak;
    /**
     * 阿里云访问SK
     */
    private String sk;
    /**
     * 阿里云表格存储服务域名地址
     */
    private String endpoint;
    /**
     * 阿里云表格存储实例名
     */
    private String instance;
}
