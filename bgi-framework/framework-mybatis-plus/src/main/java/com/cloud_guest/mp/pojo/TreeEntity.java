package com.cloud_guest.mp.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Tree基类
 *
 * @author yan
 */
@Data
public abstract class TreeEntity extends BaseEntity {
    private static final long serialVersionUID = 1L;
    /**
     * 父菜单名称
     */
    @TableField(exist = false)
    private String parentName;

    /**
     * 父菜单ID
     */
    @TableField(exist = false)
    private Long parentId;

    /**
     * 显示顺序
     */
    @TableField(exist = false)
    private Integer orderNum;

    /**
     * 祖级列表
     */
    @TableField(exist = false)
    private String ancestors;

    /**
     * 子部门
     */
    @TableField(exist = false)
    private List<? extends TreeEntity> children = new ArrayList<>();

}
