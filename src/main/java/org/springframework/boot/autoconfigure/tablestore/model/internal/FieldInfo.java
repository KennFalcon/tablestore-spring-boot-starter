package org.springframework.boot.autoconfigure.tablestore.model.internal;

import org.springframework.boot.autoconfigure.tablestore.annotation.OtsColumn;

import java.lang.reflect.Field;

/**
 * Created on 2020/10/09
 *
 * @author Kenn
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
