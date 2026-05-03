package com.cloud_guest.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloud_guest.pojo.BackupInfo;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author yan
 * @Date 2026/5/4 2:20:50
 * @Description
 */
@Mapper
public interface BackupMapper extends BaseMapper<BackupInfo> {
}
