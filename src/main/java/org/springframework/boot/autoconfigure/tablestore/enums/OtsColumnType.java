package org.springframework.boot.autoconfigure.tablestore.enums;

/**
 * Created on 2020/10/09
 *
 * @author Kenn
 */
public enum OtsColumnType {
    /**
     * UTF-8编码的字符串
     */
    STRING,
    /**
     * 64位的有符号整型
     */
    INTEGER,
    /**
     * 布尔类型
     */
    BOOLEAN,
    /**
     * 浮点数
     */
    DOUBLE,
    /**
     * 字节数组
     */
    BINARY,
    /**
     * 空类型
     */
    NONE
}
