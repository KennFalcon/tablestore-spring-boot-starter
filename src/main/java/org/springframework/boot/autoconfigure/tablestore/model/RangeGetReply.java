package org.springframework.boot.autoconfigure.tablestore.model;

import com.alicloud.openservices.tablestore.model.PrimaryKey;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * @project: tablestore-spring-boot-starter
 * @description:
 * @author: Kenn
 * @create: 2019-12-05 15:34
 */
public class RangeGetReply<T> {

    private List<T> records = Lists.newArrayList();

    private PrimaryKey nextStartPrimaryKey;

    public void add(T record) {
        records.add(record);
    }

    public List<T> records() {
        return records;
    }

    public void records(List<T> records) {
        this.records = records;
    }

    public PrimaryKey nextStartPrimaryKey() {
        return nextStartPrimaryKey;
    }

    public void nextStartPrimaryKey(PrimaryKey nextStartPrimaryKey) {
        this.nextStartPrimaryKey = nextStartPrimaryKey;
    }
}
