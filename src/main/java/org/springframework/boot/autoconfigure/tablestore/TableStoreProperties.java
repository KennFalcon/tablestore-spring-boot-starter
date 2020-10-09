package org.springframework.boot.autoconfigure.tablestore;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @project: tablestore-spring-boot-starter
 * @description: TableStore连接配置
 * @author: Kenn
 * @create: 2019-02-28 10:53
 */
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

    public String getAk() {
        return ak;
    }

    public void setAk(String ak) {
        this.ak = ak;
    }

    public String getSk() {
        return sk;
    }

    public void setSk(String sk) {
        this.sk = sk;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getInstance() {
        return instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }
}
