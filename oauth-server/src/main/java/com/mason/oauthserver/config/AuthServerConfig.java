package com.mason.oauthserver.config;

import com.mason.oauthserver.service.UserDetailsServiceInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.redis.RedisTokenStore;


@Configuration
@EnableAuthorizationServer
public class AuthServerConfig extends AuthorizationServerConfigurerAdapter {

    // 令牌端点的安全约束
    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
        security
                // 允许表单登录
                .allowFormAuthenticationForClients()
                // 公开token
                .tokenKeyAccess("permitAll()")
                // 全部允许验证token
                .checkTokenAccess("permitAll()");
    }


    // 使用redis存储token
    @Autowired
    private RedisConnectionFactory redisConnectionFactory;
    @Bean
    public TokenStore tokenStore(){
        return new RedisTokenStore(this.redisConnectionFactory);
    }

    // 自动创建UserDetailsServiceInfo实例
    @Autowired
    private UserDetailsServiceInfo userDetailsServiceInfo;
    // 自动加载WebSecurityConfig中的authenticationManagerBean()方法的返回值AuthenticationManager对象
    @Autowired
    private AuthenticationManager authenticationManager;
    // 令牌端点配置
    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        super.configure(endpoints);

        // 认证管理器,密码模式时使用
        endpoints.authenticationManager(this.authenticationManager)
                // 会自动调用UserDetailsServiceInfo下的loadUserByUsername()方法
                .userDetailsService(this.userDetailsServiceInfo)
                // 此处可根据需要，不设置
                .accessTokenConverter(this.jwtAccessTokenConverter())
                .tokenStore(this.tokenStore());
    }

    // 添加数字签名
    @Bean
    public JwtAccessTokenConverter jwtAccessTokenConverter() {
        JwtAccessTokenConverter jwtAccessTokenConverter = new JwtAccessTokenConverter();
        // 设置数字签名
        jwtAccessTokenConverter.setSigningKey("123456");
        return jwtAccessTokenConverter;
    }


    // 自动加载WebSecurityConfig中的bcryptPasswordEncoder()方法的返回值BCryptPasswordEncoder对象
    @Autowired
    private PasswordEncoder bcryptPasswordEncoder;
    // 客户端信息配置
    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.inMemory()
                // 客户端名称
                .withClient("web")
                // 客户端密钥
                .secret(this.bcryptPasswordEncoder.encode("123456"))
                // 设置授权模式为password
                .authorizedGrantTypes("password", "refresh_token")
                .scopes("all")
                // 设置token有效期
                .accessTokenValiditySeconds(20)
                // 设置刷新token的有效期
                .refreshTokenValiditySeconds(40)
                .autoApprove(true);

    }

}
