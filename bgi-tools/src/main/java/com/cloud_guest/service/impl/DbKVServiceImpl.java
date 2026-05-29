package com.cloud_guest.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud_guest.mapper.DbKVMapper;
import com.cloud_guest.entitys.pojo.DbKV;
import com.cloud_guest.service.DbKVService;
import org.springframework.stereotype.Service;

/**
 * @Author yan
 * @Date 2026/4/28 14:06:11
 * @Description
 */
@Service
public class DbKVServiceImpl extends ServiceImpl<DbKVMapper, DbKV> implements DbKVService {
}
