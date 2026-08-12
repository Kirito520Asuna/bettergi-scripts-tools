package com.cloud_guest.entitys.pojo;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cloud_guest.entitys.common.auto_plan.AutoBoss;
import com.cloud_guest.entitys.common.auto_plan.AutoDomain;
import com.cloud_guest.entitys.common.auto_plan.AutoLeyLineOutcrop;
import com.cloud_guest.entitys.common.auto_plan.AutoStygianOnslaught;
import com.cloud_guest.entitys.common.enums.AutoPlanType;
import com.cloud_guest.mp.pojo.BaseEntity;
import com.cloud_guest.entitys.vo.AutoPlanVo;
import com.cloud_guest.utils.EnumUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;


/**
 * @Author yan
 * @Date 2026/4/27 20:23:55
 * @Description
 */
@NoArgsConstructor
@Data
@AllArgsConstructor
@TableName(AutoPlanConfig.TABLE_NAME)
public class AutoPlanConfig extends BaseEntity {
    @TableId(value = COL_ID, type = IdType.ASSIGN_ID)
    private Long id;
    @TableField(value = COL_UID)
    private String uid;
    @TableField(value = COL_ORDER)
    private Integer orderSort;
    @TableField(value = COL_DAYS)
    private String days;
    @TableField(value = COL_DAY_NAME)
    private String dayName;
    @TableField(value = COL_SELECTED_TYPE)
    private String selectedType;
    @TableField(value = COL_RUN_TYPE)
    private String runType;
    @TableField(value = COL_ENABLE)
    private Boolean enable;
    @TableField(value = COL_CULTIVATE)
    private Boolean cultivate = Boolean.FALSE;
    @TableField(value = COL_RECORD)
    private Boolean record = Boolean.FALSE;
    @TableField(value = COL_JSON)
    private String json;

    //todo: 后续版本需要移除的字段-start
    @Deprecated
    @TableField(value = COL_AUTO_FIGHT)
    private String autoFight;
    @Deprecated
    @TableField(value = COL_AUTO_LEY_LINE_OUTCROP)
    private String autoLeyLineOutcrop;
    @Deprecated
    @TableField(value = COL_AUTO_STYGIAN_ONSLAUGHT)
    private String autoStygianOnslaught;
    @Deprecated
    @TableField(value = COL_AUTO_BOSS)
    private String autoBoss;
    //todo: 后续版本需要移除的字段-end


    public static final String TABLE_NAME = "auto_plan_config";
    public static final String COL_ID = "id";
    public static final String COL_UID = "uid";
    public static final String COL_ORDER = "col_order";
    public static final String COL_DAYS = "days";
    public static final String COL_DAY_NAME = "day_name";
    public static final String COL_SELECTED_TYPE = "selected_type";
    public static final String COL_RUN_TYPE = "run_type";
    public static final String COL_ENABLE = "enable";
    public static final String COL_CULTIVATE = "cultivate";
    public static final String COL_RECORD = "record";
    public static final String COL_JSON = "json";
    //todo: 后续版本需要移除的字段-start
    public static final String COL_AUTO_FIGHT = "auto_fight";
    public static final String COL_AUTO_LEY_LINE_OUTCROP = "auto_ley_line_outcrop";
    public static final String COL_AUTO_STYGIAN_ONSLAUGHT = "auto_stygian_onslaught";
    public static final String COL_AUTO_BOSS = "auto_boss";
    //todo: 后续版本需要移除的字段-end
    public static final String REMARK_COL_JSON = "JSON配置";
    //todo: 后续版本需要移除的字段-start
    public static final String REMARK_COL_AUTO_BOSS = "自动Boss配置";
    //todo: 后续版本需要移除的字段-end
    public static final String REMARK_COL_RECORD = "是否记录";
    public static final String REMARK_COL_CULTIVATE = "是培养计划";


    public AutoPlanVo toVo() {
        AutoPlanVo autoPlanVo = new AutoPlanVo();
        autoPlanVo
                .setId(id != null ? String.valueOf(id) : null)
                .setRunType(runType)
                .setCultivate(cultivate)
                .setSelectedType(selectedType)
                .setEnable(enable)
                .setRecord(record)
                .setDays(StrUtil.isBlank(days) ? new ArrayList<>() : Arrays.stream(days.split(",")).map(Integer::valueOf).toList())
                .setDayName(dayName)
                .setOrder(orderSort)
        ;

        AutoPlanType planType = EnumUtils.getEnumByPrivateFieldName(AutoPlanType.class, runType, "key");

        switch (planType) {
            case DOMAIN:
                autoPlanVo.setAutoDomain(parsePlanConfig(json, autoFight, AutoDomain.class));
                break;
            case LEY_LINE_OUTCROP:
                autoPlanVo.setAutoLeyLineOutcrop(parsePlanConfig(json, autoLeyLineOutcrop, AutoLeyLineOutcrop.class));
                break;
            case BOSS:
                autoPlanVo.setAutoBoss(parsePlanConfig(json, autoBoss, AutoBoss.class));
                break;
            case STYGIAN_ONSLAUGHT:
                autoPlanVo.setAutoStygianOnslaught(parsePlanConfig(json, autoStygianOnslaught, AutoStygianOnslaught.class));
                break;
            default:
                break;
        }

        return autoPlanVo;
    }


    // 在类中提取通用解析方法
    private <T> T parsePlanConfig(String json, String fallbackJson, Class<T> clazz) {
        // 优先使用新字段 json 反序列化
        if (StrUtil.isNotBlank(json)) {
            T result = JSONUtil.toBean(json, clazz);
            if (result != null) {
                return result;
            }
        }
        // 新字段解析失败（对象为 null 或字段为空），使用旧字段兜底
        if (StrUtil.isNotBlank(fallbackJson)) {
            return JSONUtil.toBean(fallbackJson, clazz);
        }
        return null;
    }
}
