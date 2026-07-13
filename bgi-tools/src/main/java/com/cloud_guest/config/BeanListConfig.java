package com.cloud_guest.config;

import com.cloud_guest.abs.service.AbstractApiSaltService;
import com.cloud_guest.abs.service.AbstractKeyService;
import com.cloud_guest.abs.service.SimpleApiSaltService;
import com.cloud_guest.abs.service.SimpleKeyService;
import com.cloud_guest.mp.service.AuthUserService;
import com.cloud_guest.mp.service.impl.SimpleAuthUserService;
import com.cloud_guest.service.impl.AuthUserServiceImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * @Author yan
 * @Date 2026/3/31 19:53:19
 * @Description
 */
@Configuration
public class BeanListConfig {
    @Bean
    @ConditionalOnMissingBean(AbstractApiSaltService.class)
    public AbstractApiSaltService apiSaltService() {
        return new SimpleApiSaltService();
    }
    @Bean
    public AuthUserService authUserService() {
        return new AuthUserServiceImpl();
    }

    //@Bean
    //@ConditionalOnMissingBean(AbstractKeyService.class)
    //public AbstractKeyService keyService() {
    //    return new SimpleKeyService();
    //}

}
