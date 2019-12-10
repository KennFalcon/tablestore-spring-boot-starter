package org.springframework.boot.autoconfigure.tablestore.utils;

import com.alicloud.openservices.tablestore.model.*;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.tablestore.annotation.OtsColumn;
import org.springframework.boot.autoconfigure.tablestore.exception.OtsException;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
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
     * @param row   行结果
     * @param clazz 结果类类型
     * @param <T>   结果类泛型
     * @return 返回结果类
     */
    public static <T> T build(Row row, Class<T> clazz) {
        if (row == null) {
            return null;
        }
        T data;
        try {
            data = clazz.getConstructor().newInstance();
        } catch (InstantiationException | InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
            throw new OtsException("reflect instance error, class: %s, primary key: %s", e, clazz.getName(), row.getPrimaryKey().toString());
        }
        List<Field> fields = getDeclaredFields(clazz);
        for (Field field : fields) {
            String fieldName = field.getName();
            PropertyDescriptor pd;
            try {
                pd = new PropertyDescriptor(fieldName, clazz);
            } catch (IntrospectionException e) {
                throw new OtsException("reflect error, class: %s, field: %s", e, data.getClass().getName(), fieldName);
            }
            Method method = pd.getWriteMethod();
            OtsColumn otsColumn = field.getAnnotation(OtsColumn.class);
            if (otsColumn != null && !otsColumn.readable()) {
                continue;
            }
            String columnName = ColumnUtils.getColumnName(fieldName, otsColumn);
            Object value = null;
            if (otsColumn != null && otsColumn.primaryKey()) {
                PrimaryKeyColumn primaryKeyColumn = row.getPrimaryKey().getPrimaryKeyColumn(columnName);
                if (primaryKeyColumn != null) {
                    value = ColumnUtils.getValue(primaryKeyColumn, otsColumn, field.getType(), field.getGenericType());

                }
            } else {
                Column column = row.getLatestColumn(columnName);
                if (column == null) {
                    continue;
                }
                value = ColumnUtils.getValue(column, otsColumn, field.getType(), field.getGenericType());
            }
            try {
                method.invoke(data, value);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new OtsException("reflect error, class: %s, field: %s", e, data.getClass().getName(), fieldName);
            }
        }
        return data;
    }

    /**
     * 根据Stream结果构造结果类
     *
     * @param record stream结果
     * @param clazz  结果类类型
     * @param <T>    结果类泛型
     * @return 返回结果类
     */
    public static <T> T build(StreamRecord record, Class<T> clazz) {
        if (record == null) {
            return null;
        }
        T data;
        try {
            data = clazz.getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new OtsException("reflect instance error, class: %s, primary key: %s", e, clazz.getName(), record.getPrimaryKey().toString());
        }
        List<Field> fields = getDeclaredFields(clazz);

        Map<String, Column> columnMap = record.getColumns().stream()
            .map(RecordColumn::getColumn)
            .filter(Objects::nonNull)
            .collect(Collectors.toMap(Column::getName, column -> column));

        for (Field field : fields) {
            String fieldName = field.getName();
            PropertyDescriptor pd;
            try {
                pd = new PropertyDescriptor(fieldName, clazz);
            } catch (IntrospectionException e) {
                throw new OtsException("reflect error, class: %s, field: %s", e, data.getClass().getName(), fieldName);
            }
            Method method = pd.getWriteMethod();
            OtsColumn otsColumn = field.getAnnotation(OtsColumn.class);
            if (otsColumn != null && !otsColumn.readable()) {
                continue;
            }
            String columnName = ColumnUtils.getColumnName(fieldName, otsColumn);
            Object value = null;
            if (otsColumn != null && otsColumn.primaryKey()) {
                PrimaryKeyColumn primaryKeyColumn = record.getPrimaryKey().getPrimaryKeyColumn(columnName);
                if (primaryKeyColumn != null) {
                    value = ColumnUtils.getValue(primaryKeyColumn, otsColumn, field.getType(), field.getGenericType());

                }
            } else {
                if (!columnMap.containsKey(columnName)) {
                    continue;
                }
                Column column = columnMap.get(columnName);
                value = ColumnUtils.getValue(column, otsColumn, field.getType(), field.getGenericType());
            }
            try {
                method.invoke(data, value);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new OtsException("reflect error, class: %s, field: %s", e, data.getClass().getName(), fieldName);
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
