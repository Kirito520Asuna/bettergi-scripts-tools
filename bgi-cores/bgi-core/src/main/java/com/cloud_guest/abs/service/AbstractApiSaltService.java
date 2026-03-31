package com.cloud_guest.abs.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.cloud_guest.domain.api.SaltInfo;
import com.cloud_guest.properties.auth.ApiProperties;

import java.util.Collection;


/**
 * @Author yan
 * @Date 2024/5/20 0020 15:35
 * @Description
 */
public interface AbstractApiSaltService {
    /**
     * 获取saltList
     * @return
     */
    default Collection<SaltInfo> getSaltList() {
        ApiProperties bean = SpringUtil.getBean(ApiProperties.class);
        String apiSalt = bean.getSalt();
        SaltInfo saltInfo = new SaltInfo().setSalt(apiSalt).setServiceName("通用");
        return CollUtil.newArrayList(saltInfo);
    }
}
