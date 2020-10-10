package org.springframework.boot.autoconfigure.tablestore.model;

import com.alicloud.openservices.tablestore.model.Error;
import com.alicloud.openservices.tablestore.model.PrimaryKey;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
 * Created on 2020/10/09
 *
 * @author Kenn
 */
public class BatchGetReply<T> {

    private final List<T> records = Lists.newArrayList();

    private final List<Pair<PrimaryKey, Error>> errors = Lists.newArrayList();

    public void add(T record) {
        records.add(record);
    }

    public void addError(Pair<PrimaryKey, Error> error) {
        errors.add(error);
    }

    public List<T> records() {
        return records;
    }

    public List<Pair<PrimaryKey, Error>> errors() {
        return errors;
    }
}
