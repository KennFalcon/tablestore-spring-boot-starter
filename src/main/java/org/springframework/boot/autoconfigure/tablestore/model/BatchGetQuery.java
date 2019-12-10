package org.springframework.boot.autoconfigure.tablestore.model;

import com.alicloud.openservices.tablestore.model.PrimaryKey;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.autoconfigure.tablestore.utils.ColumnUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @project: tablestore-spring-boot-starter
 * @description:
 * @author: Kenn
 * @create: 2019-12-05 17:39
 */
@Getter
@Setter
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
