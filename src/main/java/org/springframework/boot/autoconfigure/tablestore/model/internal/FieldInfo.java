package org.springframework.boot.autoconfigure.tablestore.model.internal;

import org.springframework.boot.autoconfigure.tablestore.annotation.OtsColumn;

import java.lang.reflect.Field;

/**
 * @project: tablestore-spring-boot-starter
 * @description:
 * @author: Kenn
 * @create: 2019-12-12 17:07
 */
public class FieldInfo {

    private final OtsColumn otsColumn;

    private final Field field;

    public FieldInfo(OtsColumn otsColumn, Field field) {
        this.otsColumn = otsColumn;
        this.field = field;
    }

    public OtsColumn otsColumn() {
        return otsColumn;
    }

    public Field field() {
        return field;
    }
}
