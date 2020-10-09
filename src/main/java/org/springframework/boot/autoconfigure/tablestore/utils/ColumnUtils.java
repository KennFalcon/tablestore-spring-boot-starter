package org.springframework.boot.autoconfigure.tablestore.utils;

import com.alibaba.fastjson.JSON;
import com.alicloud.openservices.tablestore.model.*;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.tablestore.annotation.OtsColumn;
import org.springframework.boot.autoconfigure.tablestore.model.RangeGetQuery.KeyType;
import org.springframework.boot.autoconfigure.tablestore.utils.compress.NoCompress;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * @project: tablestore-spring-boot-starter
 * @description: OTS字段工具类
 * @author: Kenn
 * @create: 2018-11-24 14:27
 */
public class ColumnUtils {

    public static String getColumnName(String fieldName, OtsColumn otsColumn) {
        if (otsColumn == null || StringUtils.isBlank(otsColumn.name())) {
            return fieldName;
        }
        return otsColumn.name();
    }

    public static ColumnValue getColumnValue(Object value, OtsColumn otsColumn) {
        if (value == null) {
            return null;
        }
        if (value instanceof byte[]) {
            return columnValue((byte[])value, otsColumn);
        } else if (value instanceof Short) {
            return columnValue((Short)value, otsColumn);
        } else if (value instanceof Integer) {
            return columnValue((Integer)value, otsColumn);
        } else if (value instanceof Long) {
            return columnValue((Long)value, otsColumn);
        } else if (value instanceof Float) {
            return columnValue((Float)value, otsColumn);
        } else if (value instanceof Double) {
            return columnValue((Double)value, otsColumn);
        } else if (value instanceof Boolean) {
            return columnValue((Boolean)value, otsColumn);
        } else if (value instanceof String) {
            return columnValue((String)value, otsColumn);
        } else {
            return columnValue(value, otsColumn);
        }
    }

    public static PrimaryKeyValue getPrimaryKeyValue(Object value, OtsColumn otsColumn) {
        if (value == null) {
            return null;
        }
        if (value instanceof byte[]) {
            return primaryKeyValue((byte[])value, otsColumn);
        } else if (value instanceof Short) {
            return primaryKeyValue((Short)value, otsColumn);
        } else if (value instanceof Integer) {
            return primaryKeyValue((Integer)value, otsColumn);
        } else if (value instanceof Long) {
            return primaryKeyValue((Long)value, otsColumn);
        } else if (value instanceof Float) {
            return primaryKeyValue((Float)value, otsColumn);
        } else if (value instanceof Double) {
            return primaryKeyValue((Double)value, otsColumn);
        } else if (value instanceof Boolean) {
            return primaryKeyValue((Boolean)value, otsColumn);
        } else if (value instanceof String) {
            return primaryKeyValue((String)value, otsColumn);
        } else {
            return primaryKeyValue(value, otsColumn);
        }
    }

    public static Object getValue(Column column) {
        switch (column.getValue().getType()) {
            case BINARY: {
                return column.getValue().asBinary();
            }
            case INTEGER: {
                return column.getValue().asLong();
            }
            case DOUBLE: {
                return column.getValue().asDouble();
            }
            case BOOLEAN: {
                return column.getValue().asBoolean();
            }
            case STRING: {
                return column.getValue().asString();
            }
            default: {
                return null;
            }
        }
    }

    public static Object getValue(Column column, OtsColumn otsColumn, Class<?> clazz, Type type) {
        switch (column.getValue().getType()) {
            case BINARY:
                return getValue(column.getValue().asBinary(), otsColumn, clazz);
            case INTEGER:
                return getValue(column.getValue().asLong(), clazz);
            case DOUBLE:
                return getValue(column.getValue().asDouble(), clazz);
            case BOOLEAN:
                return getValue(column.getValue().asBoolean(), clazz);
            case STRING:
                return getValue(column.getValue().asString(), otsColumn, clazz, type);
            default:
                return null;
        }
    }

    public static Object getValue(PrimaryKeyColumn column, OtsColumn otsColumn, Class<?> clazz, Type type) {
        switch (column.getValue().getType()) {
            case BINARY:
                return getValue(column.getValue().asBinary(), otsColumn, clazz);
            case INTEGER:
                return getValue(column.getValue().asLong(), clazz);
            case STRING:
                return getValue(column.getValue().asString(), otsColumn, clazz, type);
            default:
                return null;
        }
    }

