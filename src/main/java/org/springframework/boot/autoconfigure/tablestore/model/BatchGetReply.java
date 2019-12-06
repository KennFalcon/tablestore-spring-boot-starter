package org.springframework.boot.autoconfigure.tablestore.model;

import com.alicloud.openservices.tablestore.model.Error;
import com.alicloud.openservices.tablestore.model.PrimaryKey;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
 * @project: tablestore-spring-boot-starter
 * @description:
 * @author: Kenn
 * @create: 2019-12-05 17:39
 */
@Data
@Accessors(fluent = true)
public class BatchGetReply<T> {

    private List<T> records = Lists.newArrayList();

    private List<Pair<PrimaryKey, Error>> errors = Lists.newArrayList();

    public void add(T record) {
        records.add(record);
    }

    public void addError(Pair<PrimaryKey, Error> error) {
        errors.add(error);
    }
}
