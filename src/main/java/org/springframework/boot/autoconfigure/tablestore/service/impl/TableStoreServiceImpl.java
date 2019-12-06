package org.springframework.boot.autoconfigure.tablestore.service.impl;

import com.alicloud.openservices.tablestore.SyncClient;
import com.alicloud.openservices.tablestore.model.*;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.boot.autoconfigure.tablestore.service.TableStoreService;
import org.springframework.boot.autoconfigure.tablestore.annotation.FieldMapper;
import org.springframework.boot.autoconfigure.tablestore.annotation.Table;
import org.springframework.boot.autoconfigure.tablestore.model.BatchGetQuery;
import org.springframework.boot.autoconfigure.tablestore.model.BatchGetReply;
import org.springframework.boot.autoconfigure.tablestore.model.RangeGetQuery;
import org.springframework.boot.autoconfigure.tablestore.model.RangeGetReply;
import org.springframework.boot.autoconfigure.tablestore.utils.ColumnUtils;
import org.springframework.boot.autoconfigure.tablestore.utils.OtsUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Objects;

/**
 * @project: tablestore-spring-boot-starter
 * @description:
 * @author: Kenn
 * @create: 2019-12-05 18:57
 */
@AllArgsConstructor
public class TableStoreServiceImpl implements TableStoreService {

    private SyncClient syncClient;

    @Override
    public <T> PutRowResponse put(T data, Condition condition) throws Exception {
        Preconditions.checkNotNull(data);
        RowPutChange rowPutChange = rowPutChange(data);
        rowPutChange.setCondition(condition);
        return syncClient.putRow(new PutRowRequest(rowPutChange));
    }

    @Override
    public <T> UpdateRowResponse update(T data, Condition condition, boolean deleteNull) throws Exception {
        Preconditions.checkNotNull(data);
        RowUpdateChange rowUpdateChange = rowUpdateChange(data, deleteNull);
        rowUpdateChange.setCondition(condition);
        return syncClient.updateRow(new UpdateRowRequest(rowUpdateChange));
    }

    @Override
    public DeleteRowResponse delete(String table, List<Pair<String, Object>> keyPairs, Condition condition) {
        Preconditions.checkArgument(StringUtils.isNotEmpty(table));
        Preconditions.checkNotNull(keyPairs);
        PrimaryKey primaryKey = ColumnUtils.primaryKey(keyPairs);
        RowDeleteChange rowDeleteChange = new RowDeleteChange(table, primaryKey);
        rowDeleteChange.setCondition(condition);
        return syncClient.deleteRow(new DeleteRowRequest(rowDeleteChange));
    }

    @Override
    public <T> T get(List<Pair<String, Object>> keyPairs, List<String> columnNames) throws Exception {
        Preconditions.checkNotNull(keyPairs);
        Class<T> clazz = getGenericsClass();
        Table table = clazz.getAnnotation(Table.class);
        if (table == null) {
            throw new RuntimeException("the data have no table annotation");
        }
        PrimaryKey primaryKey = ColumnUtils.primaryKey(keyPairs);
        SingleRowQueryCriteria criteria = new SingleRowQueryCriteria(table.name(), primaryKey);
        criteria.setMaxVersions(1);
        if (CollectionUtils.isNotEmpty(columnNames)) {
            criteria.addColumnsToGet(columnNames);
        }
        GetRowResponse response = syncClient.getRow(new GetRowRequest(criteria));
        Row row = response.getRow();
        if (row == null) {
            return null;
        }

        return OtsUtils.build(row, clazz);
    }

    @Override
    public <T> BatchWriteRowResponse batchPut(List<Pair<T, Condition>> dataPairs) throws Exception {
        Preconditions.checkNotNull(dataPairs);
        BatchWriteRowRequest request = new BatchWriteRowRequest();
        for (Pair<T, Condition> dataPair : dataPairs) {
            RowPutChange rowPutChange = rowPutChange(dataPair.getKey());
            rowPutChange.setCondition(dataPair.getValue());
            request.addRowChange(rowPutChange);
        }
        return syncClient.batchWriteRow(request);
    }

    @Override
    public <T> BatchWriteRowResponse batchUpdate(List<Pair<T, Condition>> dataPairs, boolean deleteNull) throws Exception {
        Preconditions.checkNotNull(dataPairs);
        BatchWriteRowRequest request = new BatchWriteRowRequest();
        for (Pair<T, Condition> dataPair : dataPairs) {
            RowUpdateChange rowUpdateChange = rowUpdateChange(dataPair.getKey(), deleteNull);
            rowUpdateChange.setCondition(dataPair.getValue());
            request.addRowChange(rowUpdateChange);
        }
        return syncClient.batchWriteRow(request);
    }

