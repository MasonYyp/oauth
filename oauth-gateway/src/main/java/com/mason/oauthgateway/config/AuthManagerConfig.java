package com.mason.oauthgateway.config;

import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.ReactiveAuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * 权限管理配置
 */

@Component
public class AuthManagerConfig  implements ReactiveAuthorizationManager<AuthorizationContext> {

    // 设置不验证的请求uri
    private final Set<String> permitAllUri = new ConcurrentSkipListSet<>();
    // 匹配路径
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    public AuthManagerConfig(){
        // 不验证下面请求uri的token
        permitAllUri.add("/");
        permitAllUri.add("/**/oauth/**");
    }

    @Override
    public Mono<AuthorizationDecision> check(Mono<Authentication> mono, AuthorizationContext authorizationContext) {
        // 当前请求和响应的上下文
        ServerWebExchange exchange = authorizationContext.getExchange();

        // 请求资源的path
        String requestPath = exchange.getRequest().getURI().getPath();

        // 判断请求路径
        boolean isPathPresent = this.permitAllUri.stream() // 将Set转化为流
                // 使用匿名函数查看路径是否存在
                .anyMatch(r -> antPathMatcher.match(r, requestPath));
        // 是否放行请求
        if (isPathPresent) {
            return Mono.just(new AuthorizationDecision(true));
        }

        // 验证权限
        return mono.map(
                auth -> new AuthorizationDecision(checkAuthorities(exchange, auth, requestPath))
        ).defaultIfEmpty(new AuthorizationDecision(false));

    }


    // 权限校验
    private boolean checkAuthorities(ServerWebExchange exchange, Authentication auth, String requestPath) {
        if (auth instanceof OAuth2Authentication) {
            OAuth2Authentication oauth2 = (OAuth2Authentication) auth;
            String clientId = oauth2.getOAuth2Request().getClientId();
            System.out.println("clientId is " + clientId);

            Object principal = auth.getPrincipal();
            System.out.println("用户信息: " + principal.toString());
        }
        return true;
    }
}
