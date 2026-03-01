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

    /**
     * 根据键和ID移除对应的元素
     *
     * @param key 主键，用于标识要操作的数据集合
     * @param id  要移除的具体元素的ID
     * @return 如果成功移除则返回true，如果未找到对应元素或移除失败则返回false
     */
    boolean removeId(String key, String id);

    /**
     * 保存ID的方法
     *
     * @param key 用于标识ID的键值
     * @param id  需要保存的ID字符串
     * @return 保存操作是否成功
     */
    boolean saveId(String key, String id);

    /**
     * 根据指定的ID查找缓存中的数据
     * 这是一个泛型方法，用于查找类型为String的缓存数据
     *
     * @param id 要查找的缓存项的唯一标识符
     * @return 返回找到的缓存数据，类型为Cache<String>，如果没有找到则可能返回null
     */
    Cache<String> find(String id);

    /**
     * 根据键查找对应的值
     *
     * @param key 要查找的键，不能为null
     * @return 找到的对应值，如果未找到则可能返回null
     * @throws IllegalArgumentException 如果传入的key为null
     */
    String findValueByKey(String key);

    /**
     * 根据ID查找对应的字符串
     *
     * @param id 要查找的唯一标识符
     * @return 找到的字符串，如果未找到则可能返回null
     */
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

    /**
     * 根据指定的键和类型查找所有匹配的元素
     * 这是一个泛型方法，可以用于任何类型的列表查询
     *
     * @param <T>   要查询的元素的类型
     * @param key   查询的键值，用于筛选条件
     * @param clazz 要查询的元素的类型Class对象
     * @return 返回匹配条件的元素列表，类型为List<T>
     */
    <T> List<T> findAll(String key, Class<T> clazz);
}
