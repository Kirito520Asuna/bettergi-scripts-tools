package com.cloud_guest.entitys.pojo;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cloud_guest.entitys.records.UidGlobalInfo;
import com.cloud_guest.exception.exceptions.GlobalException;
import com.cloud_guest.mp.pojo.BaseEntity;
import com.cloud_guest.utils.object.ObjectUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author yan
 * @Date 2026/6/29 9:11:44
 * @Description
 */
@NoArgsConstructor
@Data
@AllArgsConstructor
@TableName(AutoPlanUidGlobalConfig.TABLE_NAME)
@Schema(description = "全局UID自动计划配置")
public class AutoPlanUidGlobalConfig extends BaseEntity {
    @TableId(value = COL_UID)
    @Schema(description = "UID")
    private String uid;
    @TableField(value = COL_CULTIVATE)
    @Schema(description = "是否启用培养计划")
    private Boolean cultivate;

    //加一个字段全局设置排序规则 存储前端方法 给前端调用,
    public static final String TABLE_NAME = "auto_plan_uid_global_config";
    public static final String COL_UID = "uid";
    public static final String COL_CULTIVATE = "cultivate";
}
