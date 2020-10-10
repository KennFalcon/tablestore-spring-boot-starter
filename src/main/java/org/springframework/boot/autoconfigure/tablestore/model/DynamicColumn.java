package org.springframework.boot.autoconfigure.tablestore.model;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import com.alicloud.openservices.tablestore.model.ColumnType;
import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;
import org.springframework.boot.autoconfigure.tablestore.exception.OtsException;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * Created on 2020/10/09
 *
 * @author Kenn
 */
public abstract class DynamicColumn {

    @JSONField(serialize = false, deserialize = false)
    private Map<String, Object> dynamicColumns = Maps.newHashMap();

    @JSONField(serialize = false, deserialize = false)
    private final Map<String, ColumnType> columnTypes = Maps.newHashMap();

    public void addDynamicColumn(String name, Object value) {
        if (value instanceof byte[]) {
            columnTypes.put(name, ColumnType.BINARY);
        } else if (value instanceof Short) {
            columnTypes.put(name, ColumnType.INTEGER);
        } else if (value instanceof Integer) {
            columnTypes.put(name, ColumnType.INTEGER);
        } else if (value instanceof Long) {
            columnTypes.put(name, ColumnType.INTEGER);
        } else if (value instanceof Float) {
            columnTypes.put(name, ColumnType.DOUBLE);
        } else if (value instanceof Double) {
            columnTypes.put(name, ColumnType.DOUBLE);
        } else if (value instanceof Boolean) {
            columnTypes.put(name, ColumnType.BOOLEAN);
        } else if (value instanceof String) {
            columnTypes.put(name, ColumnType.STRING);
        } else {
            columnTypes.put(name, ColumnType.STRING);
        }
        dynamicColumns.put(name, value);
    }

    public void addDynamicColumn(String name, ColumnType type, Object value) {
        columnTypes.put(name, type);
        dynamicColumns.put(name, value);
    }

    public boolean containsDynamicColumn(String name) {
        return dynamicColumns.containsKey(name);
    }

    public Object getDynamicColumn(String name) {
        return dynamicColumns.getOrDefault(name, null);
    }

    public byte[] getDynamicColumnAsByteArray(String name) {
        if (!dynamicColumns.containsKey(name)) {
            throw new OtsException("dynamic column [%s] is absent", name);
        }
        Object value = dynamicColumns.get(name);
        if (value == null) {
            throw new OtsException("dynamic column [%s] is null, can't convert to byte array", name);
        }
        ColumnType type = columnTypes.get(name);
        switch (type) {
            case BINARY: {
                return (byte[])value;
            }
            case STRING: {
                return ((String)value).getBytes(StandardCharsets.UTF_8);
            }
            default: {
                throw new OtsException("dynamic column [%s] is not supported convert to byte array", name);
            }
        }
    }

    public short getDynamicColumnAsShort(String name) {
        Object value = dynamicColumns.get(name);
        if (value == null) {
            throw new OtsException("dynamic column [%s] is null, can't convert to short", name);
        }
        ColumnType type = columnTypes.get(name);
        switch (type) {
            case INTEGER: {
                return ((Long)value).shortValue();
            }
            case DOUBLE: {
                return ((Double)value).shortValue();
            }
            case STRING: {
                return Short.parseShort((String)value);
            }
            default: {
                throw new OtsException("dynamic column [%s] is not supported convert to short", name);
            }
        }
    }

    public int getDynamicColumnAsInt(String name) {
        if (!dynamicColumns.containsKey(name)) {
            throw new OtsException("dynamic column [%s] is absent", name);
        }
        Object value = dynamicColumns.get(name);
        if (value == null) {
            throw new OtsException("dynamic column [%s] is null, can't convert to int", name);
        }
        ColumnType type = columnTypes.get(name);
        switch (type) {
            case INTEGER: {
                return ((Long)value).intValue();
            }
            case DOUBLE: {
                return ((Double)value).intValue();
            }
            case STRING: {
                return Integer.parseInt((String)value);
            }
            default: {
                throw new OtsException("dynamic column [%s] is not supported convert to int", name);
            }
        }
    }

    public long getDynamicColumnAsLong(String name) {
        if (!dynamicColumns.containsKey(name)) {
            throw new OtsException("dynamic column [%s] is absent", name);
        }
        Object value = dynamicColumns.get(name);
        if (value == null) {
            throw new OtsException("dynamic column [%s] is null, can't convert to long", name);
        }
        ColumnType type = columnTypes.get(name);
        switch (type) {
            case INTEGER: {
                return (Long)value;
            }
            case DOUBLE: {
                return ((Double)value).longValue();
            }
            case STRING: {
                return Long.parseLong((String)value);
            }
            default: {
                throw new OtsException("dynamic column [%s] is not supported convert to long", name);
            }
        }
    }

