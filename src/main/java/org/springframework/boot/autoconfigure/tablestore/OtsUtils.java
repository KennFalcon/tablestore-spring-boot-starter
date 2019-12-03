package org.springframework.boot.autoconfigure.tablestore;

import com.alicloud.openservices.tablestore.model.*;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.tablestore.annotation.FieldMapper;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

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
            if (field.getAnnotation(FieldMapper.class) != null) {
                String fieldName = field.getName();
                PropertyDescriptor pd = new PropertyDescriptor(fieldName, clazz);
                Method method = pd.getWriteMethod();
                FieldMapper fieldMapper = field.getAnnotation(FieldMapper.class);
                String columnName = ColumnUtils.getColumnName(fieldName, fieldMapper.name());
                if (fieldMapper.primaryKey()) {
                    PrimaryKeyColumn primaryKeyColumn = row.getPrimaryKey().getPrimaryKeyColumn(columnName);
                    if (primaryKeyColumn != null) {
                        Object value = ColumnUtils.getValue(primaryKeyColumn, fieldMapper);
                        method.invoke(data, value);
                    }
                } else {
                    Column column = row.getLatestColumn(columnName);
                    if (column != null) {
                        Object value = ColumnUtils.getValue(column, fieldMapper);
                        method.invoke(data, value);
                    }
                }
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
        for (RecordColumn recordColumn : record.getColumns()) {
            Column column = recordColumn.getColumn();
            if (column == null) {
                continue;
            }
            String columnName = column.getName();
            for (Field field : fields) {
                if (field.getAnnotation(FieldMapper.class) == null) {
                    continue;
                }
                FieldMapper fieldMapper = field.getAnnotation(FieldMapper.class);
                if (fieldMapper.primaryKey()) {
                    continue;
                }
                if (fieldMapper.name().equals(columnName)) {
                    String fieldName = field.getName();
                    PropertyDescriptor pd = new PropertyDescriptor(fieldName, clazz);
                    Method method = pd.getWriteMethod();
                    Object value = ColumnUtils.getValue(column, fieldMapper);
                    method.invoke(data, value);
                }
            }
        }
        for (Field field : fields) {
            FieldMapper fieldMapper = field.getAnnotation(FieldMapper.class);
            String fieldName = field.getName();
            String columnName = ColumnUtils.getColumnName(fieldName, fieldMapper.name());
            if (!fieldMapper.primaryKey()) {
                continue;
            }
            PrimaryKeyColumn primaryKeyColumn = record.getPrimaryKey().getPrimaryKeyColumn(columnName);
            if (primaryKeyColumn == null) {
                continue;
            }
            PropertyDescriptor pd = new PropertyDescriptor(fieldName, clazz);
            Method method = pd.getWriteMethod();
            Object value = ColumnUtils.getValue(primaryKeyColumn, fieldMapper);
            method.invoke(data, value);
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
