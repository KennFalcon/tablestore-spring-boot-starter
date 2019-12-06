package org.springframework.boot.autoconfigure.tablestore.utils;

import com.alicloud.openservices.tablestore.model.*;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.tablestore.annotation.FieldMapper;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @project: tablestore-spring-boot-starter
 * @description:
 * @author: Kenn
 * @create: 2019-12-03 16:49
 */
public class OtsUtils {

    /**
     * 根据行结果构造结果类
     *
     * @param row 行结果
     * @return 返回结果类
     * @throws Exception 异常
     */
    public static <T> T build(Row row, Class<T> clazz) throws Exception {
        if (row == null) {
            return null;
        }
        T data = clazz.getConstructor().newInstance();
        List<Field> fields = getDeclaredFields(clazz);
        for (Field field : fields) {
            String fieldName = field.getName();
            PropertyDescriptor pd = new PropertyDescriptor(fieldName, clazz);
            Method method = pd.getWriteMethod();
            FieldMapper fieldMapper = field.getAnnotation(FieldMapper.class);
            String columnName = ColumnUtils.getColumnName(fieldName, fieldMapper);
            if (fieldMapper != null && fieldMapper.primaryKey()) {
                PrimaryKeyColumn primaryKeyColumn = row.getPrimaryKey().getPrimaryKeyColumn(columnName);
                if (primaryKeyColumn != null) {
                    Object value = ColumnUtils.getValue(primaryKeyColumn, fieldMapper);
                    method.invoke(data, value);
                }
            } else {
                Column column = row.getLatestColumn(columnName);
                if (column == null) {
                    continue;
                }
                Object value;
                if (fieldMapper == null) {
                    value = ColumnUtils.getValue(column, field);
                } else {
                    value = ColumnUtils.getValue(column, fieldMapper);
                }
                method.invoke(data, value);
            }
        }
        return data;
    }

    /**
     * 根据Stream结果构造结果类
     *
     * @param record stream结果
     * @return 返回结果类
     * @throws Exception 异常
     */
    public static <T> T build(StreamRecord record, Class<T> clazz) throws Exception {
        if (record == null) {
            return null;
        }
        T data = clazz.getConstructor().newInstance();
        List<Field> fields = getDeclaredFields(clazz);

        Map<String, Column> columnMap = record.getColumns().stream()
            .map(RecordColumn::getColumn)
            .filter(Objects::nonNull)
            .collect(Collectors.toMap(Column::getName, column -> column));

        for (Field field : fields) {
            String fieldName = field.getName();
            PropertyDescriptor pd = new PropertyDescriptor(fieldName, clazz);
            Method method = pd.getWriteMethod();
            FieldMapper fieldMapper = field.getAnnotation(FieldMapper.class);
            String columnName = ColumnUtils.getColumnName(fieldName, fieldMapper);
            if (fieldMapper != null && fieldMapper.primaryKey()) {
                PrimaryKeyColumn primaryKeyColumn = record.getPrimaryKey().getPrimaryKeyColumn(columnName);
                if (primaryKeyColumn != null) {
                    Object value = ColumnUtils.getValue(primaryKeyColumn, fieldMapper);
                    method.invoke(data, value);
                }
            } else {
                if (!columnMap.containsKey(columnName)) {
                    continue;
                }
                Column column = columnMap.get(columnName);
                Object value;
                if (fieldMapper == null) {
                    value = ColumnUtils.getValue(column, field);
                } else {
                    value = ColumnUtils.getValue(column, fieldMapper);
                }
                method.invoke(data, value);
            }
        }
        return data;
    }

    public static boolean isErrorRetryable(String errorCode) {
        return StringUtils.equals(errorCode, "OTSInternalServerError") ||
            StringUtils.equals(errorCode, "OTSQuotaExhausted") ||
            StringUtils.equals(errorCode, "OTSServerBusy") ||
            StringUtils.equals(errorCode, "OTSPartitionUnavailable") ||
            StringUtils.equals(errorCode, "OTSTimeout") ||
            StringUtils.equals(errorCode, "OTSServerUnavailable") ||
            StringUtils.equals(errorCode, "OTSRowOperationConflict") ||
            StringUtils.equals(errorCode, "OTSTableNotReady") ||
            StringUtils.equals(errorCode, "OTSCapacityUnitExhausted");
    }

    private static List<Field> getDeclaredFields(Class<?> clazz) {
        List<Field> fields = Lists.newArrayList(clazz.getDeclaredFields());
        Class<?> superClass = clazz.getSuperclass();
        if (!Object.class.isAssignableFrom(superClass)) {
            fields.addAll(getDeclaredFields(superClass));
        }
        return fields;
    }
}
