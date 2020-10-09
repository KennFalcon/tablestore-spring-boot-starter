package org.springframework.boot.autoconfigure.tablestore.model;

import com.alicloud.openservices.tablestore.model.PrimaryKey;
import org.springframework.boot.autoconfigure.tablestore.utils.ColumnUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @project: tablestore-spring-boot-starter
 * @description:
 * @author: Kenn
 * @create: 2019-12-05 17:39
 */
public class BatchGetQuery {

    private List<PrimaryKey> primaryKeys;

    private List<String> columnNames;

    public void primaryKeys(List<PrimaryKey> primaryKeys) {
        this.primaryKeys = primaryKeys;
    }

    public void columnNames(List<String> columnNames) {
        this.columnNames = columnNames;
    }

    public List<PrimaryKey> primaryKeys() {
        return primaryKeys;
    }

    public List<String> columnNames() {
        return columnNames;
    }
}
