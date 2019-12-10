package org.springframework.boot.autoconfigure.tablestore.model;

import com.alicloud.openservices.tablestore.model.Direction;
import com.alicloud.openservices.tablestore.model.PrimaryKey;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.autoconfigure.tablestore.utils.ColumnUtils;

import java.util.List;

/**
 * @project: tablestore-spring-boot-starter
 * @description:
 * @author: Kenn
 * @create: 2019-12-05 15:31
 */
@Getter
@Setter
@Accessors(fluent = true)
public class RangeGetQuery {

    private PrimaryKey startPrimaryKey;

    private PrimaryKey endPrimaryKey;

    private List<String> columnNames;

    private int limit = 100;

    private Direction direction = Direction.FORWARD;

    public PrimaryKey startPrimaryKey() {
        return startPrimaryKey;
    }

    public <T> void startPrimaryKey(T key) {
        startPrimaryKey = ColumnUtils.primaryKey(key, KeyType.START, direction);
    }

    public PrimaryKey endPrimaryKey() {
        return endPrimaryKey;
    }

    public <T> void endPrimaryKey(T key) {
        endPrimaryKey = ColumnUtils.primaryKey(key, KeyType.END, direction);
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
