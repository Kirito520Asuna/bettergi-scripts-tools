package com.cloud_guest.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud_guest.constants.KeyConstants;
import com.cloud_guest.entitys.pojo.DbKV;
import com.cloud_guest.mp.service.IServicePlus;

/**
 * @Author yan
 * @Date 2026/4/28 14:05:18
 * @Description
 */

public interface DbKVService extends IServicePlus<DbKV>, BaseService {
    default String getSuffix() {
        return KeyConstants.db_kv_key;
    }
}
