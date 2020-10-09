package org.springframework.boot.autoconfigure.tablestore.tunnel;

import com.alicloud.openservices.tablestore.model.StreamRecord;
import com.alicloud.openservices.tablestore.tunnel.worker.IChannelProcessor;
import com.alicloud.openservices.tablestore.tunnel.worker.ProcessRecordsInput;
import org.springframework.boot.autoconfigure.tablestore.utils.OtsUtils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @project: tablestore-spring-boot-starter
 * @description:
 * @author: Kenn
 * @create: 2019-12-12 19:26
 */
public abstract class BaseTunnelProcessor<T> implements IChannelProcessor {

    @Override
    public void process(ProcessRecordsInput input) {
        input.getRecords().forEach(this::process);
    }

    private void process(StreamRecord streamRecord) {
        process(OtsUtils.build(streamRecord, getActualTypeArgument()));
    }

    /**
     * 单条数据处理
     *
     * @param data 数据
     */
    abstract void process(T data);

    @SuppressWarnings(value = "unchecked")
    private Class<T> getActualTypeArgument() {
        Type genericSuperclass = getClass().getGenericSuperclass();
        if (genericSuperclass instanceof ParameterizedType) {
            Type[] actualTypeArguments = ((ParameterizedType)genericSuperclass).getActualTypeArguments();
            if (actualTypeArguments != null && actualTypeArguments.length > 0) {
                return (Class<T>)actualTypeArguments[0];
            }
        }
        return null;
    }
}
