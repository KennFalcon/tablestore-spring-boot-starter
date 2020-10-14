package org.springframework.boot.autoconfigure.tablestore.utils.compress;

import org.xerial.snappy.Snappy;

import java.io.IOException;

/**
 * Created on 2020/10/09
 *
 * @author Kenn
 */
public class SnappyCompress {

    public static byte[] compress(byte[] input) {
        try {
            return Snappy.compress(input);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static byte[] uncompress(byte[] input) {
        try {
            return Snappy.uncompress(input);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
