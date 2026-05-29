package com.cloud_guest.entitys.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cloud_guest.entitys.domain.UidInfo;
import com.cloud_guest.mp.pojo.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author yan
 * @Date 2026/4/28 18:39:25
 * @Description
 */
@NoArgsConstructor
@Data
@AllArgsConstructor
@TableName(UidInfoConfig.TABLE_NAME)
public class UidInfoConfig extends BaseEntity {
    @TableId(COL_UID)
    private String uid;
    @TableField(COL_AS)
    private String asName;
    public static final String TABLE_NAME = "uid_info_config";
    public static final String COL_UID = "uid";
    public static final String COL_AS = "col_as";
    public UidInfo toUidInfo() {
        return new UidInfo(uid, asName);
    }
}
