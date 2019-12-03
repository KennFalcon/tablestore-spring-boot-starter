package org.springframework.boot.autoconfigure.tablestore;

import com.alicloud.openservices.tablestore.SyncClient;
import com.alicloud.openservices.tablestore.model.*;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.boot.autoconfigure.tablestore.annotation.FieldMapper;
import org.springframework.boot.autoconfigure.tablestore.annotation.Table;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @project: tablestore-spring-boot-starter
 * @description:
 * @author: Kenn
 * @create: 2019-02-28 10:58
 */
@Slf4j
@AllArgsConstructor
public class TableStoreService {

    private SyncClient client;

    /**
     * 向TableStore中插入数据
     *
     * @param data      数据
     * @param condition 条件
     * @return 返回Put响应
     * @throws Exception 异常
     */
    public PutRowResponse put(Object data, Condition condition) throws Exception {
        Preconditions.checkNotNull(data);
        RowPutChange rowPutChange = rowPutChange(data);
        rowPutChange.setCondition(condition);
        return client.putRow(new PutRowRequest(rowPutChange));
    }

    /**
     * 向TableStore中更新数据
     *
     * @param data       数据
     * @param condition  条件
     * @param deleteNull 是否删除为Null字段
     * @return 返回Update响应
     * @throws Exception 异常
     */
    public UpdateRowResponse update(Object data, Condition condition, boolean deleteNull) throws Exception {
        Preconditions.checkNotNull(data);
        RowUpdateChange rowUpdateChange = rowUpdateChange(data, deleteNull);
        rowUpdateChange.setCondition(condition);
        return client.updateRow(new UpdateRowRequest(rowUpdateChange));
    }

    /**
     * 从TableStore中删除数据
     *
     * @param table     表名
     * @param keyPairs  主键键值对
     * @param condition 条件
     * @return 返回Delete响应
     */
    public DeleteRowResponse delete(String table, List<Pair<String, Object>> keyPairs, Condition condition) {
        Preconditions.checkArgument(StringUtils.isNotEmpty(table));
        Preconditions.checkNotNull(keyPairs);
        PrimaryKey primaryKey = primaryKey(keyPairs);
        RowDeleteChange rowDeleteChange = new RowDeleteChange(table, primaryKey);
        rowDeleteChange.setCondition(condition);
        return client.deleteRow(new DeleteRowRequest(rowDeleteChange));
    }

    /**
     * 从TableStore获取数据
     *
     * @param table    表名
     * @param keyPairs 主键键值对
     * @return 返回Get响应
     */
    public GetRowResponse get(String table, List<Pair<String, Object>> keyPairs) {
        Preconditions.checkArgument(StringUtils.isNotEmpty(table));
        Preconditions.checkNotNull(keyPairs);
        PrimaryKey primaryKey = primaryKey(keyPairs);
        SingleRowQueryCriteria criteria = new SingleRowQueryCriteria(table, primaryKey);
        return client.getRow(new GetRowRequest(criteria));
    }

    /**
     * 构造TableStore插入行
     *
     * @param data 原始数据
     * @return 返回TableStore插入行变更
     * @throws Exception 异常
     */
    private RowPutChange rowPutChange(Object data) throws Exception {
        Table table = data.getClass().getAnnotation(Table.class);
        if (table == null) {
            throw new RuntimeException("the data have no table annotation");
        }

        List<PrimaryKeyColumn> primaryKeyColumns = Lists.newArrayList();
        List<Column> columns = Lists.newArrayList();

        Field[] fields = data.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.getAnnotation(FieldMapper.class) == null) {
                continue;
            }
            String fieldName = field.getName();
            PropertyDescriptor pd = new PropertyDescriptor(fieldName, data.getClass());
            Method method = pd.getReadMethod();
            Object value = method.invoke(data);
            if (value == null) {
                continue;
            }
            FieldMapper fieldMapper = field.getAnnotation(FieldMapper.class);
            String columnName = ColumnUtils.getColumnName(fieldName, fieldMapper.name());
            if (fieldMapper.primaryKey()) {
                PrimaryKeyValue primaryKeyValue = ColumnUtils.getPrimaryKeyValue(value);
                if (primaryKeyValue == null) {
                    continue;
                }
                primaryKeyColumns.add(new PrimaryKeyColumn(columnName, primaryKeyValue));
            } else {
                ColumnValue columnValue = ColumnUtils.getColumnValue(value);
                if (columnValue == null) {
                    continue;
                }
                columns.add(new Column(columnName, columnValue));
            }
        }
        String tableName = table.name();
        RowPutChange rowPutChange = new RowPutChange(tableName, new PrimaryKey(primaryKeyColumns));
        rowPutChange.addColumns(columns);
        return rowPutChange;
    }

    /**
     * 构造TableStore更新行
     *
     * @param data 原始数据
     * @return 返回TableStore更新行变更
     * @throws Exception 异常
     */
    private RowUpdateChange rowUpdateChange(Object data, boolean deleteNull) throws Exception {
        Table table = data.getClass().getAnnotation(Table.class);
        if (table == null) {
            throw new RuntimeException("the data have no table annotation");
        }
        String tableName = table.name();
        RowUpdateChange rowUpdateChange = new RowUpdateChange(tableName);

        List<PrimaryKeyColumn> primaryKeyColumns = Lists.newArrayList();
        List<Column> columns = Lists.newArrayList();

        Field[] fields = data.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.getAnnotation(FieldMapper.class) == null) {
                continue;
            }
            FieldMapper fieldMapper = field.getAnnotation(FieldMapper.class);
            String fieldName = field.getName();
            String columnName = ColumnUtils.getColumnName(fieldName, fieldMapper.name());

            PropertyDescriptor pd = new PropertyDescriptor(fieldName, data.getClass());
            Method method = pd.getReadMethod();
            Object value = method.invoke(data);
            if (value == null) {
                if (deleteNull && !fieldMapper.primaryKey()) {
                    rowUpdateChange.deleteColumns(columnName);
                }
                continue;
            }
            if (fieldMapper.primaryKey()) {
                PrimaryKeyValue primaryKeyValue = ColumnUtils.getPrimaryKeyValue(value);
                if (primaryKeyValue == null) {
                    continue;
                }
                primaryKeyColumns.add(new PrimaryKeyColumn(columnName, primaryKeyValue));
            } else {
                ColumnValue columnValue = ColumnUtils.getColumnValue(value);
                if (columnValue == null) {
                    continue;
                }
                columns.add(new Column(columnName, columnValue));
            }
        }
        rowUpdateChange.setPrimaryKey(new PrimaryKey(primaryKeyColumns));
        rowUpdateChange.put(columns);
        return rowUpdateChange;
    }

    private PrimaryKey primaryKey(List<Pair<String, Object>> keyPairs) {
        List<PrimaryKeyColumn> columns = keyPairs.stream()
            .map(pair -> new PrimaryKeyColumn(pair.getKey(), ColumnUtils.getPrimaryKeyValue(pair.getValue())))
            .collect(Collectors.toList());
        return new PrimaryKey(columns);
    }
}
