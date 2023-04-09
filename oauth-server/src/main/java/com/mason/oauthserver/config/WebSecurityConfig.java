package com.mason.oauthserver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;


@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    // 用户权限管理器，进行用户认证，配置用户签名服务和用户权限控制
    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }


    // 将BCryptPasswordEncoder对象注入Spring容器中，
    // SpringSecurity会使用PasswordEncoder自动密码校验
    @Bean
    public PasswordEncoder bcryptPasswordEncoder(){
        return new BCryptPasswordEncoder();
    }

    // 用户授权，配置拦截请求、请求验证、异常处理
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        //关闭csrf
        http.csrf().disable();

        // 解决跨域
        http.cors();

        // 开启Spring Security默认的表单登录
        http.formLogin();
                // 根据需求，自定义登录页面，注意不要拦截此Action
//              .loginPage("/login");

        // 设置认证的action
        http.authorizeRequests()
                // 不拦截以下action
                .antMatchers("/sso/rsa/publicKey").permitAll()

                // 处了上面的action，都需要鉴权认证
                .anyRequest().authenticated();
    }


}
