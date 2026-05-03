package com.cloud_guest.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cloud_guest.mp.pojo.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @Author yan
 * @Date 2026/5/4 2:12:15
 * @Description
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(BackupInfo.TABLE_NAME)
public class BackupInfo extends BaseEntity {
    @Schema(description = "主键")
    @TableId(value = COL_ID, type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "备份名称")
    @TableField(value = COL_BACKUP_NAME)
    private String backupName;

    @Schema(description = "备份路径")
    @TableField(value = COL_BACKUP_PATH)
    private String backupPath;

    @Schema(description = "备份信息")
    @TableField(value = COL_BACKUP_JSON)
    private String backupJson;

    @Schema(description = "备份时间")
    @TableField(value = COL_BACKUP_TIME)
    private LocalDateTime backupTime;

    @Schema(description = "备份大小")
    @TableField(value = COL_BACKUP_SIZE)
    private Long backupSize;

    public static final String TABLE_NAME = "backup_info";
    public static final String COL_ID = "id";
    public static final String COL_BACKUP_NAME = "backup_name";
    public static final String COL_BACKUP_JSON = "backup_json";
    public static final String COL_BACKUP_PATH = "backup_path";
    public static final String COL_BACKUP_TIME = "backup_time";
    public static final String COL_BACKUP_SIZE = "backup_size";
}
