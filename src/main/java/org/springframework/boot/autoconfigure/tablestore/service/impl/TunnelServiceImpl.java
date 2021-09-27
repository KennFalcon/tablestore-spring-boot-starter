package org.springframework.boot.autoconfigure.tablestore.service.impl;

import com.alicloud.openservices.tablestore.TunnelClient;
import com.alicloud.openservices.tablestore.model.tunnel.CreateTunnelRequest;
import com.alicloud.openservices.tablestore.model.tunnel.CreateTunnelResponse;
import com.alicloud.openservices.tablestore.model.tunnel.DeleteTunnelRequest;
import com.alicloud.openservices.tablestore.model.tunnel.DeleteTunnelResponse;
import com.alicloud.openservices.tablestore.model.tunnel.DescribeTunnelRequest;
import com.alicloud.openservices.tablestore.model.tunnel.DescribeTunnelResponse;
import com.alicloud.openservices.tablestore.model.tunnel.ListTunnelRequest;
import com.alicloud.openservices.tablestore.model.tunnel.ListTunnelResponse;
import com.alicloud.openservices.tablestore.model.tunnel.StreamTunnelConfig;
import com.alicloud.openservices.tablestore.model.tunnel.TunnelType;
import org.springframework.boot.autoconfigure.tablestore.service.TunnelService;

/**
 * Project: tablestore-spring-boot-starter
 * Description:
 * Author: Kenn
 * Create: 2021/5/8 16:01
 */
public class TunnelServiceImpl implements TunnelService {

    private final TunnelClient tunnelClient;

    public TunnelServiceImpl(TunnelClient tunnelClient) {
        this.tunnelClient = tunnelClient;
    }

    @Override
    public CreateTunnelResponse createTunnel(String tableName, String tunnelName, TunnelType tunnelType) {
        CreateTunnelRequest request = new CreateTunnelRequest(tableName, tunnelName, tunnelType);
        return tunnelClient.createTunnel(request);
    }

    @Override
    public CreateTunnelResponse createTunnel(String tableName, String tunnelName, TunnelType tunnelType, long startTime, long endTime) {
        CreateTunnelRequest request = new CreateTunnelRequest(tableName, tunnelName, tunnelType);
        StreamTunnelConfig streamTunnelConfig = new StreamTunnelConfig();
        streamTunnelConfig.setStartOffset(startTime);
        streamTunnelConfig.setEndOffset(endTime);
        request.setStreamTunnelConfig(streamTunnelConfig);
        return tunnelClient.createTunnel(request);
    }

    @Override
    public ListTunnelResponse listTunnel(String tableName) {
        ListTunnelRequest request = new ListTunnelRequest(tableName);
        return tunnelClient.listTunnel(request);
    }

    @Override
    public DescribeTunnelResponse describeTunnel(String tableName, String tunnelName) {
        DescribeTunnelRequest request = new DescribeTunnelRequest(tableName, tunnelName);
        return tunnelClient.describeTunnel(request);
    }

    @Override
    public DeleteTunnelResponse deleteTunnel(String tableName, String tunnelName) {
        DeleteTunnelRequest request = new DeleteTunnelRequest(tableName, tunnelName);
        return tunnelClient.deleteTunnel(request);
    }
}