    public static <T> PrimaryKey primaryKey(T key) {
        Class<?> clazz = key.getClass();
        if (clazz.isAssignableFrom(PrimaryKey.class)) {
            return (PrimaryKey)key;
        } else {
            List<PrimaryKeyColumn> columns = Lists.newArrayList();
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                String fieldName = field.getName();
                Object value = FieldUtils.invokeRead(field, key);
                if (value == null) {
                    continue;
                }
                OtsColumn otsColumn = field.getAnnotation(OtsColumn.class);
                String columnName = getColumnName(fieldName, otsColumn);
                PrimaryKeyValue primaryKeyValue = null;
                if (otsColumn == null) {
                    primaryKeyValue = getPrimaryKeyValue(value, null);
                } else if (otsColumn.primaryKey()) {
                    primaryKeyValue = getPrimaryKeyValue(value, otsColumn);
                }
                if (primaryKeyValue == null) {
                    continue;
                }
                columns.add(new PrimaryKeyColumn(columnName, primaryKeyValue));
            }
            return new PrimaryKey(columns);
        }
    }

    public static <T> PrimaryKey primaryKey(T key, KeyType keyType, Direction direction) {
        Class<?> clazz = key.getClass();
        if (clazz.isAssignableFrom(PrimaryKey.class)) {
            return (PrimaryKey)key;
        } else {
            List<PrimaryKeyColumn> columns = Lists.newArrayList();
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                String fieldName = field.getName();
                Object value = FieldUtils.invokeRead(field, key);
                OtsColumn otsColumn = field.getAnnotation(OtsColumn.class);
                String columnName = ColumnUtils.getColumnName(fieldName, otsColumn);
                PrimaryKeyValue primaryKeyValue = primaryKeyValue(value, otsColumn, keyType, direction);
                if (primaryKeyValue != null) {
                    columns.add(new PrimaryKeyColumn(columnName, primaryKeyValue));
                }
            }
            return new PrimaryKey(columns);
        }
    }

    private static PrimaryKeyValue primaryKeyValue(byte[] value, OtsColumn otsColumn) {
        if (otsColumn == null) {
            return PrimaryKeyValue.fromBinary(value);
        }
        switch (otsColumn.type()) {
            case NONE:
            case BINARY: {
                byte[] actual = compress(value, otsColumn);
                if (actual == null) {
                    return null;
                }
                return PrimaryKeyValue.fromBinary(actual);
            }
            case STRING: {
                byte[] actual = compress(value, otsColumn);
                if (actual == null) {
                    return null;
                }
                return PrimaryKeyValue.fromString(new String(actual, StandardCharsets.UTF_8));
            }
            default:
                return null;
        }
    }

    private static PrimaryKeyValue primaryKeyValue(Short value, OtsColumn otsColumn) {
        if (otsColumn == null) {
            return PrimaryKeyValue.fromLong(value.longValue());
        }
        switch (otsColumn.type()) {
            case NONE:
            case INTEGER: {
                return PrimaryKeyValue.fromLong(value.longValue());
            }
            case STRING: {
                return PrimaryKeyValue.fromString(value.toString());
            }
            default: {
                return null;
            }
        }
    }

    private static PrimaryKeyValue primaryKeyValue(Integer value, OtsColumn otsColumn) {
        if (otsColumn == null) {
            return PrimaryKeyValue.fromLong(value.longValue());
        }
        switch (otsColumn.type()) {
            case NONE:
            case INTEGER: {
                return PrimaryKeyValue.fromLong(value.longValue());
            }
            case STRING: {
                return PrimaryKeyValue.fromString(value.toString());
            }
            default: {
                return null;
            }
        }
    }

    private static PrimaryKeyValue primaryKeyValue(Long value, OtsColumn otsColumn) {
        if (otsColumn == null) {
            return PrimaryKeyValue.fromLong(value);
        }
        switch (otsColumn.type()) {
            case NONE:
            case INTEGER: {
                return PrimaryKeyValue.fromLong(value);
            }
            case STRING: {
                return PrimaryKeyValue.fromString(value.toString());
            }
            default: {
                return null;
            }
        }
    }

    private static PrimaryKeyValue primaryKeyValue(Float value, OtsColumn otsColumn) {
        if (otsColumn == null) {
            return PrimaryKeyValue.fromString(value.toString());
        }
        switch (otsColumn.type()) {
            case NONE:
            case STRING: {
                return PrimaryKeyValue.fromString(value.toString());
            }
            default: {
                return null;
            }
        }
    }

    private static PrimaryKeyValue primaryKeyValue(Double value, OtsColumn otsColumn) {
        if (otsColumn == null) {
            return PrimaryKeyValue.fromString(value.toString());
        }
        switch (otsColumn.type()) {
            case NONE:
            case STRING: {
                return PrimaryKeyValue.fromString(value.toString());
            }
            default: {
                return null;
            }
        }
    }

    private static PrimaryKeyValue primaryKeyValue(Boolean value, OtsColumn otsColumn) {
        if (otsColumn == null) {
            return PrimaryKeyValue.fromString(value.toString());
        }
        switch (otsColumn.type()) {
            case NONE:
            case STRING: {
                return PrimaryKeyValue.fromString(value.toString());
            }
            default: {
                return null;
            }
        }
    }

    private static PrimaryKeyValue primaryKeyValue(String value, OtsColumn otsColumn) {
        if (otsColumn == null) {
            return PrimaryKeyValue.fromString(value);
        }
        switch (otsColumn.type()) {
            case NONE:
            case STRING: {
                return PrimaryKeyValue.fromString(value);
            }
            case INTEGER: {
                return PrimaryKeyValue.fromLong(Long.parseLong(value));
            }
            case BINARY: {
                byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
                byte[] actual = compress(bytes, otsColumn);
                if (actual == null) {
                    return null;
                }
                return PrimaryKeyValue.fromBinary(actual);
            }
            default: {
                return null;
            }
        }
    }

    private static PrimaryKeyValue primaryKeyValue(Object value, OtsColumn otsColumn) {
        if (otsColumn == null) {
            return PrimaryKeyValue.fromString(JSON.toJSONString(value));
        }
        switch (otsColumn.type()) {
            case NONE:
            case STRING: {
                return PrimaryKeyValue.fromString(JSON.toJSONString(value));
            }
            default: {
                return null;
            }
        }
    }

    private static ColumnValue columnValue(byte[] value, OtsColumn otsColumn) {
        if (otsColumn == null) {
            return ColumnValue.fromBinary(value);
        }
        switch (otsColumn.type()) {
            case NONE:
            case BINARY: {
                byte[] actual = compress(value, otsColumn);
                if (actual == null) {
                    return null;
                }
                return ColumnValue.fromBinary(actual);
            }
            case STRING: {
                byte[] actual = compress(value, otsColumn);
                if (actual == null) {
                    return null;
                }
                return ColumnValue.fromString(new String(actual, StandardCharsets.UTF_8));
            }
            default:
                return null;
        }
    }

    private static ColumnValue columnValue(Short value, OtsColumn otsColumn) {
        if (otsColumn == null) {
            return ColumnValue.fromLong(value.longValue());
        }
        switch (otsColumn.type()) {
            case NONE:
            case INTEGER: {
                return ColumnValue.fromLong(value.longValue());
            }
            case DOUBLE: {
                return ColumnValue.fromDouble(value.doubleValue());
            }
            case STRING: {
                return ColumnValue.fromString(value.toString());
            }
            default: {
                return null;
            }
        }
    }

    private static ColumnValue columnValue(Integer value, OtsColumn otsColumn) {
        if (otsColumn == null) {
            return ColumnValue.fromLong(value.longValue());
        }
        switch (otsColumn.type()) {
            case NONE:
            case INTEGER: {
                return ColumnValue.fromLong(value.longValue());
            }
            case DOUBLE: {
                return ColumnValue.fromDouble(value.doubleValue());
            }
            case STRING: {
                return ColumnValue.fromString(value.toString());
            }
            default: {
                return null;
            }
        }
    }

    private static ColumnValue columnValue(Long value, OtsColumn otsColumn) {
        if (otsColumn == null) {
            return ColumnValue.fromLong(value);
        }
        switch (otsColumn.type()) {
            case NONE:
            case INTEGER: {
                return ColumnValue.fromLong(value);
            }
            case DOUBLE: {
                return ColumnValue.fromDouble(value.doubleValue());
            }
            case STRING: {
                return ColumnValue.fromString(value.toString());
            }
            default: {
                return null;
            }
        }
    }

    private static ColumnValue columnValue(Float value, OtsColumn otsColumn) {
        if (otsColumn == null) {
            return ColumnValue.fromDouble(value);
        }
        switch (otsColumn.type()) {
            case NONE:
            case DOUBLE: {
                return ColumnValue.fromDouble(value);
            }
            case STRING: {
                return ColumnValue.fromString(value.toString());
            }
            default: {
                return null;
            }
        }
    }

    private static ColumnValue columnValue(Double value, OtsColumn otsColumn) {
        if (otsColumn == null) {
            return ColumnValue.fromDouble(value);
        }
        switch (otsColumn.type()) {
            case NONE:
            case DOUBLE: {
                return ColumnValue.fromDouble(value);
            }
            case STRING: {
                return ColumnValue.fromString(value.toString());
            }
            default: {
                return null;
            }
        }
    }

    private static ColumnValue columnValue(Boolean value, OtsColumn otsColumn) {
        if (otsColumn == null) {
            return ColumnValue.fromBoolean(value);
        }
        switch (otsColumn.type()) {
            case NONE:
            case BOOLEAN: {
                return ColumnValue.fromBoolean(value);
            }
            case STRING: {
                return ColumnValue.fromString(value.toString());
            }
            default: {
                return null;
            }
        }
    }

    private static ColumnValue columnValue(String value, OtsColumn otsColumn) {
        if (otsColumn == null) {
            return ColumnValue.fromString(value);
        }
        switch (otsColumn.type()) {
            case NONE:
            case STRING: {
                return ColumnValue.fromString(value);
            }
            case INTEGER: {
                return ColumnValue.fromLong(Long.parseLong(value));
            }
            case DOUBLE: {
                return ColumnValue.fromDouble(Double.parseDouble(value));
            }
            case BOOLEAN: {
                return ColumnValue.fromBoolean(Boolean.parseBoolean(value));
            }
            case BINARY: {
                byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
                byte[] actual = compress(bytes, otsColumn);
                if (actual == null) {
                    return null;
                }
                return ColumnValue.fromBinary(actual);
            }
            default: {
                return null;
            }
        }
    }

    private static ColumnValue columnValue(Object value, OtsColumn otsColumn) {
        if (otsColumn == null) {
            return ColumnValue.fromString(JSON.toJSONString(value));
        }
        switch (otsColumn.type()) {
            case NONE:
            case STRING: {
                return ColumnValue.fromString(JSON.toJSONString(value));
            }
            default: {
                return null;
            }
        }
    }

    private static Object getValue(byte[] value, OtsColumn otsColumn, Class<?> clazz) {
        if (otsColumn == null) {
            return getValue(value, clazz, NoCompress.class);
        } else {
            return getValue(value, clazz, otsColumn.compress());
        }
    }

    private static Object getValue(byte[] value, Class<?> clazz, Class<?> uncompress) {
        if (clazz.isAssignableFrom(byte[].class) || clazz.isAssignableFrom(Byte[].class)) {
            return uncompress(value, uncompress);
        } else if (clazz.isAssignableFrom(String.class)) {
            byte[] actual = uncompress(value, uncompress);
            if (actual != null) {
                return new String(actual, StandardCharsets.UTF_8);
            }
        }
        return null;
    }

    private static Object getValue(Long value, Class<?> clazz) {
        if (clazz.isAssignableFrom(Byte.class) || clazz.isAssignableFrom(byte.class)) {
            return value.byteValue();
        } else if (clazz.isAssignableFrom(Short.class) || clazz.isAssignableFrom(short.class)) {
            return value.shortValue();
        } else if (clazz.isAssignableFrom(Integer.class) || clazz.isAssignableFrom(int.class)) {
            return value.intValue();
        } else if (clazz.isAssignableFrom(Long.class) || clazz.isAssignableFrom(long.class)) {
            return value;
        } else if (clazz.isAssignableFrom(Float.class) || clazz.isAssignableFrom(float.class)) {
            return value.floatValue();
        } else if (clazz.isAssignableFrom(Double.class) || clazz.isAssignableFrom(double.class)) {
            return value.doubleValue();
        } else if (clazz.isAssignableFrom(String.class)) {
            return String.valueOf(value);
        }
        return null;
    }

    private static Object getValue(Boolean value, Class<?> clazz) {
        if (clazz.isAssignableFrom(Boolean.class) || clazz.isAssignableFrom(boolean.class)) {
            return value;
        } else if (clazz.isAssignableFrom(String.class)) {
            return String.valueOf(value);
        }
        return null;
    }

    private static Object getValue(Double value, Class<?> clazz) {
        if (clazz.isAssignableFrom(Byte.class) || clazz.isAssignableFrom(byte.class)) {
            return value.byteValue();
        } else if (clazz.isAssignableFrom(Short.class) || clazz.isAssignableFrom(short.class)) {
            return value.shortValue();
        } else if (clazz.isAssignableFrom(Integer.class) || clazz.isAssignableFrom(int.class)) {
            return value.intValue();
        } else if (clazz.isAssignableFrom(Long.class) || clazz.isAssignableFrom(long.class)) {
            return value.longValue();
        } else if (clazz.isAssignableFrom(Float.class) || clazz.isAssignableFrom(float.class)) {
            return value.floatValue();
        } else if (clazz.isAssignableFrom(Double.class) || clazz.isAssignableFrom(double.class)) {
            return value;
        } else if (clazz.isAssignableFrom(String.class)) {
            return String.valueOf(value);
        }
        return null;
    }

    private static Object getValue(String value, OtsColumn otsColumn, Class<?> clazz, Type type) {
        if (otsColumn == null) {
            return getValue(value, clazz, NoCompress.class, type);
        } else {
            return getValue(value, clazz, otsColumn.compress(), type);
        }
    }

    private static Object getValue(String value, Class<?> clazz, Class<?> uncompress, Type type) {
        if (clazz.isAssignableFrom(byte[].class) || clazz.isAssignableFrom(Byte[].class)) {
            byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
            return uncompress(bytes, uncompress);
        } else if (clazz.isAssignableFrom(Short.class) || clazz.isAssignableFrom(short.class)) {
            return Short.parseShort(value);
        } else if (clazz.isAssignableFrom(Integer.class) || clazz.isAssignableFrom(int.class)) {
            return Integer.parseInt(value);
        } else if (clazz.isAssignableFrom(Long.class) || clazz.isAssignableFrom(long.class)) {
            return Long.parseLong(value);
        } else if (clazz.isAssignableFrom(Float.class) || clazz.isAssignableFrom(float.class)) {
            return Float.parseFloat(value);
        } else if (clazz.isAssignableFrom(Double.class) || clazz.isAssignableFrom(double.class)) {
            return Double.parseDouble(value);
        } else if (clazz.isAssignableFrom(char[].class) || clazz.isAssignableFrom(Charset[].class)) {
            return value.toCharArray();
        } else if (clazz.isAssignableFrom(Boolean.class) || clazz.isAssignableFrom(boolean.class)) {
            return Boolean.parseBoolean(value);
        } else if (clazz.isAssignableFrom(String.class)) {
            return value;
        } else if (clazz.isAssignableFrom(List.class)) {
            if (type == null) {
                return JSON.parseArray(value);
            } else if (type instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType)type;
                return JSON.parseArray(value, (Class<?>)parameterizedType.getActualTypeArguments()[0]);
            } else {
                return JSON.parseArray(value);
            }
        } else if (clazz.isAssignableFrom(Map.class)) {
            return JSON.parseObject(value, Map.class);
        } else {
            return JSON.parseObject(value, clazz);
        }
    }

    private static byte[] compress(byte[] value, OtsColumn otsColumn) {
        if (otsColumn == null) {
            return value;
        }
        try {
            Method method = otsColumn.compress().getDeclaredMethod("compress", byte[].class);
            return (byte[])method.invoke(null, (Object)value);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static byte[] uncompress(byte[] value, Class<?> uncompress) {
        if (uncompress == null) {
            return value;
        }
        try {
            Method method = uncompress.getDeclaredMethod("uncompress", byte[].class);
            return (byte[])method.invoke(null, (Object)value);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static PrimaryKeyValue primaryKeyValue(Object value, OtsColumn otsColumn, KeyType keyType, Direction direction) {
        switch (keyType) {
            case START:
                switch (direction) {
                    case FORWARD: {
                        PrimaryKeyValue primaryKeyValue = getPrimaryKeyValue(value, otsColumn);
                        if (primaryKeyValue == null) {
                            return PrimaryKeyValue.INF_MIN;
                        } else {
                            return primaryKeyValue;
                        }
                    }
                    case BACKWARD: {
                        PrimaryKeyValue primaryKeyValue = getPrimaryKeyValue(value, otsColumn);
                        if (primaryKeyValue == null) {
                            return PrimaryKeyValue.INF_MAX;
                        } else {
                            return primaryKeyValue;
                        }
                    }
                }
            case END:
                switch (direction) {
                    case FORWARD: {
                        PrimaryKeyValue primaryKeyValue = getPrimaryKeyValue(value, otsColumn);
                        if (primaryKeyValue == null) {
                            return PrimaryKeyValue.INF_MAX;
                        } else {
                            return primaryKeyValue;
                        }
                    }
                    case BACKWARD: {
                        PrimaryKeyValue primaryKeyValue = getPrimaryKeyValue(value, otsColumn);
                        if (primaryKeyValue == null) {
                            return PrimaryKeyValue.INF_MIN;
                        } else {
                            return primaryKeyValue;
                        }
                    }
                }
        }
        return null;
    }
}
