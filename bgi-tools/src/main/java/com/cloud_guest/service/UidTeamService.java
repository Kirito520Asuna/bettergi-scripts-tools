package com.cloud_guest.service;

import com.cloud_guest.constants.KeyConstants;
import com.cloud_guest.entitys.pojo.UidTeamConfig;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud_guest.mp.service.IServicePlus;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

/**
 * @Author yan
 * @Date 2026/9/1 17:31:10
 * @Description
 */
public interface UidTeamService extends IServicePlus<UidTeamConfig>, BaseService{
    @Override
    default String getSuffix() {
        return KeyConstants.mapping_uid_team_key;
    }

    UidTeamConfig searchOne(@NotBlank String uid, @NotBlank String type);

    List<UidTeamConfig> searchList(String id,String uid, String type);

    UidTeamConfig saveOrUpdateById(UidTeamConfig config);
}

