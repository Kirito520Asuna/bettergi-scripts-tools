package com.cloud_guest.service;

import com.cloud_guest.vo.AutoPlanDomainVo;

import java.util.List;
import java.util.Map;

/**
 * @Author yan
 * @Date 2026/2/8 15:31:44
 * @Description
 */
public interface AutoPlanDomainService {
    boolean delList(List<String> ids);

    boolean save(String id, String json);

    List<AutoPlanDomainVo> find(String id);

    boolean saveAll(String json);

    List<Map<String, Object>> findAll();
}
