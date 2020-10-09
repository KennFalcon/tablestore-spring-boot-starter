package org.springframework.boot.autoconfigure.tablestore.utils;

import com.google.common.collect.Maps;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.boot.autoconfigure.tablestore.annotation.OtsColumn;
import org.springframework.boot.autoconfigure.tablestore.exception.OtsException;
import org.springframework.boot.autoconfigure.tablestore.model.DynamicColumn;
import org.springframework.boot.autoconfigure.tablestore.model.internal.FieldInfo;
import org.springframework.util.ReflectionUtils;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * @project: tablestore-spring-boot-starter
 * @description:
 * @author: Kenn
 * @create: 2019-12-12 16:12
 */
public class FieldUtils {

    public static Pair<Map<String, FieldInfo>, Boolean> getDeclaredFields(Class<?> clazz) {
        Map<String, FieldInfo> fieldInfoMap = Maps.newHashMap();
        boolean hasDynamicField = getDeclaredFields(clazz, fieldInfoMap);
        return Pair.of(fieldInfoMap, hasDynamicField);
    }

    public static <T> void invokeWrite(Field field, T data, Object value) {
        ReflectionUtils.setField(field, data, value);
    }

    public static <T> Object invokeRead(Field field, T data) {
        return ReflectionUtils.getField(field, data);
    }

    public static <T> Object invokeRead(String fieldName, T data) {
        Field field = ReflectionUtils.findField(data.getClass(), fieldName);
        if (field != null) {
            return ReflectionUtils.getField(field, data);
        }
        return null;
    }

    public static <T> Object invoke(Method method, T data, Object... args) {
        return ReflectionUtils.invokeMethod(method, data, args);
    }

    public static Method getMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        return ReflectionUtils.findMethod(clazz, methodName, parameterTypes);
    }

    private static Method getWriteMethod(Class<?> clazz, String fieldName) {
        PropertyDescriptor pd;
        try {
            pd = new PropertyDescriptor(fieldName, clazz);
        } catch (IntrospectionException e) {
            throw new OtsException("reflect error, class: %s, field: %s", e, clazz.getName(), fieldName);
        }
        return pd.getWriteMethod();
    }

    public static Method getReadMethod(Class<?> clazz, String fieldName) {
        PropertyDescriptor pd;
        try {
            pd = new PropertyDescriptor(fieldName, clazz);
        } catch (IntrospectionException e) {
            throw new OtsException("reflect error, class: %s, field: %s", e, clazz.getName(), fieldName);
        }
        return pd.getReadMethod();
    }

    private static boolean getDeclaredFields(Class<?> clazz, Map<String, FieldInfo> fieldMap) {
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            OtsColumn otsColumn = field.getAnnotation(OtsColumn.class);
            String columnName = ColumnUtils.getColumnName(field.getName(), otsColumn);
            if (!fieldMap.containsKey(field.getName())) {
                fieldMap.put(columnName, new FieldInfo(otsColumn, field));
            }
        }
        Class<?> superClass = clazz.getSuperclass();
        if (superClass.equals(Object.class)) {
            return false;
        }
        if (superClass.isAssignableFrom(DynamicColumn.class)) {
            return true;
        } else {
            return getDeclaredFields(superClass, fieldMap);
        }
    }
}
