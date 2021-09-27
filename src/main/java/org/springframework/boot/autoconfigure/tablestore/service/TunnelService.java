package org.springframework.boot.autoconfigure.tablestore.service;

import com.alicloud.openservices.tablestore.model.tunnel.CreateTunnelResponse;
import com.alicloud.openservices.tablestore.model.tunnel.DeleteTunnelResponse;
import com.alicloud.openservices.tablestore.model.tunnel.DescribeTunnelResponse;
import com.alicloud.openservices.tablestore.model.tunnel.ListTunnelResponse;
import com.alicloud.openservices.tablestore.model.tunnel.TunnelType;

/**
 * Project: tablestore-spring-boot-starter
 * Description:
 * Author: Kenn
 * Create: 2021/5/8 15:58
 */
public interface TunnelService {
    /**
     * 创建通道
     *
     * @param tableName  表名
     * @param tunnelName 通道名
     * @param tunnelType 通道类型
     * @return 返回CreateTunnel响应
     */
    CreateTunnelResponse createTunnel(String tableName, String tunnelName, TunnelType tunnelType);

    /**
     * 创建通道，并指定读取的增量数据时间范围（仅支持增量或者全量加增量类型）
     *
     * @param tableName  表名
     * @param tunnelName 通道名
     * @param tunnelType 通道类型
     * @param startTime  起始时间戳（毫秒）
     * @param endTime    结束时间戳（毫秒）
     * @return 返回CreateTunnel响应
     */
    CreateTunnelResponse createTunnel(String tableName, String tunnelName, TunnelType tunnelType, long startTime, long endTime);

    /**
     * 列举某张表中的通道具体信息
     *
     * @param tableName 表名
     * @return 返回ListTunnel响应
     */
    ListTunnelResponse listTunnel(String tableName);

    /**
     * 描述某个通道里的具体Channel信息。目前一个Channel对应TableStore Stream接口的一个数据分片
     *
     * @param tableName  表名
     * @param tunnelName 通道名
     * @return 返回DescribeTunnel响应
     */
    DescribeTunnelResponse describeTunnel(String tableName, String tunnelName);

    /**
     * 删除某张表的通道
     *
     * @param tableName  表名
     * @param tunnelName 通道名
     * @return 返回DeleteTunnel响应
     */
    DeleteTunnelResponse deleteTunnel(String tableName, String tunnelName);
}
