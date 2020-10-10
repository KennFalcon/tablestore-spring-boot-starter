package org.springframework.boot.autoconfigure.tablestore.model;

import com.alicloud.openservices.tablestore.model.PrimaryKey;

import java.util.List;

/**
 * Created on 2020/10/09
 *
 * @author Kenn
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
