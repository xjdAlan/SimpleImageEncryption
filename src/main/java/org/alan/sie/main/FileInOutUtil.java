package org.alan.sie.main;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileInOutUtil {
    static int size = 1024;
    /**
     * 读取文件内容到byte数组
     * Alan
     * @param file
     * @return
     * @throws IOException
     * 2015-1-29 下午9:07:26
     */
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
    
    /**
     * 将byte数组写入文件
     * Alan
     * @param file
     * @param bytes
     * 2015-1-29 下午9:08:17
     * @throws IOException 
     */
    public static void writeFile(File file, byte[] bytes) throws IOException {
        OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
        out.write(bytes);
        out.close();
    }

    /**
     * 读取文件内容（用于解密时）
     * Alan
     * @param file
     * @return
     * 2015-1-29 下午9:39:31
     * @throws IOException 
     */
    public static String readFileStr(File file) throws IOException {
        StringBuffer result = new StringBuffer();
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
        byte[] buffer = new byte[size];
        int len = 0;
        while ((len = bis.read(buffer)) > 0) {
            result.append(new String(buffer, 0, len));
        }
        bis.close();
        return result.toString();
    }
}