    @Override
    public <T> RangeGetReply<T> rangeGet(RangeGetQuery query) {
        Preconditions.checkNotNull(query);
        Class<T> clazz = getGenericsClass();
        Table table = clazz.getAnnotation(Table.class);
        if (table == null) {
            throw new RuntimeException("the data have no table annotation");
        }
        RangeGetReply<T> reply = new RangeGetReply<>();
        PrimaryKey start = query.startPrimaryKey();
        int batchSize = Math.min(query.limit(), 100);
        while (start != null) {
            GetRangeResponse response = this.doGetRange(table.name(), start, query.endPrimaryKey(), query.columnNames(), query.direction(), batchSize);
            if (response == null || response.getRows() == null) {
                reply.nextStartPrimaryKey(null);
                break;
            }
            response.getRows().stream()
                .map(row -> {
                    try {
                        return OtsUtils.build(row, clazz);
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .forEach(reply::add);
            start = response.getNextStartPrimaryKey();
            if (query.limit() > 0) {
                batchSize = Math.min(query.limit() - reply.records().size(), 500);
                if (batchSize <= 0) {
                    response.setNextStartPrimaryKey(start);
                    break;
                }
            }
        }
        return reply;
    }

    @Override
    public <T> BatchGetReply<T> batchGet(BatchGetQuery query) {
        Preconditions.checkNotNull(query);
        Class<T> clazz = getGenericsClass();
        Table table = clazz.getAnnotation(Table.class);
        if (table == null) {
            throw new RuntimeException("the data have no table annotation");
        }

        MultiRowQueryCriteria criteria = new MultiRowQueryCriteria(table.name());
        criteria.setRowKeys(query.primaryKeys());
        criteria.setMaxVersions(1);
        if (CollectionUtils.isNotEmpty(query.columnNames())) {
            criteria.addColumnsToGet(query.columnNames());
        }
        BatchGetRowRequest request = new BatchGetRowRequest();
        request.addMultiRowQueryCriteria(criteria);
        BatchGetRowResponse response = syncClient.batchGetRow(request);
        if (response == null) {
            return null;
        }
        BatchGetReply<T> reply = new BatchGetReply<>();
        response.getSucceedRows().stream()
            .map(BatchGetRowResponse.RowResult::getRow)
            .filter(Objects::nonNull)
            .map(row -> {
                try {
                    return OtsUtils.build(row, clazz);
                } catch (Exception e) {
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .forEach(reply::add);
        response.getFailedRows().forEach(
            result -> reply.addError(Pair.of(result.getRow().getPrimaryKey(), result.getError())));
        return reply;
    }

    /**
     * 构造TableStore插入行
     *
     * @param data 原始数据
     * @param <T>  泛型
     * @return 返回TableStore插入行变更
     * @throws Exception 异常
     */
    private <T> RowPutChange rowPutChange(T data) throws Exception {
        Table table = data.getClass().getAnnotation(Table.class);
        if (table == null) {
            throw new RuntimeException("the data have no table annotation");
        }

        List<PrimaryKeyColumn> primaryKeyColumns = Lists.newArrayList();
        List<Column> columns = Lists.newArrayList();

        Field[] fields = data.getClass().getDeclaredFields();
        for (Field field : fields) {
            String fieldName = field.getName();
            PropertyDescriptor pd = new PropertyDescriptor(fieldName, data.getClass());
            Method method = pd.getReadMethod();
            Object value = method.invoke(data);
            FieldMapper fieldMapper = field.getAnnotation(FieldMapper.class);
            String columnName = ColumnUtils.getColumnName(fieldName, fieldMapper);
            setColumns(primaryKeyColumns, columns, fieldMapper, columnName, value);
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
     * @param <T>  泛型
     * @return 返回TableStore更新行变更
     * @throws Exception 异常
     */
    private <T> RowUpdateChange rowUpdateChange(T data, boolean deleteNull) throws Exception {
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
            String columnName = ColumnUtils.getColumnName(fieldName, fieldMapper);
            PropertyDescriptor pd = new PropertyDescriptor(fieldName, data.getClass());
            Method method = pd.getReadMethod();
            Object value = method.invoke(data);
            if (value == null) {
                if (deleteNull && fieldMapper != null && !fieldMapper.primaryKey()) {
                    rowUpdateChange.deleteColumns(columnName);
                }
                continue;
            }
            setColumns(primaryKeyColumns, columns, fieldMapper, columnName, value);
        }
        rowUpdateChange.setPrimaryKey(new PrimaryKey(primaryKeyColumns));
        rowUpdateChange.put(columns);
        return rowUpdateChange;
    }

    private void setColumns(List<PrimaryKeyColumn> primaryKeyColumns, List<Column> columns, FieldMapper fieldMapper, String columnName, Object value) {
        if (fieldMapper != null && fieldMapper.primaryKey()) {
            PrimaryKeyValue primaryKeyValue;
            if (fieldMapper.autoIncrease() && value == null) {
                primaryKeyValue = PrimaryKeyValue.AUTO_INCREMENT;
            } else {
                primaryKeyValue = ColumnUtils.getPrimaryKeyValue(value);
            }
            if (primaryKeyValue != null) {
                primaryKeyColumns.add(new PrimaryKeyColumn(columnName, primaryKeyValue));
            }
        } else {
            if (value == null) {
                return;
            }
            ColumnValue columnValue = ColumnUtils.getColumnValue(value);
            if (columnValue == null) {
                return;
            }
            columns.add(new Column(columnName, columnValue));
        }
    }

    private GetRangeResponse doGetRange(String tableName, PrimaryKey start, PrimaryKey end, List<String> columnNames, Direction direction, int limit) {
        GetRangeRequest getRangeRequest = new GetRangeRequest();
        RangeRowQueryCriteria criteria = new RangeRowQueryCriteria(tableName);
        criteria.setInclusiveStartPrimaryKey(start);
        criteria.setExclusiveEndPrimaryKey(end);
        criteria.setMaxVersions(1);
        criteria.setDirection(direction);
        if (CollectionUtils.isNotEmpty(columnNames)) {
            criteria.addColumnsToGet(columnNames);
        }
        if (limit > 0) {
            criteria.setLimit(limit);
        }
        getRangeRequest.setRangeRowQueryCriteria(criteria);
        return syncClient.getRange(getRangeRequest);
    }

    /**
     * 获取泛型对象Class
     *
     * @param <T> 泛型
     * @return 泛型对象Class
     */
    @SuppressWarnings(value = "unchecked")
    private <T> Class<T> getGenericsClass() {
        return (Class<T>)((ParameterizedType)getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }
}
