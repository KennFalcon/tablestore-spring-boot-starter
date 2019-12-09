package org.springframework.boot.autoconfigure.tablestore.utils.compress;

/**
 * @project: tablestore-spring-boot-starter
 * @description:
 * @author: Kenn
 * @create: 2019-12-06 17:11
 */
public class NoCompress {

    public static byte[] compress(byte[] input) {
        return input;
    }

    public static byte[] uncompress(byte[] input) {
        return input;
    }
}
