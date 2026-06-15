package com.cloud_guest.utils;

/**
 * @Author yan
 * @Date 2026/3/23 14:10:07
 * @Description
 */
public class StrUtils extends cn.hutool.core.util.StrUtil {
    /**
     * 检查输入的字符串是否都不为空或空白
     *
     * @param str 可变数量的字符串参数
     * @return 如果所有字符串都不为空且不为空白，则返回true；否则返回false
     */
    public static boolean isNotBlank(String... str) {
        // 调用重载的isNotBlank方法，传入false作为第一个参数
        return isNotBlank(Boolean.FALSE, str);
    }

    /**
     * 判断给定的字符串是否不为空（至少有一个不为空或全部不为空）
     *
     * @param isOr 逻辑判断类型，true表示OR（或）关系，false表示AND（与）关系
     * @param str  可变参数，需要检查的字符串数组
     * @return 根据isOr参数返回不同的结果：
     * - 当isOr为true时，只要有一个字符串不为空就返回true
     * - 当isOr为false时，所有字符串都不为空才返回true
     */
    public static boolean isNotBlank(boolean isOr, String... str) {
        // 如果输入的字符串数组为null或长度为0，直接返回false
        if (str == null || str.length == 0) {
            return false;
        }
        // 根据isOr初始化flag的值
        // 当isOr为true时，初始值为false（OR关系的起点）
        // 当isOr为false时，初始值为true（AND关系的起点）
        //boolean flag = isOr ? false : true;
        boolean flag = !isOr ;
        // 遍历字符串数组
        for (String s : str) {
            // 根据isOr的值选择不同的逻辑判断
            if (isOr) {
                // OR关系：只要有一个不为空，整个表达式就为true
                flag = flag || isNotBlank(s);
            } else {
                // AND关系：必须所有都不为空，整个表达式才为true
                flag = flag && isNotBlank(s);
            }
        }
        // 返回最终的判断结果
        return flag;
    }

/*    public static void main(String[] args) {
        System.out.println(isNotBlank("1", null));
        System.out.println(isNotBlank("1", "2"));
        System.out.println(isNotBlank(true, "1", null));
    }*/
}
