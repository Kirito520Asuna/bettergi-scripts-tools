package com.cloud_guest.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud_guest.mapper.WsProxyMapper;
import com.cloud_guest.entitys.pojo.WsProxyAccessConfig;
import com.cloud_guest.service.WsProxyService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author yan
 * @Date 2026/3/22 16:19:45
 * @Description
 */
@Service
public class WsProxyServiceImpl extends ServiceImpl<WsProxyMapper, WsProxyAccessConfig> implements WsProxyService {

    @Override
    public List<String> findUidAll() {
        LambdaQueryWrapper<WsProxyAccessConfig> query = Wrappers.lambdaQuery(WsProxyAccessConfig.class);
        query.select(WsProxyAccessConfig::getUid);
        List<String> uidList = list(query).stream().map(WsProxyAccessConfig::getUid).toList();
        return uidList;
    }

    @Override
    public List<WsProxyAccessConfig> searchList(String uid) {
        return list(lambdaQueryWrapper().eq(StrUtil.isNotBlank(uid),WsProxyAccessConfig::getUid, uid));
    }
}
