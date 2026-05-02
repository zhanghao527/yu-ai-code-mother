package com.yupi.yuaicodemother.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

/**
 * 编辑模式枚举
 */
@Getter
public enum EditorModeEnum {

    AI_CHAT("AI 对话模式", "ai_chat"),
    LOWCODE("低代码拖拽模式", "lowcode");

    private final String text;
    private final String value;

    EditorModeEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据 value 获取枚举
     */
    public static EditorModeEnum getEnumByValue(String value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (EditorModeEnum anEnum : EditorModeEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }
}
