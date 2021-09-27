package org.springframework.boot.autoconfigure.tablestore;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Created on 2020/10/09
 *
 * @author Kenn
 */
@ConfigurationProperties(prefix = "tablestore")
public class TableStoreProperties {
    /**
     * 阿里云访问AK
     */
    private String accessKeyId;
    /**
     * 阿里云访问SK
     */
    private String accessKeySecret;
    /**
     * 阿里云表格存储服务域名地址
     */
    private String endpoint;
    /**
     * 阿里云表格存储实例名
     */
    private String instance;

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    public String getAccessKeySecret() {
        return accessKeySecret;
    }

    public void setAccessKeySecret(String accessKeySecret) {
        this.accessKeySecret = accessKeySecret;
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
