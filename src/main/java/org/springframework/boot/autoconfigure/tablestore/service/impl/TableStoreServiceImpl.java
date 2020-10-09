package org.springframework.boot.autoconfigure.tablestore.service.impl;

import com.alicloud.openservices.tablestore.SyncClient;
import com.alicloud.openservices.tablestore.model.BatchGetRowRequest;
import com.alicloud.openservices.tablestore.model.BatchGetRowResponse;
import com.alicloud.openservices.tablestore.model.BatchWriteRowRequest;
import com.alicloud.openservices.tablestore.model.BatchWriteRowResponse;
import com.alicloud.openservices.tablestore.model.Column;
import com.alicloud.openservices.tablestore.model.ColumnValue;
import com.alicloud.openservices.tablestore.model.Condition;
import com.alicloud.openservices.tablestore.model.DeleteRowRequest;
import com.alicloud.openservices.tablestore.model.DeleteRowResponse;
import com.alicloud.openservices.tablestore.model.Direction;
import com.alicloud.openservices.tablestore.model.GetRangeRequest;
import com.alicloud.openservices.tablestore.model.GetRangeResponse;
import com.alicloud.openservices.tablestore.model.GetRowRequest;
import com.alicloud.openservices.tablestore.model.GetRowResponse;
import com.alicloud.openservices.tablestore.model.MultiRowQueryCriteria;
import com.alicloud.openservices.tablestore.model.PrimaryKey;
import com.alicloud.openservices.tablestore.model.PrimaryKeyColumn;
import com.alicloud.openservices.tablestore.model.PrimaryKeyValue;
import com.alicloud.openservices.tablestore.model.PutRowRequest;
import com.alicloud.openservices.tablestore.model.PutRowResponse;
import com.alicloud.openservices.tablestore.model.RangeRowQueryCriteria;
import com.alicloud.openservices.tablestore.model.Row;
import com.alicloud.openservices.tablestore.model.RowDeleteChange;
import com.alicloud.openservices.tablestore.model.RowPutChange;
import com.alicloud.openservices.tablestore.model.RowUpdateChange;
import com.alicloud.openservices.tablestore.model.SingleRowQueryCriteria;
import com.alicloud.openservices.tablestore.model.UpdateRowRequest;
import com.alicloud.openservices.tablestore.model.UpdateRowResponse;
import com.alicloud.openservices.tablestore.model.search.SearchQuery;
import com.alicloud.openservices.tablestore.model.search.SearchRequest;
import com.alicloud.openservices.tablestore.model.search.SearchResponse;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.boot.autoconfigure.tablestore.annotation.OtsColumn;
import org.springframework.boot.autoconfigure.tablestore.annotation.Table;
import org.springframework.boot.autoconfigure.tablestore.exception.OtsException;
import org.springframework.boot.autoconfigure.tablestore.model.BatchGetQuery;
import org.springframework.boot.autoconfigure.tablestore.model.BatchGetReply;
import org.springframework.boot.autoconfigure.tablestore.model.IndexSearchQuery;
import org.springframework.boot.autoconfigure.tablestore.model.IndexSearchReply;
import org.springframework.boot.autoconfigure.tablestore.model.RangeGetQuery;
import org.springframework.boot.autoconfigure.tablestore.model.RangeGetReply;
import org.springframework.boot.autoconfigure.tablestore.model.internal.FieldInfo;
import org.springframework.boot.autoconfigure.tablestore.service.TableStoreService;
import org.springframework.boot.autoconfigure.tablestore.utils.ColumnUtils;
import org.springframework.boot.autoconfigure.tablestore.utils.FieldUtils;
import org.springframework.boot.autoconfigure.tablestore.utils.OtsUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @project: tablestore-spring-boot-starter
 * @description:
 * @author: Kenn
 * @create: 2019-12-05 18:57
 */
public class TableStoreServiceImpl implements TableStoreService {

    private final SyncClient syncClient;

    public TableStoreServiceImpl(SyncClient syncClient) {
        this.syncClient = syncClient;
    }

    @Override
    public <T> PutRowResponse put(T data, Condition condition) {
        Preconditions.checkNotNull(data);
        RowPutChange rowPutChange = rowPutChange(data);
        rowPutChange.setCondition(condition);
        return syncClient.putRow(new PutRowRequest(rowPutChange));
    }

    @Override
    public <T> UpdateRowResponse update(T data, Condition condition, boolean deleteNull) {
        Preconditions.checkNotNull(data);
        RowUpdateChange rowUpdateChange = rowUpdateChange(data, deleteNull);
        rowUpdateChange.setCondition(condition);
        return syncClient.updateRow(new UpdateRowRequest(rowUpdateChange));
    }

