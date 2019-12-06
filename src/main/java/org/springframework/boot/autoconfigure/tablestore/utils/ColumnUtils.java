package org.springframework.boot.autoconfigure.tablestore.utils;

import com.alibaba.fastjson.JSON;
import com.alicloud.openservices.tablestore.model.*;
import com.google.common.base.CaseFormat;
import com.google.common.base.Converter;
import com.google.common.collect.Lists;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.checkerframework.checker.units.qual.A;
import org.springframework.boot.autoconfigure.tablestore.annotation.FieldMapper;
import org.springframework.boot.autoconfigure.tablestore.model.RangeGetQuery;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @project: tablestore-spring-boot-starter
 * @description: OTS字段工具类
 * @author: Kenn
 * @create: 2018-11-24 14:27
 */
public class ColumnUtils {

    private static final Converter<String, String> CONVERTER = CaseFormat.LOWER_CAMEL.converterTo(CaseFormat.LOWER_UNDERSCORE);

    public static ColumnValue getColumnValue(Object value) {
        ColumnValue columnValue;
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
            columnValue = ColumnValue.fromString((String)value);
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
            long tmpValue = column.getValue().asLong();
            if (fieldClass.isAssignableFrom(Integer.class)) {
                value = (int)tmpValue;
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

    public static Object getValue(Column column, Field field) {
        Object value = null;
        Class<?> clazz = field.getType();
        if (column.getValue().getType() == ColumnType.BOOLEAN) {
            value = column.getValue().asBoolean();
        } else if (column.getValue().getType() == ColumnType.BINARY) {
            value = column.getValue().asBinary();
        } else if (column.getValue().getType() == ColumnType.DOUBLE) {
            value = column.getValue().asDouble();
        } else if (column.getValue().getType() == ColumnType.INTEGER) {
            long tmpValue = column.getValue().asLong();
            if (clazz.isAssignableFrom(Integer.class)) {
                value = (int)tmpValue;
            } else if (clazz.isAssignableFrom(Long.class)) {
                value = tmpValue;
            }
        } else if (column.getValue().getType() == ColumnType.STRING) {
            if (clazz.isAssignableFrom(List.class)) {
                Type genericType = field.getGenericType();
                if (genericType == null) {
                    value =  JSON.parseArray(column.getValue().asString());
                } else if (genericType instanceof ParameterizedType) {
                    ParameterizedType parameterizedType = (ParameterizedType)genericType;
                    Type[] types = parameterizedType.getActualTypeArguments();
                    value = JSON.parseArray(column.getValue().asString(), types);
                }
            } else if (clazz.isAssignableFrom(Map.class)) {
                value = JSON.parseObject(column.getValue().asString(), clazz);
            } else if (clazz.isAssignableFrom(String.class)) {
                value = column.getValue().asString();
            } else {
                value = JSON.parseObject(column.getValue().asString(), clazz);
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

    public static String getColumnName(String fieldName, FieldMapper fieldMapper) {
        if (fieldMapper == null) {
            return CONVERTER.convert(fieldName);
        }
        if (StringUtils.isBlank(fieldMapper.name())) {
            return CONVERTER.convert(fieldName);
        }
        return fieldMapper.name();
    }

    public static PrimaryKey primaryKey(List<Pair<String, Object>> keyPairs, RangeGetQuery.KeyType keyType, Direction direction) {
        List<PrimaryKeyColumn> columns = keyPairs.stream()
            .map(pair -> new PrimaryKeyColumn(pair.getKey(), Objects.requireNonNull(primaryKeyValue(pair.getValue(), keyType, direction))))
            .collect(Collectors.toList());
        return new PrimaryKey(columns);
    }

    public static PrimaryKey primaryKey(List<Pair<String, Object>> keyPairs) {
        List<PrimaryKeyColumn> columns = keyPairs.stream()
            .map(pair -> new PrimaryKeyColumn(pair.getKey(), getPrimaryKeyValue(pair.getValue())))
            .collect(Collectors.toList());
        return new PrimaryKey(columns);
    }

    private static PrimaryKeyValue primaryKeyValue(Object value, RangeGetQuery.KeyType keyType, Direction direction) {
        switch (keyType) {
            case START:
                switch (direction) {
                    case FORWARD: {
                        PrimaryKeyValue primaryKeyValue = ColumnUtils.getPrimaryKeyValue(value);
                        if (primaryKeyValue == null) {
                            return PrimaryKeyValue.INF_MIN;
                        } else {
                            return primaryKeyValue;
                        }
                    }
                    case BACKWARD: {
                        PrimaryKeyValue primaryKeyValue = ColumnUtils.getPrimaryKeyValue(value);
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
                        PrimaryKeyValue primaryKeyValue = ColumnUtils.getPrimaryKeyValue(value);
                        if (primaryKeyValue == null) {
                            return PrimaryKeyValue.INF_MAX;
                        } else {
                            return primaryKeyValue;
                        }
                    }
                    case BACKWARD: {
                        PrimaryKeyValue primaryKeyValue = ColumnUtils.getPrimaryKeyValue(value);
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
