package com.cloud_guest.pojo;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cloud_guest.domain.auto_plan.AutoFight;
import com.cloud_guest.domain.auto_plan.AutoLeyLineOutcrop;
import com.cloud_guest.domain.auto_plan.AutoStygianOnslaught;
import com.cloud_guest.mp.pojo.BaseEntity;
import com.cloud_guest.vo.AutoPlanVo;
import jakarta.annotation.PostConstruct;
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
    @TableField(value = COL_AUTO_FIGHT)
    private String autoFight;
    @TableField(value = COL_AUTO_LEY_LINE_OUTCROP)
    private String autoLeyLineOutcrop;
    @TableField(value = COL_AUTO_STYGIAN_ONSLAUGHT)
    private String autoStygianOnslaught;



    public static final String TABLE_NAME = "auto_plan_config";
    public static final String COL_ID = "id";
    public static final String COL_UID = "uid";
    public static final String COL_ORDER = "col_order";
    public static final String COL_DAYS = "days";
    public static final String COL_DAY_NAME = "day_name";
    public static final String COL_SELECTED_TYPE = "selected_type";
    public static final String COL_RUN_TYPE = "run_type";
    public static final String COL_ENABLE = "enable";
    public static final String COL_AUTO_FIGHT = "auto_fight";
    public static final String COL_AUTO_LEY_LINE_OUTCROP = "auto_ley_line_outcrop";
    public static final String COL_AUTO_STYGIAN_ONSLAUGHT = "auto_stygian_onslaught";

    public AutoPlanVo toVo() {
        AutoPlanVo autoPlanVo = new AutoPlanVo();
        autoPlanVo.setRunType(runType)
                .setSelectedType(selectedType)
                .setEnable(enable)
                .setDays(StrUtil.isBlank(days) ? new ArrayList<>() : Arrays.stream(days.split(",")).map(Integer::valueOf).toList())
                .setDayName(dayName)
                .setOrder(orderSort)
                .setAutoFight(StrUtil.isBlank(autoFight) ? null : JSONUtil.toBean(autoFight, AutoFight.class))
                .setAutoLeyLineOutcrop(StrUtil.isBlank(autoLeyLineOutcrop) ? null : JSONUtil.toBean(autoLeyLineOutcrop, AutoLeyLineOutcrop.class))
                .setAutoStygianOnslaught(StrUtil.isBlank(autoStygianOnslaught) ? null : JSONUtil.toBean(autoStygianOnslaught, AutoStygianOnslaught.class));
        return autoPlanVo;
    }
}
