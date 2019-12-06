package org.springframework.boot.autoconfigure.tablestore.model;

import com.alicloud.openservices.tablestore.model.Direction;
import com.alicloud.openservices.tablestore.model.PrimaryKey;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.boot.autoconfigure.tablestore.utils.ColumnUtils;

import java.util.List;

/**
 * @project: tablestore-spring-boot-starter
 * @description:
 * @author: Kenn
 * @create: 2019-12-05 15:31
 */
@Data
@Accessors(fluent = true)
public class RangeGetQuery {

    private List<Pair<String, Object>> startKeyPairs;

    private List<Pair<String, Object>> endKeyPairs;

    private PrimaryKey startPrimaryKey;

    private PrimaryKey endPrimaryKey;

    private List<String> columnNames;

    private int limit = 100;

    private Direction direction = Direction.FORWARD;

    public PrimaryKey startPrimaryKey() {
        if (startPrimaryKey != null) {
            return startPrimaryKey;
        }
        return ColumnUtils.primaryKey(startKeyPairs, KeyType.START, direction);
    }

    public PrimaryKey endPrimaryKey() {
        if (endPrimaryKey != null) {
            return endPrimaryKey;
        }
        return ColumnUtils.primaryKey(endKeyPairs, KeyType.END, direction);
    }

    public enum KeyType {
        /**
         * 起始主键
         */
        START,
        /**
         * 终止主键
         */
        END
    }
}
