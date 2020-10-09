package org.springframework.boot.autoconfigure.tablestore.model;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * @project: tablestore-spring-boot-starter
 * @description:
 * @author: Kenn
 * @create: 2019-12-09 14:05
 */
public class IndexSearchReply<T> {

    private long totalCount;

    private boolean allSuccess;

    private List<T> records = Lists.newArrayList();

    public void add(T record) {
        records.add(record);
    }

    public long totalCount() {
        return totalCount;
    }

    public void totalCount(long totalCount) {
        this.totalCount = totalCount;
    }

    public boolean allSuccess() {
        return allSuccess;
    }

    public void allSuccess(boolean allSuccess) {
        this.allSuccess = allSuccess;
    }

    public List<T> records() {
        return records;
    }

    public void records(List<T> records) {
        this.records = records;
    }
}
