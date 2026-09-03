package com.cloud_guest.entitys.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cloud_guest.entitys.records.UidTeam;
import com.cloud_guest.mp.pojo.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

/**
 * @Author yan
 * @Date 2026/9/1 17:15:36
 * @Description
 */
@Schema
@Data
@Accessors(chain = true)
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = UidTeamConfig.TABLE_NAME)
public class UidTeamConfig extends BaseEntity {
    @TableId(value = COL_ID, type = IdType.ASSIGN_ID)
    @Schema(description = "ID")
    private Long id;

    @TableField(value = COL_UID)
    @Schema(description = "UID")
    private String uid;

    @TableField(value = COL_TEAM)
    @Schema(description = "队伍")
    private String team;

    @TableField(value = COL_TEAM_TYPE)
    @Schema(description = "类型")
    private String teamType;

    public static final String TABLE_NAME = "uid_team_config";
    public static final String COL_ID = "id";
    public static final String COL_UID = "uid";
    public static final String COL_TEAM = "team";
    public static final String COL_TEAM_TYPE = "team_type";
}