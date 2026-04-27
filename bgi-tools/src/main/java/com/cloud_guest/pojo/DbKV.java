package com.cloud_guest.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cloud_guest.mp.pojo.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author yan
 * @Date 2026/4/28 14:01:26
 * @Description
 */
@NoArgsConstructor
@Data
@AllArgsConstructor
@TableName(DbKV.TABLE_NAME)
public class DbKV extends BaseEntity {
    private static final long serialVersionUID = 1L;
    @TableId(value = COL_ID, type = IdType.ASSIGN_ID)
    private Long id;
    @TableField(value = COL_TYPE)
    private String type;
    @TableField(value = COL_KEY)
    private String key;
    @TableField(value = COL_VALUE)
    private String value;
    public static final String TABLE_NAME = "db_kv";
    public static final String COL_KEY = "key";
    public static final String COL_VALUE = "value";
    public static final String COL_ID = "id";
    public static final String COL_TYPE = "type";
}
