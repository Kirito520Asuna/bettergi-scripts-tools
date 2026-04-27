package com.cloud_guest.mp.pojo;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Entity基类
 *
 * @author yan
 */

@Data
@Slf4j
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public abstract class BaseEntity extends SearchEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 创建者
     */
    @Schema(description = "创建者")
    //@TableField(value = "`create_by`", fill = FieldFill.INSERT)
    @TableField(value = COL_CREATE_BY, fill = FieldFill.INSERT)
    private String createBy;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = DatePattern.NORM_DATETIME_PATTERN)
    //@TableField(value = "`create_time`", fill = FieldFill.INSERT)
    @TableField(value = COL_CREATE_TIME, fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新者
     */
    @Schema(description = "更新者")
    //@TableField(value = "`update_by`", fill = FieldFill.UPDATE)
    @TableField(value = COL_UPDATE_BY, fill = FieldFill.UPDATE)
    private String updateBy;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    @DateTimeFormat(pattern = DatePattern.NORM_DATETIME_PATTERN)
    //@TableField(value = "`update_time`", fill = FieldFill.INSERT_UPDATE)
    @TableField(value = COL_UPDATE_TIME, fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 备注
     */
    @Schema(description = "备注")
    //@TableField(value = "`remark`")
    @TableField(value = COL_REMARK)
    private String remark;

    public static final String COL_CREATE_BY = "create_by";
    public static final String COL_CREATE_TIME = "create_time";
    public static final String COL_UPDATE_BY = "update_by";
    public static final String COL_UPDATE_TIME = "update_time";
    public static final String COL_REMARK = "remark";

    /**
     * 是否开启扩展构建 (默认关闭)
     *
     * @return
     */
    private boolean openExtendedBuilds() {
        return false;
    }

    /**
     * 扩展构建
     *
     * @param buildMap
     */
    protected void buildEntity(Map<String, Object> buildMap) {
        log.warn("buildEntity is not implemented buildMap:{}", buildMap);
    }

    /**
     * 基础构建
     *
     * @param obj
     * @param <T>
     */
    protected <T extends BaseEntity> T buildEntity(T obj) {
        boolean openExtendedBuilds = openExtendedBuilds();
        if (openExtendedBuilds) {
            log.debug("execute buildEntity method");
            buildEntity(BeanUtil.beanToMap(obj));
        } else {
            log.warn("execute BaseEntity:{}", JSONUtil.toJsonStr(obj));
        }
        return obj;
    }

    /**
     * 一键调用构建
     */
    public <T extends BaseEntity> T buildEntity() {
        return (T) buildEntity(this);
    }

}