    public float getDynamicColumnAsFloat(String name) {
        if (!dynamicColumns.containsKey(name)) {
            throw new OtsException("dynamic column [%s] is absent", name);
        }
        Object value = dynamicColumns.get(name);
        if (value == null) {
            throw new OtsException("dynamic column [%s] is null, can't convert to float", name);
        }
        ColumnType type = columnTypes.get(name);
        switch (type) {
            case INTEGER: {
                return ((Long)value).floatValue();
            }
            case DOUBLE: {
                return ((Double)value).floatValue();
            }
            case STRING: {
                return Float.parseFloat((String)value);
            }
            default: {
                throw new OtsException("dynamic column [%s] is not supported convert to float", name);
            }
        }
    }

    public double getDynamicColumnAsDouble(String name) {
        if (!dynamicColumns.containsKey(name)) {
            throw new OtsException("dynamic column [%s] is absent", name);
        }
        Object value = dynamicColumns.get(name);
        if (value == null) {
            throw new OtsException("dynamic column [%s] is null, can't convert to double", name);
        }
        ColumnType type = columnTypes.get(name);
        switch (type) {
            case INTEGER: {
                return ((Long)value).doubleValue();
            }
            case DOUBLE: {
                return (Double)value;
            }
            case STRING: {
                return Double.parseDouble((String)value);
            }
            default: {
                throw new OtsException("dynamic column [%s] is not supported convert to double", name);
            }
        }
    }

    public boolean getDynamicColumnAsBoolean(String name) {
        if (!dynamicColumns.containsKey(name)) {
            throw new OtsException("dynamic column [%s] is absent", name);
        }
        Object value = dynamicColumns.get(name);
        if (value == null) {
            throw new OtsException("dynamic column [%s] is null, can't convert to boolean", name);
        }
        ColumnType type = columnTypes.get(name);
        switch (type) {
            case BOOLEAN: {
                return (boolean)value;
            }
            case STRING: {
                return Boolean.parseBoolean((String)value);
            }
            default: {
                throw new OtsException("dynamic column [%s] is not supported convert to boolean", name);
            }
        }
    }

    public String getDynamicColumnAsString(String name) {
        if (!dynamicColumns.containsKey(name)) {
            throw new OtsException("dynamic column [%s] is absent", name);
        }
        Object value = dynamicColumns.get(name);
        if (value == null) {
            throw new OtsException("dynamic column [%s] is null, can't convert to string", name);
        }
        ColumnType type = columnTypes.get(name);
        switch (type) {
            case BINARY: {
                return new String((byte[])value, StandardCharsets.UTF_8);
            }
            case INTEGER: {
                return String.valueOf((int)value);
            }
            case DOUBLE: {
                return String.valueOf((double)value);
            }
            case BOOLEAN: {
                return String.valueOf((boolean)value);
            }
            case STRING: {
                return (String)value;
            }
            default: {
                throw new OtsException("dynamic column [%s] is not supported convert to string", name);
            }
        }
    }

    public <T> List<T> getDynamicColumnAsList(String name, Class<T> clazz) {
        if (!dynamicColumns.containsKey(name)) {
            throw new OtsException("dynamic column [%s] is absent", name);
        }
        Object value = dynamicColumns.get(name);
        if (value == null) {
            throw new OtsException("dynamic column [%s] is null, can't convert to list", name);
        }
        ColumnType type = columnTypes.get(name);
        if (type == ColumnType.STRING) {
            return JSON.parseArray((String)value, clazz);
        }
        throw new OtsException("dynamic column [%s] is not supported convert to list", name);
    }

    public <T, U> Map<T, U> getDynamicColumnAsMap(String name, TypeToken<Map<T, U>> typeToken) {
        if (!dynamicColumns.containsKey(name)) {
            throw new OtsException("dynamic column [%s] is absent", name);
        }
        Object value = dynamicColumns.get(name);
        if (value == null) {
            throw new OtsException("dynamic column [%s] is null, can't convert to map", name);
        }
        ColumnType type = columnTypes.get(name);
        if (type == ColumnType.STRING) {
            return JSON.parseObject((String)value, typeToken.getType());
        }
        throw new OtsException("dynamic column [%s] is not supported convert to map", name);
    }

    public <T> T getDynamicColumn(String name, Class<T> clazz) {
        if (!dynamicColumns.containsKey(name)) {
            throw new OtsException("dynamic column [%s] is absent", name);
        }
        Object value = dynamicColumns.get(name);
        if (value == null) {
            throw new OtsException("dynamic column [%s] is null, can't convert to %s", name, clazz.getName());
        }
        ColumnType type = columnTypes.get(name);
        if (type == ColumnType.STRING) {
            return JSON.parseObject((String)value, clazz);
        }
        throw new OtsException("dynamic column [%s] is not supported convert to %s", name, clazz.getName());
    }

    public Map<String, Object> getDynamicColumns() {
        return dynamicColumns;
    }

    public void setDynamicColumns(Map<String, Object> dynamicColumns) {
        this.dynamicColumns = dynamicColumns;
    }
}
