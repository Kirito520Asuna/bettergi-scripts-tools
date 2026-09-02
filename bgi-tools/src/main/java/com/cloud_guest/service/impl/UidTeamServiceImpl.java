package com.cloud_guest.service.impl;

import com.cloud_guest.exception.exceptions.GlobalException;
import com.cloud_guest.utils.StrUtils;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud_guest.mapper.UidTeamMapper;
import com.cloud_guest.entitys.pojo.UidTeamConfig;
import com.cloud_guest.service.UidTeamService;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
                lambdaQueryWrapper()
                        .eq(UidTeamConfig::getUid, uid)
                        .eq(UidTeamConfig::getTeamType, type));
    }

    @Override
    public List<UidTeamConfig> searchList(String id, String uid, String type) {
        return list(
                lambdaQueryWrapper()
                        .eq(StrUtils.isNotBlank(id), UidTeamConfig::getId, id)
                        .eq(StrUtils.isNotBlank(uid), UidTeamConfig::getUid, uid)
                        .eq(StrUtils.isNotBlank(type), UidTeamConfig::getTeamType, type)
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UidTeamConfig saveOrUpdateById(UidTeamConfig config) {
        boolean existsUidType = exists(lambdaQueryWrapper()
                .eq(UidTeamConfig::getUid, config.getUid())
                .eq(UidTeamConfig::getTeamType, config.getTeamType())
        );
        if (existsUidType) {
            throw new GlobalException("记录已存在，uid = " + config.getUid() + ", type = " + config.getTeamType());
        }
        Long id = config.getId();
        if (id == null) {
            save(config);
        } else {
            // 更新时，需要根据 id 判断是否存在
            boolean exists = exists(lambdaQueryWrapper().eq(UidTeamConfig::getId, id));
            if (!exists) {
                throw new GlobalException("记录不存在或已被删除，id = " + id);
            }
            update(config, lambdaUpdateWrapper()
                    .eq(UidTeamConfig::getId, id)
                    .set(UidTeamConfig::getUid, config.getUid())
                    .set(UidTeamConfig::getTeam, config.getTeam())
                    .set(UidTeamConfig::getTeamType, config.getTeamType()));
        }
        return config;

    }
}

