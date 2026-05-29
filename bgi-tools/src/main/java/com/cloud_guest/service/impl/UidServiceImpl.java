package com.cloud_guest.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud_guest.mapper.UidMapper;
import com.cloud_guest.entitys.pojo.UidInfoConfig;
import com.cloud_guest.service.UidService;
import org.springframework.stereotype.Service;

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
