package org.alan.sie.main;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileInOutUtil {
    static int size = 1024;
    public static byte[] readFile(File file) throws IOException {
        InputStream is = new BufferedInputStream(new FileInputStream(file));
        ByteArrayOutputStream bs = new ByteArrayOutputStream(is.available());
        byte[] buffer = new byte[size];
        int len = 0;
        while ((len = is.read(buffer)) != -1) {
            bs.write(buffer, 0, len);
        }
        is.close();
        bs.close();
        return bs.toByteArray();
    }
}