    @Override
    public <T> DeleteRowResponse delete(String table, T key, Condition condition) {
        Preconditions.checkArgument(StringUtils.isNotEmpty(table));
        Preconditions.checkNotNull(key);
        PrimaryKey primaryKey = ColumnUtils.primaryKey(key);
        RowDeleteChange rowDeleteChange = new RowDeleteChange(table, primaryKey);
        rowDeleteChange.setCondition(condition);
        return syncClient.deleteRow(new DeleteRowRequest(rowDeleteChange));
    }

    @Override
    public <T, U> T get(U key, List<String> columnNames, Class<T> clazz) {
        Preconditions.checkNotNull(key);
        Table table = clazz.getAnnotation(Table.class);
        if (table == null) {
            throw new OtsException("the table annotation is absent");
        }
        if (StringUtils.isBlank(table.name())) {
            throw new OtsException("the name of table annotation is absent");
        }
        PrimaryKey primaryKey = ColumnUtils.primaryKey(key);
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
    public <T> BatchWriteRowResponse batchPut(List<Pair<T, Condition>> dataPairs) {
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
    public <T> BatchWriteRowResponse batchUpdate(List<Pair<T, Condition>> dataPairs, boolean deleteNull) {
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
    public <T> RangeGetReply<T> rangeGet(RangeGetQuery query, Class<T> clazz) {
        Preconditions.checkNotNull(query);
        Table table = clazz.getAnnotation(Table.class);
        if (table == null) {
            throw new OtsException("the table annotation is absent");
        }
        if (StringUtils.isBlank(table.name())) {
            throw new OtsException("the name of table annotation is absent");
        }
        RangeGetReply<T> reply = new RangeGetReply<>();
        PrimaryKey start = query.startPrimaryKey();
        int batchSize = Math.min(query.limit(), 100);
        while (start != null) {
            GetRangeResponse response = getRange(table.name(), start, query.endPrimaryKey(), query.columnNames(),
                    query.direction(), batchSize);
            if (response == null || response.getRows() == null) {
                reply.nextStartPrimaryKey(null);
                break;
            }
            response.getRows().stream()
                    .map(row -> OtsUtils.build(row, clazz))
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
    public <T> BatchGetReply<T> batchGet(BatchGetQuery query, Class<T> clazz) {
        Preconditions.checkNotNull(query);
        Table table = clazz.getAnnotation(Table.class);
        if (table == null) {
            throw new OtsException("the table annotation is absent");
        }
        if (StringUtils.isBlank(table.name())) {
            throw new OtsException("the name of table annotation is absent");
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
                .map(row -> OtsUtils.build(row, clazz))
                .filter(Objects::nonNull)
                .forEach(reply::add);
        response.getFailedRows().forEach(
                result -> reply.addError(Pair.of(result.getRow().getPrimaryKey(), result.getError())));
        return reply;
    }

    @Override
    public <T> IndexSearchReply<T> search(IndexSearchQuery query, Class<T> clazz) {
        Preconditions.checkNotNull(query);
        Table table = clazz.getAnnotation(Table.class);
        if (table == null) {
            throw new OtsException("the table annotation is absent");
        }
        if (StringUtils.isBlank(table.name())) {
            throw new OtsException("the name of table annotation is absent");
        }
        if (StringUtils.isBlank(table.index())) {
            throw new OtsException("the index of table annotation is absent");
        }
        SearchQuery searchQuery = query.searchQuery();
        SearchRequest.ColumnsToGet columnsToGet = new SearchRequest.ColumnsToGet();
        if (CollectionUtils.isNotEmpty(query.columns())) {
            columnsToGet.setColumns(query.columns());
        } else {
            columnsToGet.setReturnAll(true);
        }
        SearchRequest request = new SearchRequest(table.name(), table.index(), searchQuery);
        request.setColumnsToGet(columnsToGet);
        SearchResponse response = syncClient.search(request);

        IndexSearchReply<T> reply = new IndexSearchReply<>();
        response.getRows().stream()
                .map(row -> OtsUtils.build(row, clazz))
                .filter(Objects::nonNull)
                .forEach(reply::add);
        reply.totalCount(response.getTotalCount());
        reply.allSuccess(response.isAllSuccess());
        return reply;
    }

    /**
     * 构造TableStore插入行
     *
     * @param data 原始数据
     * @param <T>  泛型
     * @return 返回TableStore插入行变更
     */
    @SuppressWarnings(value = "unchecked")
    private <T> RowPutChange rowPutChange(T data) {
        Table table = data.getClass().getAnnotation(Table.class);
        if (table == null) {
            throw new OtsException("the table annotation is absent");
        }
        if (StringUtils.isBlank(table.name())) {
            throw new OtsException("the name of table annotation is absent");
        }
        List<PrimaryKeyColumn> primaryKeyColumns = Lists.newArrayList();
        List<Column> columns = Lists.newArrayList();

        Pair<Map<String, FieldInfo>, Boolean> declaredFieldInfo = FieldUtils.getDeclaredFields(data.getClass());
        declaredFieldInfo.getKey().forEach((columnName, fieldInfo) -> {
            Object value = FieldUtils.invokeRead(fieldInfo.field(), data);
            if (value == null) {
                return;
            }
            if (fieldInfo.otsColumn() != null && !fieldInfo.otsColumn().writable()) {
                return;
            }
            if (fieldInfo.otsColumn() != null && fieldInfo.otsColumn().primaryKey()) {
                setPrimaryColumns(fieldInfo.otsColumn(), columnName, value, primaryKeyColumns);
            } else {
                setColumns(fieldInfo.otsColumn(), columnName, value, columns);
            }
        });
        if (declaredFieldInfo.getValue()) {
            Map<String, Object> values = (Map<String, Object>) FieldUtils.invokeRead("dynamicColumns", data);
            if (MapUtils.isNotEmpty(values)) {
                values.forEach((key, value) -> {
                    if (value != null) {
                        setColumns(null, key, value, columns);
                    }
                });
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
     * @param <T>  泛型
     * @return 返回TableStore更新行变更
     */
    @SuppressWarnings(value = "unchecked")
    private <T> RowUpdateChange rowUpdateChange(T data, boolean deleteNull) {
        Table table = data.getClass().getAnnotation(Table.class);
        if (table == null) {
            throw new OtsException("the table annotation is absent");
        }
        if (StringUtils.isBlank(table.name())) {
            throw new OtsException("the name of table annotation is absent");
        }
        String tableName = table.name();
        RowUpdateChange rowUpdateChange = new RowUpdateChange(tableName);

        List<PrimaryKeyColumn> primaryKeyColumns = Lists.newArrayList();
        List<Column> columns = Lists.newArrayList();

        Pair<Map<String, FieldInfo>, Boolean> declaredFieldInfo = FieldUtils.getDeclaredFields(data.getClass());
        declaredFieldInfo.getKey().forEach((columnName, fieldInfo) -> {
            if (fieldInfo.otsColumn() != null && !fieldInfo.otsColumn().writable()) {
                return;
            }
            Object value = FieldUtils.invokeRead(fieldInfo.field(), data);
            if (value == null) {
                if (deleteNull && fieldInfo.otsColumn() != null && !fieldInfo.otsColumn().primaryKey()) {
                    rowUpdateChange.deleteColumns(columnName);
                }
                return;
            }
            if (fieldInfo.otsColumn() != null && fieldInfo.otsColumn().primaryKey()) {
                setPrimaryColumns(fieldInfo.otsColumn(), columnName, value, primaryKeyColumns);
            } else {
                setColumns(fieldInfo.otsColumn(), columnName, value, columns);
            }
        });

        if (declaredFieldInfo.getValue()) {
            Map<String, Object> values = (Map<String, Object>) FieldUtils.invokeRead("dynamicColumns", data);
            if (MapUtils.isNotEmpty(values)) {
                values.forEach((key, value) -> {
                    if (value == null) {
                        if (deleteNull) {
                            rowUpdateChange.deleteColumns(key);
                        }
                    } else {
                        setColumns(null, key, value, columns);
                    }
                });
            }
        }

        rowUpdateChange.setPrimaryKey(new PrimaryKey(primaryKeyColumns));
        rowUpdateChange.put(columns);
        return rowUpdateChange;
    }

    private void setPrimaryColumns(OtsColumn otsColumn, String columnName, Object value, List<PrimaryKeyColumn> primaryKeyColumns) {
        PrimaryKeyValue primaryKeyValue;
        if (otsColumn.autoIncrease() && value == null) {
            primaryKeyValue = PrimaryKeyValue.AUTO_INCREMENT;
        } else {
            primaryKeyValue = ColumnUtils.getPrimaryKeyValue(value, otsColumn);
        }
        if (primaryKeyValue != null) {
            primaryKeyColumns.add(new PrimaryKeyColumn(columnName, primaryKeyValue));
        } else {
            throw new OtsException("primary key config error, primary column: %s", columnName);
        }
    }

    private void setColumns(OtsColumn otsColumn, String columnName, Object value, List<Column> columns) {
        ColumnValue columnValue = ColumnUtils.getColumnValue(value, otsColumn);
        if (columnValue != null) {
            columns.add(new Column(columnName, columnValue));
        } else {
            throw new OtsException("column config error, column: %s", columnName);
        }
    }

    private GetRangeResponse getRange(String tableName, PrimaryKey start, PrimaryKey end, List<String> columnNames, Direction direction, int limit) {
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
}
