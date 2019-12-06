package org.springframework.boot.autoconfigure.tablestore.model;

import com.alicloud.openservices.tablestore.model.PrimaryKey;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @project: tablestore-spring-boot-starter
 * @description:
 * @author: Kenn
 * @create: 2019-12-05 15:34
 */
@Data
@Accessors(fluent = true)
public class RangeGetReply<T> {

    private List<T> records = Lists.newArrayList();

    private PrimaryKey nextStartPrimaryKey;

    public void add(T record) {
        records.add(record);
    }
}
