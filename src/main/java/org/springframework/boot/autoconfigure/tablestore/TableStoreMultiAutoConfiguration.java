package org.springframework.boot.autoconfigure.tablestore;

import com.alicloud.openservices.tablestore.SyncClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.tablestore.service.TableStoreService;
import org.springframework.context.annotation.Configuration;

/**
 * @project: tablestore-spring-boot-starter
 * @description:
 * @author: Kenn
 * @create: 2019-12-05 19:41
 */
@Configuration
@ConditionalOnClass({TableStoreService.class, SyncClient.class})
public class TableStoreMultiAutoConfiguration {


}
