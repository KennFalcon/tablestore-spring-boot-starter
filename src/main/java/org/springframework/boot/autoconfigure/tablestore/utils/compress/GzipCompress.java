package org.springframework.boot.autoconfigure.tablestore.utils.compress;

import com.alicloud.openservices.tablestore.core.utils.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @project: tablestore-spring-boot-starter
 * @description:
 * @author: Kenn
 * @create: 2019-12-06 16:35
 */
public class GzipCompress {

    public static byte[] compress(byte[] input) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GZIPOutputStream gzip = null;
        try {
            gzip = new GZIPOutputStream(out);
            gzip.write(input);
            return out.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            IOUtils.safeClose(gzip);
            IOUtils.safeClose(out);
        }
    }

    public static byte[] uncompress(byte[] input) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayInputStream in = new ByteArrayInputStream(input);
        GZIPInputStream unzip = null;
        try {
            unzip = new GZIPInputStream(in);
            byte[] buffer = new byte[2048];
            int n;
            while ((n = unzip.read(buffer)) >= 0) {
                out.write(buffer, 0, n);
            }
            return out.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            IOUtils.safeClose(unzip);
            IOUtils.safeClose(in);
            IOUtils.safeClose(out);
        }
    }
}
