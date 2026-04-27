package com.cloud_guest.mp.config;

import com.cloud_guest.mp.abs.config.AbsMybatisPlusConfig;
import com.cloud_guest.mp.abs.handler.AbsEntityHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
//import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 *
 */
@EnableAspectJAutoProxy(exposeProxy = true)
@Configuration
@MapperScan(basePackages = {"com.cloud_guest.**.dao","com.cloud_guest.**.**.dao","com.cloud_guest.mapper","com.cloud_guest.**.mapper","com.cloud_guest.**.**.mapper"})
@ConditionalOnMissingBean(AbsMybatisPlusConfig.class)
public class MybatisPlusConfig implements AbsMybatisPlusConfig {
    /**
     * 3.4.0之前的版本用这个
     * @return
     */
   /* @Bean
    public PaginationInterceptor paginationInterceptor(){
        return  new PaginationInterceptor();
    }*/

    /**
     * 分页插件 3.5.X
     *
     * @author
     */
    //@Bean
    //@Override
    //public PaginationInnerInterceptor paginationInnerInterceptor() {
    //    return AbsMybatisPlusConfig.super.paginationInnerInterceptor();
    //}

    /**
     * 3.4.0之后提供的拦截器的配置方式
     *
     * @return
     */
    //@Bean
    //@Override
    //public MybatisPlusInterceptor mybatisPlusInterceptor() {
    //    return AbsMybatisPlusConfig.super.mybatisPlusInterceptor();
    //}

    /**
     * 防止全表更新与删除插件
     *
     * @return
     */
    //@Bean
    //@Override
    //public MybatisPlusInterceptor blockAttackInnerInterceptor() {
    //    return AbsMybatisPlusConfig.super.blockAttackInnerInterceptor();
    //}

    /**
     * 乐观锁支持
     *
     * @return
     */
    @Bean
    @Override
    public MybatisPlusInterceptor optimisticLockerInterceptor() {
        return AbsMybatisPlusConfig.super.optimisticLockerInterceptor();
    }


}
