package org.springframework.boot.autoconfigure.tablestore.model;

import com.alicloud.openservices.tablestore.model.PrimaryKey;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.boot.autoconfigure.tablestore.utils.ColumnUtils;

import java.util.List;
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

    private List<List<Pair<String, Object>>> keyPairsList;

    private List<PrimaryKey> primaryKeys;

    private List<String> columnNames;

    public List<PrimaryKey> primaryKeys() {
        if (primaryKeys != null) {
            return primaryKeys;
        }
        return keyPairsList.stream().map(ColumnUtils::primaryKey).collect(Collectors.toList());
    }
}
