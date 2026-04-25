package com.cloud_guest.config;

import cn.hutool.extra.spring.SpringUtil;
import com.cloud_guest.abs.ApiSignFilter;
import com.cloud_guest.abs.AuthFilter;
import com.cloud_guest.aop.bean.AbsBean;
import com.cloud_guest.filter.AuthJwtFilter;
import com.cloud_guest.filter.SignApiFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import jakarta.annotation.PostConstruct;

/**
 * @Author yan
 * @Date 2025/6/12 23:24:35
 * @Description Spring Boot 3.x 的安全配置
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig implements AbsBean {
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

    @Bean
    @ConditionalOnExpression("${auth.enabled:true}")
    public AuthFilter authFilter() {
        return new AuthJwtFilter();
    }
    @Bean
    @ConditionalOnExpression("${sign.api.sign-enable:false}")
    public ApiSignFilter apiSignFilter(){
        return new SignApiFilter();
    }
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http
                .cors(cors -> cors.configure(http))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth ->
                        auth.requestMatchers("/login", "/logout", "/static/**").permitAll()
                                .requestMatchers("/jwt/**").authenticated()
                                .anyRequest().permitAll()
                );

        if (securityAutoConfiguration != null) {
            http.formLogin(form -> form
                    .loginPage("/login")
                    .loginProcessingUrl("/login")
                    .permitAll()
            );
        }

        AuthFilter authFilter = SpringUtil.getBean(AuthFilter.class);
        http.addFilterBefore(authFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }


    @Bean
    //@Override
    public AuthenticationManager authenticationManagerBean(HttpSecurity http) throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class).build();
    }


}