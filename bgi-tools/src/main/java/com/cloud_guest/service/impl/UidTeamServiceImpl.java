package com.cloud_guest.service.impl;

import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.cloud_guest.exception.exceptions.GlobalException;
import com.cloud_guest.utils.LockUtil;
import com.cloud_guest.utils.StrUtils;
import com.cloud_guest.wrappers.lock.LockWrapper;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud_guest.mapper.UidTeamMapper;
import com.cloud_guest.entitys.pojo.UidTeamConfig;
import com.cloud_guest.service.UidTeamService;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @Author yan
 * @Date 2026/9/1 17:31:10
 * @Description
 */
@Service
public class UidTeamServiceImpl extends ServiceImpl<UidTeamMapper, UidTeamConfig> implements UidTeamService {
    @Override
    public UidTeamConfig searchOne(String uid, String type) {
        return getOne(
                lambdaQuery()
                        .eq(UidTeamConfig::getUid, uid)
                        .eq(UidTeamConfig::getTeamType, type));
    }

    @Override
    public List<UidTeamConfig> searchList(String uid, String type) {
        return list(
                lambdaQuery()
                        .eq(StrUtils.isNotBlank(uid), UidTeamConfig::getUid, uid)
                        .eq(StrUtils.isNotBlank(type), UidTeamConfig::getTeamType, type)
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UidTeamConfig saveOrUpdateById(UidTeamConfig config) {
        Long id = config.getId();
        if (id == null) {
            save(config);
        } else {
            // 更新时，需要根据 id 判断是否存在
            boolean exists = exists(lambdaQuery().eq(UidTeamConfig::getId, id));
            if (!exists) {
                throw new GlobalException("记录不存在或已被删除，id = " + id);
            }
            update(config, lambdaUpdate()
                    .eq(UidTeamConfig::getId, id)
                    .set(UidTeamConfig::getUid, config.getUid())
                    .set(UidTeamConfig::getTeam, config.getTeam())
                    .set(UidTeamConfig::getTeamType, config.getTeamType()));
        }
        return config;

    }
}

