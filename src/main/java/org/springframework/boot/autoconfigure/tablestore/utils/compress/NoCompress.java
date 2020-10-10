package org.springframework.boot.autoconfigure.tablestore.utils.compress;

/**
 * Created on 2020/10/09
 *
 * @author Kenn
 */
public class NoCompress {

    public static byte[] compress(byte[] input) {
        return input;
    }

    public static byte[] uncompress(byte[] input) {
        return input;
    }
}
