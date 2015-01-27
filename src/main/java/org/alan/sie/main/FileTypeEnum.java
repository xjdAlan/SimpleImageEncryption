package org.alan.sie.main;

public enum FileTypeEnum {
    TXT(1, "txt"),
    DOC(2, "doc"),
    DOCX(3, "docx"),
    XLS(4, "xls"),
    XLSX(5, "xlsx"),
    JPG(6, "jpg"),
    PNG(7, "png"),
    GIF(8, "gif"),
    BMP(9, "bmp"),
    ;
    
    private int value;
    private String name;
    public int getValue() {
        return value;
    }
    public void setValue(int value) {
        this.value = value;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    FileTypeEnum(int value, String name) {
        this.value = value;
        this.name = name;
    }
    
    public static FileTypeEnum getByName(String name) {
        for (FileTypeEnum fte : values()) {
            if (fte.getName().equals(name))
                return fte;
        }
        return null;
    }
    
    public static FileTypeEnum getByValue(int value) {
        for (FileTypeEnum fte : values()) {
            if (fte.getValue() == value)
                return fte;
        }
        return null;
    }
}
