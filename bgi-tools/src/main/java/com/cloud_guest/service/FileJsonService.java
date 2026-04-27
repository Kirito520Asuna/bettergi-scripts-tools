package com.cloud_guest.service;

import com.cloud_guest.domain.Cache;

import java.util.List;

/**
 * @Author yan
 * @Date 2026/2/6 18:10:25
 * @Description 文件与JSON转换服务接口
 */
@Deprecated  // 标记该接口已过时，建议使用新的替代接口
public interface FileJsonService {
/**
 * FileJsonService 接口定义了文件与JSON数据转换的基本操作
 * 该接口提供保存文件、查找文件和删除文件的方法
 */
    String save(String filename, byte[] bytes);
    /**
     * 保存文件方法
     * @param filename 文件名
     * @param bytes 文件内容的字节数组
     * @return 返回保存后的文件标识符
     */
    Cache<String> find(String id);
    /**
     * 查找文件方法
     * @param id 文件的唯一标识符
     * @return 返回包含文件信息的缓存对象
     */

    boolean del(List<String> ids);
    /**
     * 批量删除文件方法
     * @param ids 要删除的文件ID列表
     * @return 返回删除操作是否成功
     */
}
