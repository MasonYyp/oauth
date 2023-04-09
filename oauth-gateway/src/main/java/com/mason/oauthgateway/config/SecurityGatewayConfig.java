package com.mason.oauthgateway.config;


import com.mason.oauthgateway.handler.AuthenticationEntryPoint;
import com.mason.oauthgateway.handler.AccessDeniedHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.redis.RedisTokenStore;
import org.springframework.security.oauth2.server.resource.web.server.ServerBearerTokenAuthenticationConverter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;

/**
 * 配置网关安全
 */

@Configuration
public class SecurityGatewayConfig {

    // 使用redis存储token
    @Autowired
    private RedisConnectionFactory redisConnectionFactory;
    @Bean
    public TokenStore tokenStore(){
        return new RedisTokenStore(this.redisConnectionFactory);
    }

    // 设置权限管理
    @Autowired
    private AuthManagerConfig authManagerConfig;
    @Autowired
    private AccessDeniedHandler accessDeniedHandler;
    @Autowired
    private AuthenticationEntryPoint authenticationEntryPoint;

    @Bean
    public SecurityWebFilterChain webFluxSecurityFilterChain(ServerHttpSecurity http) throws Exception{

        // token管理器
        ReactiveAuthenticationManager tokenAuthenticationManager = new AuthManagerTokenConfig(this.tokenStore());

        // 认证过滤器
        AuthenticationWebFilter authWebFilter = new AuthenticationWebFilter(tokenAuthenticationManager);
        authWebFilter.setServerAuthenticationConverter(new ServerBearerTokenAuthenticationConverter());

        http.httpBasic().disable()
                .csrf().disable()
                .authorizeExchange()
                // OPTIONS表示获取当前URL所支持的方法
                .pathMatchers(HttpMethod.OPTIONS).permitAll()
                // 配置权限管理
                .anyExchange().access(this.authManagerConfig)
                .and().exceptionHandling()
                // 处理未授权
                .accessDeniedHandler(this.accessDeniedHandler)
                // 处理未认证
                .authenticationEntryPoint(this.authenticationEntryPoint)
                .and()
                // oauth2认证过滤器
                .addFilterAt(authWebFilter, SecurityWebFiltersOrder.AUTHENTICATION);
        return http.build();
    }
}
