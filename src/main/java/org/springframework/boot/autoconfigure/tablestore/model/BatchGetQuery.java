package org.springframework.boot.autoconfigure.tablestore.model;

import com.alicloud.openservices.tablestore.model.PrimaryKey;
import com.alicloud.openservices.tablestore.model.PrimaryKeyColumn;
import com.alicloud.openservices.tablestore.model.PrimaryKeyValue;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.boot.autoconfigure.tablestore.annotation.OtsColumn;
import org.springframework.boot.autoconfigure.tablestore.utils.ColumnUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @project: tablestore-spring-boot-starter
 * @description:
 * @author: Kenn
 * @create: 2019-12-05 17:39
 */
@Data
@Accessors(fluent = true)
public class BatchGetQuery {

    private List<PrimaryKey> primaryKeys;

    private List<String> columnNames;

    public <T> void primaryKeys(List<T> keys) {
        this.primaryKeys = keys.stream().map(ColumnUtils::primaryKey).collect(Collectors.toList());
    }

    public List<PrimaryKey> primaryKeys() {
        return primaryKeys;
    }
}
