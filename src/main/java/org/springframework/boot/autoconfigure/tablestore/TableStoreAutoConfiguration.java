package org.springframework.boot.autoconfigure.tablestore;

import com.alicloud.openservices.tablestore.SyncClient;
import com.alicloud.openservices.tablestore.TunnelClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.tablestore.service.TableStoreService;
import org.springframework.boot.autoconfigure.tablestore.service.impl.TableStoreServiceImpl;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * Created on 2020/10/09
 *
 * @author Kenn
 */
@Configuration
@ConditionalOnClass({TableStoreService.class, SyncClient.class, TunnelClient.class})
@EnableConfigurationProperties({TableStoreProperties.class})
public class TableStoreAutoConfiguration {

    @Resource
    private TableStoreProperties properties;

    @Bean
    @ConditionalOnMissingBean(name = {"tableStoreService"})
    public TableStoreService tableStoreService(SyncClient syncClient) {
        return new TableStoreServiceImpl(syncClient);
    }

    @Bean(destroyMethod = "shutdown")
    @ConditionalOnMissingBean(name = {"syncClient"})
    public SyncClient syncClient() {
        return new SyncClient(properties.getEndpoint(),
            properties.getAk(),
            properties.getSk(),
            properties.getInstance());
    }

    @Bean(destroyMethod = "shutdown")
    @ConditionalOnMissingBean(name = {"tunnelClient"})
    public TunnelClient tunnelClient() {
        return new TunnelClient(properties.getEndpoint(),
            properties.getAk(),
            properties.getSk(),
            properties.getInstance());
    }
}
