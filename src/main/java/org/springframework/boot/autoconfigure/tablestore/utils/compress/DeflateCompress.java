package org.springframework.boot.autoconfigure.tablestore.utils.compress;

import com.alicloud.openservices.tablestore.core.utils.IOUtils;

import java.io.ByteArrayOutputStream;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * @project: tablestore-spring-boot-starter
 * @description:
 * @author: Kenn
 * @create: 2019-12-06 16:23
 */
public class DeflateCompress {

    public static byte[] compress(byte[] input) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Deflater compressor = new Deflater(1);
        try {
            compressor.setInput(input);
            compressor.finish();
            final byte[] buf = new byte[2048];
            while (!compressor.finished()) {
                int count = compressor.deflate(buf);
                bos.write(buf, 0, count);
            }
            return bos.toByteArray();
        } finally {
            compressor.end();
            IOUtils.safeClose(bos);
        }
    }

    public static byte[] uncompress(byte[] input) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Inflater decompressor = new Inflater();
        try {
            decompressor.setInput(input);
            final byte[] buf = new byte[2048];
            while (!decompressor.finished()) {
                int count = decompressor.inflate(buf);
                bos.write(buf, 0, count);
            }
            return bos.toByteArray();
        } catch (DataFormatException e) {
            e.printStackTrace();
            return null;
        } finally {
            decompressor.end();
            IOUtils.safeClose(bos);
        }
    }
}
