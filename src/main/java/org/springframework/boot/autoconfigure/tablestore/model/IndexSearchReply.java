package org.springframework.boot.autoconfigure.tablestore.model;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @project: tablestore-spring-boot-starter
 * @description:
 * @author: Kenn
 * @create: 2019-12-09 14:05
 */
@Getter
@Setter
@Accessors(fluent = true)
public class IndexSearchReply<T> {

    private long totalCount;

    private boolean allSuccess;

    private List<T> records = Lists.newArrayList();

    public void add(T record) {
        records.add(record);
    }
}
