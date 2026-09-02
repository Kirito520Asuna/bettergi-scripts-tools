package com.cloud_guest.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud_guest.constants.KeyConstants;
import com.cloud_guest.entitys.pojo.AutoPlanUidGlobalConfig;
import com.cloud_guest.mp.service.IServicePlus;

/**
 * @Author yan
 * @Date 2026/6/29 21:33:51
 * @Description
 */
public interface AutoPlanUidGlobalService extends IServicePlus<AutoPlanUidGlobalConfig>, BaseService {
    default String getSuffix() {
        return KeyConstants.auto_plan_global_key;
    }
}
