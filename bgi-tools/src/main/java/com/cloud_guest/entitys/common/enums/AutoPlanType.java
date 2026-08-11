package com.cloud_guest.entitys.common.enums;

import com.cloud_guest.entitys.common.auto_plan.AutoBoss;
import com.cloud_guest.entitys.common.auto_plan.AutoFight;
import com.cloud_guest.entitys.common.auto_plan.AutoLeyLineOutcrop;
import com.cloud_guest.entitys.common.auto_plan.AutoStygianOnslaught;
import lombok.AllArgsConstructor;
import lombok.Getter;


/**
 * @Author yan
 * @Date 2026/8/11 12:32:39
 * @Description
 */
@AllArgsConstructor
@Getter
public enum AutoPlanType {
    DOMAIN("秘境", AutoFight.class), LEY_LINE_OUTCROP("地脉", AutoLeyLineOutcrop.class),
    BOSS("Boss", AutoBoss.class), SECRET("幽境", AutoStygianOnslaught.class)
    ;
    String key;
    Class<?> clazz;

    /**
     * 根据 clazz 获取对应的枚举实例
     * @param clazz 枚举的 clazz 值
     * @return 匹配的 AutoPlanType，若未找到则返回 null
     */
    public static AutoPlanType getByKey(Class<?> clazz) {
        if (clazz == null) {
            return null;
        }
        for (AutoPlanType type : values()) {
            if (type.getClazz().equals(clazz)) {
                return type;
            }
        }
        return null;
    }
    /**
     * 根据 key 获取对应的枚举实例
     * @param key 枚举的 key 值
     * @return 匹配的 AutoPlanType，若未找到则返回 null
     */
    public static AutoPlanType getByKey(String key) {
        if (key == null) {
            return null;
        }
        for (AutoPlanType type : values()) {
            if (type.getKey().equals(key)) {
                return type;
            }
        }
        return null;
    }
}
