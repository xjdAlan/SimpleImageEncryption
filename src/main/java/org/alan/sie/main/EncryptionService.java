package org.alan.sie.main;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.alan.libs.util.HexConversionUtil;
import org.alan.libs.util.StringUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 加密处理类
 * 1.读取配置的文件夹，获取所有文件，判断文件类型（只需加密时判断，因为解密所有的文件都是txt）
 * 2.遍历文件并加密：
 *   (1)读取文件类型，加密后生成的文件的第一个字符表示文件类型（因为32进制，目前最多支持32中文件类型）
 *   (2)读取文件名称，将文件名称转为32进制，并添加在文件类型后，文件名结束后写一个32进制字符之外的字符做分割（该字符可在配置文件中配置）
 *   (3)读取文件内容，转为32进制，然后将文件内容做rot16加密
 *   (4)将rot16加密后的文件内容，按照配置在对应的位置上添加对应数量的32进制随机字符串
 *   (5)将上述内容转为字节数组并写入txt文件中，文件名称根据配置，如果保持原名，即使用原文件名命名，否则文件名为原文件名16位md5加密结果
 * Alan
 * 2015-1-26 下午9:23:19
 */
public class EncryptionService {
    
    private static Logger logger = LogManager.getLogger(EncryptionService.class);
    
    private String encoding = "utf-8";
    private int type;//0加密 1解密
    private Map<Integer, Integer> randLocAndNum;//随机字串位置和个数
    private List<String> fileType;//支持的文件类型（其他文件忽略）
    private String oldFilePath;//原始文件路径（待加（解）密文件所在路径）
    private String targetFilePath;//加（解）密后的文件存放路径
    private char fileNameSplitChar = '@';//文件名称之后的分隔符
    private int ifOldName;//是否原名（即生成的txt文件名称与原文件名称相同 1是0否）
    public int getType() {
        return type;
    }
    public void setType(int type) {
        this.type = type;
    }
    public Map<Integer, Integer> getRandLocAndNum() {
        return randLocAndNum;
    }
    public void setRandLocAndNum(Map<Integer, Integer> randLocAndNum) {
        this.randLocAndNum = randLocAndNum;
    }
    public List<String> getFileType() {
        return fileType;
    }
    public void setFileType(List<String> fileType) {
        this.fileType = fileType;
    }
    public String getOldFilePath() {
        return oldFilePath;
    }
    public void setOldFilePath(String oldFilePath) {
        this.oldFilePath = oldFilePath;
    }
    public String getTargetFilePath() {
        return targetFilePath;
    }
    public void setTargetFilePath(String targetFilePath) {
        this.targetFilePath = targetFilePath;
    }
    public char getFileNameSplitChar() {
        return fileNameSplitChar;
    }
    public void setFileNameSplitChar(char fileNameSplitChar) {
        this.fileNameSplitChar = fileNameSplitChar;
    }
    public int getIfOldName() {
        return ifOldName;
    }
    public void setIfOldName(int ifOldName) {
        this.ifOldName = ifOldName;
    }
    
