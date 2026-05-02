package com.yupi.yuaicodemother.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

/**
 * 组件分类枚举
 */
@Getter
public enum ComponentCategoryEnum {

    LAYOUT("布局组件", "layout"),
    BASIC("基础组件", "basic"),
    BUSINESS("业务组件", "business");

    private final String text;
    private final String value;

    ComponentCategoryEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据 value 获取枚举
     */
    public static ComponentCategoryEnum getEnumByValue(String value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (ComponentCategoryEnum anEnum : ComponentCategoryEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }
}
