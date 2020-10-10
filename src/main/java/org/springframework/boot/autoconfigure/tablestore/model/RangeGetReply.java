package org.springframework.boot.autoconfigure.tablestore.model;

import com.alicloud.openservices.tablestore.model.PrimaryKey;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * Created on 2020/10/09
 *
 * @author Kenn
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
