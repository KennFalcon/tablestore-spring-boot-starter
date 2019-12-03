package org.springframework.boot.autoconfigure.tablestore;

import com.alibaba.fastjson.JSON;
import com.alicloud.openservices.tablestore.model.*;
import com.google.common.base.CaseFormat;
import com.google.common.base.Converter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.tablestore.annotation.FieldMapper;

import java.util.List;
import java.util.Map;

/**
 * @project: tablestore-spring-boot-starter
 * @description: OTS字段工具类
 * @author: Kenn
 * @create: 2018-11-24 14:27
 */
class ColumnUtils {

    private static final Converter<String, String> CONVERTER = CaseFormat.LOWER_CAMEL.converterTo(CaseFormat.LOWER_UNDERSCORE);

    public static ColumnValue getColumnValue(Object value) {
        ColumnValue columnValue = null;
        if (value instanceof byte[]) {
            columnValue = ColumnValue.fromBinary((byte[])value);
        } else if (value instanceof Integer) {
            columnValue = ColumnValue.fromLong(((Integer)value).longValue());
        } else if (value instanceof Long) {
            columnValue = ColumnValue.fromLong((Long)value);
        } else if (value instanceof Boolean) {
            columnValue = ColumnValue.fromBoolean((Boolean)value);
        } else if (value instanceof Double) {
            columnValue = ColumnValue.fromDouble((Double)value);
        } else if (value instanceof String) {
            String stringValue = (String)value;
            if (StringUtils.isNotEmpty(stringValue)) {
                columnValue = ColumnValue.fromString(stringValue);
            }
        } else if (value instanceof List) {
            List valueList = (List)value;
            if (CollectionUtils.isNotEmpty(valueList)) {
                columnValue = ColumnValue.fromString(JSON.toJSONString(valueList));
            }
        } else if (value instanceof Map) {
            Map valueMap = (Map)value;
            if (MapUtils.isNotEmpty(valueMap)) {
                columnValue = ColumnValue.fromString(JSON.toJSONString(valueMap));
            }
        } else {
            columnValue = ColumnValue.fromString(JSON.toJSONString(value));
        }
        return columnValue;
    }

    public static PrimaryKeyValue getPrimaryKeyValue(Object value) {
        if (value == null) {
            return null;
        }
        PrimaryKeyValue primaryKeyValue = null;
        if (value instanceof byte[]) {
            primaryKeyValue = PrimaryKeyValue.fromBinary((byte[])value);
        } else if (value instanceof Integer) {
            primaryKeyValue = PrimaryKeyValue.fromLong(((Integer)value).longValue());
        } else if (value instanceof Long) {
            primaryKeyValue = PrimaryKeyValue.fromLong((Long)value);
        } else if (value instanceof String) {
            String stringValue = (String)value;
            if (StringUtils.isNotEmpty(stringValue)) {
                primaryKeyValue = PrimaryKeyValue.fromString(stringValue);
            }
        } else if (value instanceof List) {
            List valueList = (List)value;
            if (CollectionUtils.isNotEmpty(valueList)) {
                primaryKeyValue = PrimaryKeyValue.fromString(JSON.toJSONString(valueList));
            }
        } else if (value instanceof Map) {
            Map valueMap = (Map)value;
            if (MapUtils.isNotEmpty(valueMap)) {
                primaryKeyValue = PrimaryKeyValue.fromString(JSON.toJSONString(valueMap));
            }
        } else {
            if (value != null) {
                primaryKeyValue = PrimaryKeyValue.fromString(JSON.toJSONString(value));
            }
        }
        return primaryKeyValue;
    }

    public static Object getValue(Column column, FieldMapper fieldMapper) {
        Object value = null;
        Class<?> fieldClass = fieldMapper.className();
        if (column.getValue().getType() == ColumnType.BOOLEAN) {
            value = column.getValue().asBoolean();
        } else if (column.getValue().getType() == ColumnType.BINARY) {
            value = column.getValue().asBinary();
        } else if (column.getValue().getType() == ColumnType.DOUBLE) {
            value = column.getValue().asDouble();
        } else if (column.getValue().getType() == ColumnType.INTEGER) {
            Long tmpValue = column.getValue().asLong();
            if (fieldClass.isAssignableFrom(Integer.class)) {
                value = tmpValue.intValue();
            } else if (fieldClass.isAssignableFrom(Long.class)) {
                value = tmpValue;
            }
        } else if (column.getValue().getType() == ColumnType.STRING) {
            if (fieldClass.isAssignableFrom(List.class)) {
                Class<?> elementClass = fieldMapper.elementClassName();
                value = JSON.parseArray(column.getValue().asString(), elementClass);
            } else if (fieldClass.isAssignableFrom(Map.class)) {
                value = JSON.parseObject(column.getValue().asString(), fieldClass);
            } else if (fieldClass.isAssignableFrom(String.class)) {
                value = column.getValue().asString();
            } else {
                value = JSON.parseObject(column.getValue().asString(), fieldClass);
            }
        }
        return value;
    }

    public static Object getValue(PrimaryKeyColumn column, FieldMapper fieldMapper) {
        Object value = null;
        Class<?> fieldClass = fieldMapper.className();
        if (column.getValue().getType() == PrimaryKeyType.BINARY) {
            value = column.getValue().asBinary();
        } else if (column.getValue().getType() == PrimaryKeyType.INTEGER) {
            Long tmpValue = column.getValue().asLong();
            if (fieldClass.isAssignableFrom(Integer.class)) {
                value = tmpValue.intValue();
            } else if (fieldClass.isAssignableFrom(Long.class)) {
                value = tmpValue;
            }
        } else if (column.getValue().getType() == PrimaryKeyType.STRING) {
            value = column.getValue().asString();
        }
        return value;
    }

    public static String getColumnName(String fieldName, String defaultName) {
        if (StringUtils.isBlank(defaultName)) {
            return CONVERTER.convert(fieldName);
        }
        return defaultName;
    }
}
