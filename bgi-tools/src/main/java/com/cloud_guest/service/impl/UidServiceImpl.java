package com.cloud_guest.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud_guest.domain.UidInfo;
import com.cloud_guest.mapper.UidMapper;
import com.cloud_guest.mp.abs.service.impl.MpServiceImpl;
import com.cloud_guest.pojo.UidInfoConfig;
import com.cloud_guest.service.CacheService;
import com.cloud_guest.service.UidService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author yan
 * @Date 2026/3/30 17:39:54
 * @Description
 */
@Service
public class UidServiceImpl extends ServiceImpl<UidMapper, UidInfoConfig> implements UidService {

    @Override
    public boolean removeList(List<String> ids) {
        return removeByIds(ids);
    }

    @Override
    public List<UidInfoConfig> findUidAll() {
        return list();
    }

    @Override
    public UidInfoConfig find(String uid) {
        UidInfoConfig uidInfo = getById(uid);
        return uidInfo;
    }
}
