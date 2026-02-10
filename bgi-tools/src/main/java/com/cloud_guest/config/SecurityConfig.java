package com.cloud_guest.config;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.cloud_guest.aop.bean.AbsBean;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.annotation.PostConstruct;

/**
 * @Author yan
 * @Date 2025/6/12 23:24:35
 * @Description Spring Boot 3.x 的安全配置
 */
@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter implements AbsBean {
    SecurityAutoConfiguration securityAutoConfiguration = null;

    @Override
    @PostConstruct
    public void init() {
        try {
            securityAutoConfiguration = SpringUtil.getBean(SecurityAutoConfiguration.class);
        } catch (Exception e) {
        }

    }

    /**
     * 密码加密器 Bean，用于加密存储密码
     *
     * @return BCryptPasswordEncoder
     */
    @Bean
    @ConditionalOnMissingBean(PasswordEncoder.class)
    public PasswordEncoder passwordEncoder() {
        log().debug("class:{},msg:PasswordEncoder", getAClassName());
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry expressionInterceptUrlRegistry =
                http.cors().and().csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                .antMatchers("/login", "/logout").permitAll();

            String jwtPath = "/jwt/**";
            String[] paths = jwtPath.split(",");
            expressionInterceptUrlRegistry
                    .antMatchers(paths).authenticated(); // 以 "/jwt" 开头的请求需要认证
        expressionInterceptUrlRegistry.anyRequest().permitAll(); // 其他请求允许访问

        if (securityAutoConfiguration != null) {
            http.formLogin(form -> form
                    .loginPage("/login") // 自定义登录页面
                    .loginProcessingUrl("/login") // 表单提交 URL
                    .permitAll()
            );
        }

    }



    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

}