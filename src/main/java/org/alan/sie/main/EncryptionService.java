package org.alan.sie.main;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

import org.alan.libs.util.HexConversionUtil;
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
            //文件名称
            encStr.append(HexConversionUtil.byte2ThirtyTwo(fileName.getBytes(encoding))).append(fileNameSplitChar);
            if (ifOldName == 0) 
                fileName = HexConversionUtil.toMd5For16(fileName, encoding);
            byte[] fileByte = FileInOutUtil.readFile(file);
            //文件内容转32进制
            String content = HexConversionUtil.byte2ThirtyTwo(fileByte);
            //rot16
            content = HexConversionUtil.rot16(content);
            //插入随机字符
        } catch (IOException | NoSuchAlgorithmException e) {
            logger.error("文件加密出现异常{}", e);
        }
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
     */
    private void dec(File file) {
        // TODO Auto-generated method stub
        
    }
    
    
    public void test() {
        logger.info(type);
        logger.info("ok:");
    }
}
