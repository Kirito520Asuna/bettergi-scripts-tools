package com.cloud_guest.entitys.domain;

import com.cloud_guest.entitys.pojo.BackupInfo;
import com.cloud_guest.utils.StrUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @Author yan
 * @Date 2026/5/4 23:31:18
 * @Description
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BackUp {
    @Schema(description = "主键")
    private String id;

    @Schema(description = "备份名称")
    private String backupName;

    @Schema(description = "备份路径")
    private String backupPath;

    @Schema(description = "备份信息")
    private String backupJson;

    @Schema(description = "备份时间")
    private LocalDateTime backupTime;

    @Schema(description = "备份大小")
    private Long backupSize;

    public BackupInfo toInfo() {
        return new BackupInfo(StrUtils.isNotBlank(id) ? Long.valueOf(id) : null, backupName, backupPath, backupJson, backupTime, backupSize);
    }
}
