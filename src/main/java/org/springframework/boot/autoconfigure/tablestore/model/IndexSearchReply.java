package org.springframework.boot.autoconfigure.tablestore.model;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * Created on 2020/10/09
 *
 * @author Kenn
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
