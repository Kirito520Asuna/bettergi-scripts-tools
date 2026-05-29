package com.cloud_guest.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud_guest.entitys.domain.WsProxyAccess;
import com.cloud_guest.mapper.WsProxyMapper;
import com.cloud_guest.entitys.pojo.WsProxyAccessConfig;
import com.cloud_guest.service.WsProxyService;
import com.cloud_guest.utils.object.ObjectUtils;
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
    public WsProxyAccess find(String id) {
        WsProxyAccessConfig config = getById(id);
        WsProxyAccess wsProxyAccess = ObjectUtils.isEmpty(config) ? null : config.toWsProxyAccess();
        return wsProxyAccess;
    }

    @Override
    public List<WsProxyAccess> findAll() {
        return list().stream().map(WsProxyAccessConfig::toWsProxyAccess).toList();
    }
}
