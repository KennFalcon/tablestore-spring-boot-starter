package org.springframework.boot.autoconfigure.tablestore.utils;

import com.alicloud.openservices.tablestore.model.*;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.boot.autoconfigure.tablestore.exception.OtsException;
import org.springframework.boot.autoconfigure.tablestore.model.internal.FieldInfo;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

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
        Pair<Map<String, FieldInfo>, Boolean> fieldInfos = FieldUtils.getDeclaredFields(data.getClass());
        for (PrimaryKeyColumn primaryKeyColumn : row.getPrimaryKey().getPrimaryKeyColumns()) {
            fill(data, primaryKeyColumn, fieldInfos);
        }
        Method addDynamicMethod = fieldInfos.getValue() ? FieldUtils.getMethod(clazz, "addDynamicColumn", String.class, ColumnType.class, Object.class) : null;
        for (Column column : row.getColumns()) {
            fill(data, column, fieldInfos, addDynamicMethod);
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
        Pair<Map<String, FieldInfo>, Boolean> fieldInfos = FieldUtils.getDeclaredFields(data.getClass());
        for (PrimaryKeyColumn primaryKeyColumn : record.getPrimaryKey().getPrimaryKeyColumns()) {
            fill(data, primaryKeyColumn, fieldInfos);
        }
        Method addDynamicMethod = fieldInfos.getValue() ? FieldUtils.getMethod(clazz, "addDynamicColumn", String.class, ColumnType.class, Object.class) : null;
        for (RecordColumn recordColumn : record.getColumns()) {
            Column column = recordColumn.getColumn();
            fill(data, column, fieldInfos, addDynamicMethod);
        }
        return data;
    }

    private static <T> void fill(T data, PrimaryKeyColumn column, Pair<Map<String, FieldInfo>, Boolean> fieldInfos) {
        if (fieldInfos.getKey().containsKey(column.getName())) {
            FieldInfo fieldInfo = fieldInfos.getKey().get(column.getName());
            if (fieldInfo.otsColumn() != null && !fieldInfo.otsColumn().readable()) {
                return;
            }
            Object value = ColumnUtils.getValue(column, fieldInfo.otsColumn(), fieldInfo.field().getType(), fieldInfo.field().getGenericType());
            FieldUtils.invokeWrite(fieldInfo.field(), data, value);
        }
    }

    private static <T> void fill(T data, Column column, Pair<Map<String, FieldInfo>, Boolean> fieldInfos, Method method) {
        if (fieldInfos.getKey().containsKey(column.getName())) {
            FieldInfo fieldInfo = fieldInfos.getKey().get(column.getName());
            if (fieldInfo.otsColumn() != null && !fieldInfo.otsColumn().readable()) {
                return;
            }
            Object value = ColumnUtils.getValue(column, fieldInfo.otsColumn(), fieldInfo.field().getType(), fieldInfo.field().getGenericType());
            FieldUtils.invokeWrite(fieldInfo.field(), data, value);
        } else {
            if (fieldInfos.getValue() && method != null) {
                Object value = ColumnUtils.getValue(column);
                FieldUtils.invoke(method, data, column.getName(), column.getValue().getType(), value);
            }
        }
    }

    private <T> void fill(T data, Column column, Pair<Map<String, FieldInfo>, Boolean> fieldInfos) {
        FieldInfo fieldInfo = fieldInfos.getKey().get(column.getName());
        if (fieldInfo.otsColumn() != null && !fieldInfo.otsColumn().readable()) {
            return;
        }
        Object value = ColumnUtils.getValue(column, fieldInfo.otsColumn(), fieldInfo.field().getType(), fieldInfo.field().getGenericType());
        FieldUtils.invokeWrite(fieldInfo.field(), data, value);
    }
}
