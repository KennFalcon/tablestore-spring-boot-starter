package org.springframework.boot.autoconfigure.tablestore.utils;

import com.alibaba.fastjson.JSON;
import com.alicloud.openservices.tablestore.model.*;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.tablestore.annotation.OtsColumn;
import org.springframework.boot.autoconfigure.tablestore.exception.OtsException;
import org.springframework.boot.autoconfigure.tablestore.model.RangeGetQuery;
import org.springframework.boot.autoconfigure.tablestore.model.RangeGetQuery.KeyType;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
        if (otsColumn == null) {
            return fieldName;
        }
        if (StringUtils.isBlank(otsColumn.name())) {
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
        } else if (value instanceof Integer) {
            return columnValue((Integer)value, otsColumn);
        } else if (value instanceof Long) {
            return columnValue((Long)value, otsColumn);
        } else if (value instanceof Boolean) {
            return columnValue((Boolean)value, otsColumn);
        } else if (value instanceof Double) {
            return columnValue((Double)value, otsColumn);
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
        } else if (value instanceof Integer) {
            return primaryKeyValue((Integer)value, otsColumn);
        } else if (value instanceof Long) {
            return primaryKeyValue((Long)value, otsColumn);
        } else if (value instanceof String) {
            return primaryKeyValue((String)value, otsColumn);
        } else {
            return primaryKeyValue(value, otsColumn);
        }
    }

    public static Object getValue(Column column, OtsColumn otsColumn) {
        switch (column.getValue().getType()) {
            case BINARY:
                return getValue(column.getValue().asBinary(), otsColumn);
            case INTEGER:
                return getValue(column.getValue().asLong(), otsColumn);
            case DOUBLE:
                return getValue(column.getValue().asDouble(), otsColumn);
            case BOOLEAN:
                return getValue(column.getValue().asBoolean(), otsColumn);
            case STRING:
                return getValue(column.getValue().asString(), otsColumn);
            default:
                return null;
        }
    }

    public static Object getValue(PrimaryKeyColumn column, OtsColumn otsColumn) {
        switch (column.getValue().getType()) {
            case BINARY:
                return getValue(column.getValue().asBinary(), otsColumn);
            case INTEGER:
                return getValue(column.getValue().asLong(), otsColumn);
            case STRING:
                return getValue(column.getValue().asString(), otsColumn);
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
                Object value = invokeGetValue(key, fieldName);
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

    public static <T> PrimaryKey primaryKey(T key, RangeGetQuery.KeyType keyType, Direction direction) {
        Class<?> clazz = key.getClass();
        if (clazz.isAssignableFrom(PrimaryKey.class)) {
            return (PrimaryKey)key;
        } else {
            List<PrimaryKeyColumn> columns = Lists.newArrayList();
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                String fieldName = field.getName();
                Object value = invokeGetValue(key, fieldName);
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

    public static <T> Object invokeGetValue(T data, String fieldName) {
        try {
            PropertyDescriptor pd = new PropertyDescriptor(fieldName, data.getClass());
            Method method = pd.getReadMethod();
            return method.invoke(data);
        } catch (IntrospectionException | IllegalAccessException | InvocationTargetException e) {
            throw new OtsException("reflect error, class: %s, field: %s", e, data.getClass().getName(), fieldName);
        }
    }

    private static PrimaryKeyValue primaryKeyValue(byte[] value, OtsColumn otsColumn) {
        if (otsColumn == null) {
            return PrimaryKeyValue.fromBinary(value);
        }
        if (otsColumn.clazz().isAssignableFrom(void.class)) {
            return PrimaryKeyValue.fromBinary(value);
        } else if (otsColumn.clazz().isAssignableFrom(byte[].class)) {
            byte[] actual = compress(value, otsColumn);
            if (actual != null) {
                return PrimaryKeyValue.fromBinary(actual);
            }
        } else if (otsColumn.clazz().isAssignableFrom(String.class)) {
            byte[] actual = compress(value, otsColumn);
            if (actual != null) {
                if (StringUtils.isBlank(otsColumn.charset())) {
                    return PrimaryKeyValue.fromString(new String(actual, StandardCharsets.UTF_8));
                } else {
                    return PrimaryKeyValue.fromString(new String(actual, Charset.forName(otsColumn.charset())));
                }
            }
        }
        return null;
    }

    private static PrimaryKeyValue primaryKeyValue(Integer value, OtsColumn otsColumn) {
        if (otsColumn == null) {
            return PrimaryKeyValue.fromLong(value.longValue());
        }
        if (otsColumn.clazz().isAssignableFrom(void.class)) {
            return PrimaryKeyValue.fromLong(value.longValue());
        } else if (otsColumn.clazz().isAssignableFrom(String.class)) {
            return PrimaryKeyValue.fromString(value.toString());
        } else if (otsColumn.clazz().isAssignableFrom(Long.class)) {
            return PrimaryKeyValue.fromLong(value.longValue());
        }
        return null;
    }

    private static PrimaryKeyValue primaryKeyValue(Long value, OtsColumn otsColumn) {
        if (otsColumn == null) {
            return PrimaryKeyValue.fromLong(value);
        }
        if (otsColumn.clazz().isAssignableFrom(void.class)) {
            return PrimaryKeyValue.fromLong(value);
        } else if (otsColumn.clazz().isAssignableFrom(String.class)) {
            return PrimaryKeyValue.fromString(value.toString());
        } else if (otsColumn.clazz().isAssignableFrom(Long.class)) {
            return PrimaryKeyValue.fromLong(value);
        }
        return null;
    }

    private static PrimaryKeyValue primaryKeyValue(String value, OtsColumn otsColumn) {
        if (otsColumn == null) {
            return PrimaryKeyValue.fromString(value);
        }
        if (otsColumn.clazz().isAssignableFrom(void.class)) {
            return PrimaryKeyValue.fromString(value);
        } else if (otsColumn.clazz().isAssignableFrom(Integer.class)) {
            return PrimaryKeyValue.fromLong(Integer.parseInt(value));
        } else if (otsColumn.clazz().isAssignableFrom(Long.class)) {
            return PrimaryKeyValue.fromLong(Long.parseLong(value));
        } else if (otsColumn.clazz().isAssignableFrom(String.class)) {
            return PrimaryKeyValue.fromString(value);
        } else if (otsColumn.clazz().isAssignableFrom(byte[].class)) {
            byte[] bytes;
            if (StringUtils.isBlank(otsColumn.charset())) {
                bytes = value.getBytes(StandardCharsets.UTF_8);
            } else {
                bytes = value.getBytes(Charset.forName(otsColumn.charset()));
            }
            byte[] actual = compress(bytes, otsColumn);
            if (actual != null) {
                return PrimaryKeyValue.fromBinary(actual);
            }
        }
        return null;
    }

    private static PrimaryKeyValue primaryKeyValue(Object value, OtsColumn otsColumn) {
        if (otsColumn == null) {
            return PrimaryKeyValue.fromString(JSON.toJSONString(value));
        }
        if (otsColumn.clazz().isAssignableFrom(void.class)) {
            return PrimaryKeyValue.fromString(JSON.toJSONString(value));
        } else if (otsColumn.clazz().isAssignableFrom(String.class)) {
            return PrimaryKeyValue.fromString(JSON.toJSONString(value));
        }
        return null;
    }

    private static ColumnValue columnValue(byte[] value, OtsColumn otsColumn) {
        if (otsColumn == null) {
            return ColumnValue.fromBinary(value);
        }
        if (otsColumn.clazz().isAssignableFrom(void.class)) {
            return ColumnValue.fromBinary(value);
        } else if (otsColumn.clazz().isAssignableFrom(byte[].class)) {
            return ColumnValue.fromBinary(value);
        } else if (otsColumn.clazz().isAssignableFrom(String.class)) {
            byte[] actual = compress(value, otsColumn);
            if (actual != null) {
                if (StringUtils.isBlank(otsColumn.charset())) {
                    return ColumnValue.fromString(new String(actual, StandardCharsets.UTF_8));
                } else {
                    return ColumnValue.fromString(new String(actual, Charset.forName(otsColumn.charset())));
                }
            }
        }
        return null;
    }

    private static ColumnValue columnValue(Integer value, OtsColumn otsColumn) {
        if (otsColumn == null) {
            return ColumnValue.fromLong(value.longValue());
        }
        if (otsColumn.clazz().isAssignableFrom(void.class)) {
            return ColumnValue.fromLong(value.longValue());
        } else if (otsColumn.clazz().isAssignableFrom(Integer.class)) {
            return ColumnValue.fromLong(value.longValue());
        } else if (otsColumn.clazz().isAssignableFrom(Long.class)) {
            return ColumnValue.fromLong(value.longValue());
        } else if (otsColumn.clazz().isAssignableFrom(Double.class)) {
            return ColumnValue.fromDouble(value.doubleValue());
        } else if (otsColumn.clazz().isAssignableFrom(String.class)) {
            return ColumnValue.fromString(value.toString());
        }
        return null;
    }

    private static ColumnValue columnValue(Long value, OtsColumn otsColumn) {
        if (otsColumn == null) {
            return ColumnValue.fromLong(value);
        }
        if (otsColumn.clazz().isAssignableFrom(void.class)) {
            return ColumnValue.fromLong(value);
        } else if (otsColumn.clazz().isAssignableFrom(Long.class)) {
            return ColumnValue.fromLong(value);
        } else if (otsColumn.clazz().isAssignableFrom(Double.class)) {
            return ColumnValue.fromDouble(value.doubleValue());
        } else if (otsColumn.clazz().isAssignableFrom(String.class)) {
            return ColumnValue.fromString(value.toString());
        }
        return null;
    }

    private static ColumnValue columnValue(Boolean value, OtsColumn otsColumn) {
        if (otsColumn == null) {
            return ColumnValue.fromBoolean(value);
        }
        if (otsColumn.clazz().isAssignableFrom(void.class)) {
            return ColumnValue.fromBoolean(value);
        } else if (otsColumn.clazz().isAssignableFrom(Boolean.class)) {
            return ColumnValue.fromBoolean(value);
        } else if (otsColumn.clazz().isAssignableFrom(String.class)) {
            return ColumnValue.fromString(value.toString());
        }
        return null;
    }

    private static ColumnValue columnValue(Double value, OtsColumn otsColumn) {
        if (otsColumn == null) {
            return ColumnValue.fromDouble(value);
        }
        if (otsColumn.clazz().isAssignableFrom(void.class)) {
            return ColumnValue.fromDouble(value);
        } else if (otsColumn.clazz().isAssignableFrom(Double.class)) {
            return ColumnValue.fromDouble(value);
        } else if (otsColumn.clazz().isAssignableFrom(String.class)) {
            return ColumnValue.fromString(value.toString());
        }
        return null;
    }

    private static ColumnValue columnValue(String value, OtsColumn otsColumn) {
        if (otsColumn == null) {
            return ColumnValue.fromString(value);
        }
        if (otsColumn.clazz().isAssignableFrom(void.class)) {
            return ColumnValue.fromString(value);
        } else if (otsColumn.clazz().isAssignableFrom(Integer.class)) {
            return ColumnValue.fromLong(Integer.parseInt(value));
        } else if (otsColumn.clazz().isAssignableFrom(Long.class)) {
            return ColumnValue.fromLong(Long.parseLong(value));
        } else if (otsColumn.clazz().isAssignableFrom(String.class)) {
            return ColumnValue.fromString(value);
        } else if (otsColumn.clazz().isAssignableFrom(byte[].class)) {
            byte[] bytes;
            if (StringUtils.isBlank(otsColumn.charset())) {
                bytes = value.getBytes(StandardCharsets.UTF_8);
            } else {
                bytes = value.getBytes(Charset.forName(otsColumn.charset()));
            }
            byte[] actual = compress(bytes, otsColumn);
            if (actual != null) {
                return ColumnValue.fromBinary(actual);
            }
        }
        return null;
    }

    private static ColumnValue columnValue(Object value, OtsColumn otsColumn) {
        if (otsColumn == null) {
            return ColumnValue.fromString(JSON.toJSONString(value));
        }
        if (otsColumn.clazz().isAssignableFrom(void.class)) {
            return ColumnValue.fromString(JSON.toJSONString(value));
        } else if (otsColumn.clazz().isAssignableFrom(String.class)) {
            return ColumnValue.fromString(JSON.toJSONString(value));
        }
        return null;
    }

    private static Object getValue(byte[] value, OtsColumn otsColumn) {
        if (otsColumn == null) {
            return value;
        }
        if (otsColumn.clazz().isAssignableFrom(void.class)) {
            return value;
        } else if (otsColumn.clazz().isAssignableFrom(byte[].class)) {
            return uncompress(value, otsColumn);
        } else if (otsColumn.clazz().isAssignableFrom(String.class)) {
            byte[] actual = uncompress(value, otsColumn);
            if (actual != null) {
                if (StringUtils.isBlank(otsColumn.charset())) {
                    return new String(actual, StandardCharsets.UTF_8);
                } else {
                    return new String(actual, Charset.forName(otsColumn.charset()));
                }
            }
        }
        return null;
    }

    private static Object getValue(Long value, OtsColumn otsColumn) {
        if (otsColumn == null) {
            return value;
        }
        if (otsColumn.clazz().isAssignableFrom(void.class)) {
            return value;
        } else if (otsColumn.clazz().isAssignableFrom(Long.class)) {
            return value;
        } else if (otsColumn.clazz().isAssignableFrom(Integer.class)) {
            return value.intValue();
        } else if (otsColumn.clazz().isAssignableFrom(Double.class)) {
            return value.doubleValue();
        } else if (otsColumn.clazz().isAssignableFrom(String.class)) {
            return String.valueOf(value);
        }
        return null;
    }

    private static Object getValue(Boolean value, OtsColumn otsColumn) {
        if (otsColumn == null) {
            return value;
        }
        if (otsColumn.clazz().isAssignableFrom(void.class)) {
            return value;
        } else if (otsColumn.clazz().isAssignableFrom(Boolean.class)) {
            return value;
        } else if (otsColumn.clazz().isAssignableFrom(String.class)) {
            return String.valueOf(value);
        }
        return null;
    }

    private static Object getValue(Double value, OtsColumn otsColumn) {
        if (otsColumn == null) {
            return value;
        }
        if (otsColumn.clazz().isAssignableFrom(void.class)) {
            return value;
        } else if (otsColumn.clazz().isAssignableFrom(Integer.class)) {
            return value.intValue();
        } else if (otsColumn.clazz().isAssignableFrom(Long.class)) {
            return value.longValue();
        } else if (otsColumn.clazz().isAssignableFrom(Double.class)) {
            return value;
        } else if (otsColumn.clazz().isAssignableFrom(String.class)) {
            return String.valueOf(value);
        }
        return null;
    }

    private static Object getValue(String value, OtsColumn otsColumn) {
        if (otsColumn == null) {
            return value;
        }
        if (otsColumn.clazz().isAssignableFrom(void.class)) {
            return value;
        } else if (otsColumn.clazz().isAssignableFrom(Integer.class)) {
            return Integer.parseInt(value);
        } else if (otsColumn.clazz().isAssignableFrom(Long.class)) {
            return Long.parseLong(value);
        } else if (otsColumn.clazz().isAssignableFrom(String.class)) {
            return value;
        } else if (otsColumn.clazz().isAssignableFrom(byte[].class)) {
            byte[] bytes;
            if (StringUtils.isBlank(otsColumn.charset())) {
                bytes = value.getBytes(StandardCharsets.UTF_8);
            } else {
                bytes = value.getBytes(Charset.forName(otsColumn.charset()));
            }
            return uncompress(bytes, otsColumn);
        } else if (otsColumn.clazz().isAssignableFrom(List.class)) {
            if (otsColumn.elementClazz().isAssignableFrom(void.class)) {
                return JSON.parseArray(value);
            } else {
                return JSON.parseArray(value, otsColumn.elementClazz());
            }
        } else if (otsColumn.clazz().isAssignableFrom(Map.class)) {
            return JSON.parseObject(value, otsColumn.clazz());
        } else {
            return JSON.parseObject(value, otsColumn.clazz());
        }
    }

    private static byte[] compress(byte[] value, OtsColumn otsColumn) {
        if (otsColumn == null) {
            return value;
        }
        try {
            Method method = otsColumn.compress().getDeclaredMethod("compress", byte[].class);
            return (byte[])method.invoke(null, value);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static byte[] uncompress(byte[] value, OtsColumn otsColumn) {
        if (otsColumn == null) {
            return value;
        }
        try {
            Method method = otsColumn.compress().getDeclaredMethod("uncompress", byte[].class);
            return (byte[])method.invoke(null, value);
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
