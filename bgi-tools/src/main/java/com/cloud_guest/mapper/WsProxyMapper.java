package com.cloud_guest.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.cloud_guest.entitys.pojo.WsProxyAccessConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author yan
 * @Date 2026/4/28 13:45:40
 * @Description
 */
@Mapper
public interface WsProxyMapper extends BaseMapper<WsProxyAccessConfig> {
}