    public static Logger getLogger() {
        return logger;
    }
    public static void setLogger(Logger logger) {
        EncryptionService.logger = logger;
    }
    public String getEncoding() {
        return encoding;
    }
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }
    public void start() {
        File file = new File(oldFilePath);
        if (type == 0) 
            iteratorFileEnc(file);
        else 
            iteratorFileDec(file);
    }
    
    /**
     * 遍历待加密文件
     * Alan
     * @param file
     * 2015-1-26 下午9:55:49
     */
    public void iteratorFileEnc(File file) {
        File[] files = file.listFiles(new FileFilter() {
            
            public boolean accept(File file) {
                if (file.isDirectory()) return true;
                if (fileType.contains(file.getName().substring(file.getName().lastIndexOf(".") + 1))) return true;
                return false;
            }
        });
        
        for (File f : files) {
            if (f.isDirectory()) {
                iteratorFileEnc(f);
                continue;
            }
            enc(f);
        }
    }
    
    /**
     * 加密一个文件
     * Alan
     * @param file
     * 2015-1-26 下午9:56:35
     */
    private void enc(File file) {
        try {
            StringBuffer encStr = new StringBuffer();
            String fileName = file.getName();
            String fileType = fileName.substring(fileName.lastIndexOf(".") + 1);
            FileTypeEnum fte = FileTypeEnum.getByName(fileType);
            if (fte == null) throw new RuntimeException("该文件类型不被支持：" + fileType);
            //文件类型
            encStr.append(HexConversionUtil.decimal2ThirtyTwo(fte.getValue()));
            fileName = fileName.substring(0, fileName.lastIndexOf("."));
            //文件名称
            encStr.append(HexConversionUtil.byte2ThirtyTwo(fileName.getBytes(encoding))).append(fileNameSplitChar);
            if (ifOldName == 0) 
                fileName = StringUtil.repWinFileSpeChar(HexConversionUtil.toMd5For16(fileName, encoding), "_");
            fileName += ".txt";
            byte[] fileByte = FileInOutUtil.readFile(file);
            //文件内容转32进制
            String content = HexConversionUtil.byte2ThirtyTwo(fileByte);
            //rot16
            content = HexConversionUtil.rot16(content);
            StringBuffer contentSb = new StringBuffer(content);
            //插入随机字符
            List<Integer> list = new ArrayList<Integer>(randLocAndNum.keySet());
            Collections.sort(list, new Comparator<Integer>() {
                @Override
                public int compare(Integer o1, Integer o2) {
                    return o2 - o1;
                }
            });
            String randStr;
            for (Integer key : list) {
                if (key > contentSb.length()){ 
                    logger.warn("加密文件{}时随机字串的位置过大", fileName);
                    continue;
                }
                randStr = HexConversionUtil.randThirtyTwoStr(randLocAndNum.get(key));
                contentSb.insert(key, randStr);
            }
            contentSb.insert(0, encStr);
            
            write2File(contentSb.toString(), fileName);
        } catch (IOException | NoSuchAlgorithmException e) {
            logger.error("文件加密出现异常{}", e);
        }
    }
    
    /**
     * 将加密后的文件内容写入文件
     * Alan
     * @param content
     * @param fileName
     * 2015-1-29 下午9:05:37
     * @throws IOException 
     * @throws UnsupportedEncodingException 
     */
    private void write2File(String content, String fileName) throws UnsupportedEncodingException, IOException {
        File file = new File(String.format("%s/%s", targetFilePath, fileName));
        FileInOutUtil.writeFile(file, content.getBytes(encoding), false);
    }
    
    
    
    /**
     * 遍历待解密文件
     * Alan
     * @param file
     * 2015-1-26 下午9:56:17
     */
    public void iteratorFileDec(File file) {
        File[] files = file.listFiles(new FileFilter() {
            
            public boolean accept(File file) {
                if (file.isDirectory()) return false;
                if ("txt".equals(file.getName().substring(file.getName().lastIndexOf(".") + 1))) return true;
                return false;
            }
        });
        
        for (File f : files) {
            dec(f);
        }
    }
    
    /**
     * 解密一个文件
     * Alan
     * @param file
     * 2015-1-26 下午9:56:25
     * @throws IOException 
     * @throws NoSuchAlgorithmException 
     */
    private void dec(File file) {
        try {
            String content = FileInOutUtil.readFileStr(file);
            //文件类型
            int ft = (int) HexConversionUtil.thirtyTwo2Decimal(content.substring(0, 1));
            String fileType = FileTypeEnum.getByValue(ft).getName();
            
            int fileNameSplitLoc = content.indexOf(fileNameSplitChar);
            //文件名称
            String fileName = new String(HexConversionUtil.thirtyTwo2Byte(content.substring(1, fileNameSplitLoc)), encoding);
            if (ifOldName == 0) 
                fileName = StringUtil.repWinFileSpeChar(HexConversionUtil.toMd5For16(fileName, encoding), "_");
            fileName += "." + fileType;
            
            //文件内容
            content = content.substring(fileNameSplitLoc + 1);
            
            StringBuffer contentSb = new StringBuffer(content);
            List<Integer> list = new ArrayList<Integer>(randLocAndNum.keySet());
            Collections.sort(list, new Comparator<Integer>() {
                @Override
                public int compare(Integer o1, Integer o2) {
                    return o1 - o2;
                }
            });
            for (Integer key : list) {
                if (key > contentSb.length()) {
                    logger.warn("解密文件{}时随机字串的位置过大", fileName);
                    continue;
                }
                contentSb.delete(key, key + randLocAndNum.get(key));
            }
            
            write2File(HexConversionUtil.thirtyTwo2Byte(HexConversionUtil.rot16(contentSb.toString())), fileName);
        } catch (IOException | NoSuchAlgorithmException e) {
            logger.error("文件解密出现异常{}", e);
        }
    }
    
    /**
     * 将解密后的内容写入文件
     * Alan
     * @param bytes
     * @param fileName
     * 2015-1-29 下午10:08:21
     * @throws IOException 
     */
    private void write2File(byte[] bytes, String fileName) throws IOException {
        File file = new File(String.format("%s/%s", targetFilePath, fileName));
        FileInOutUtil.writeFile(file, bytes);
    }
    
    
    public void test() {
        logger.info(type);
        logger.info("ok:");
    }
}
