package study.studyai.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

@Getter
public enum FileUploadEnum {

    AVATAR("头像","avatar"),
    TEXT("文本","text");

    private final String text;

    private final String value;

    FileUploadEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value 枚举值的 value
     * @return 枚举值
     */
    public static FileUploadEnum getEnumByValue(String value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (FileUploadEnum fileUploadEnum : FileUploadEnum.values()) {
            if (fileUploadEnum.value.equals(value)) {
                return fileUploadEnum;
            }
        }
        return null;
    }
}
