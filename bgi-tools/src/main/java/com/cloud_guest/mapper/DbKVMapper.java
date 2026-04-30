package com.cloud_guest.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloud_guest.pojo.DbKV;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @Author yan
 * @Date 2026/4/28 14:07:31
 * @Description
 */
@Mapper
public interface DbKVMapper extends BaseMapper<DbKV> {
}
