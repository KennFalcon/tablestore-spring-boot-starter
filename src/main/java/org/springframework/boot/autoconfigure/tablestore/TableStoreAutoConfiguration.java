package org.springframework.boot.autoconfigure.tablestore;

import com.alicloud.openservices.tablestore.SyncClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @project: tablestore-spring-boot-starter
 * @description:
 * @author: Kenn
 * @create: 2019-02-28 11:50
 */
@Configuration
@ConditionalOnClass(TableStoreService.class)
@EnableConfigurationProperties({TableStoreProperties.class})
public class TableStoreAutoConfiguration {

    @Autowired
    private TableStoreProperties properties;

    @Bean
    @ConditionalOnMissingBean(name = {"tableStoreService"})
    public TableStoreService tableStoreService(SyncClient syncClient) {
        return new TableStoreService(syncClient);
    }

    @Bean
    public SyncClient syncClient() {
        return new SyncClient(properties.getEndpoint(),
            properties.getAk(),
            properties.getSk(),
            properties.getInstance());
    }
}
