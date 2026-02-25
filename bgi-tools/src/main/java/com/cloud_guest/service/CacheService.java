package com.cloud_guest.service;

import com.cloud_guest.domain.Cache;

import java.util.List;

/**
 * @Author yan
 * @Date 2026/2/6 16:01:28
 * @Description
 */
public interface CacheService {
    /**
     * 根据ID列表删除数据
     *
     * @param ids 要删除的数据ID列表
     * @return 删除操作是否成功执行
     * true - 删除成功
     * false - 删除失败
     */
    boolean removeList(List<String> ids);

    /**
     * 保存指定ID对应的JSON数据
     *
     * @param id   要保存数据的唯一标识符
     * @param json 要保存的JSON格式字符串
     * @return 保存操作是否成功，成功返回true，失败返回false
     */
    boolean save(String id, String json);

    boolean removeId(String key, String id);

    boolean saveId(String key, String id);

    /**
     * 根据指定的ID查找缓存中的数据
     * 这是一个泛型方法，用于查找类型为String的缓存数据
     *
     * @param id 要查找的缓存项的唯一标识符
     * @return 返回找到的缓存数据，类型为Cache<String>，如果没有找到则可能返回null
     */
    Cache<String> find(String id);

    String findById(String id);

    /**
     * 泛型方法，用于根据ID和类型查找对象
     *
     * @param <T>   泛型类型，表示返回对象的类型
     * @param id    要查找的对象的唯一标识符
     * @param clazz 目标对象的Class对象，用于指定类型
     * @return 找到的对象，类型为T
     */
    <T> T find(String id, Class<T> clazz);

    <T> List<T> findAll(String key, Class<T> clazz);
}
