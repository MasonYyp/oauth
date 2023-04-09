package com.mason.oauthgateway.config;

import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.server.resource.BearerTokenAuthenticationToken;
import reactor.core.publisher.Mono;


/**
 * 验证token配置
 */

public class AuthManagerTokenConfig implements ReactiveAuthenticationManager {

    // 设置token存储
    private final TokenStore tokenStore;

    public AuthManagerTokenConfig(TokenStore tokenStore){
        this.tokenStore = tokenStore;
    }

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {

        // justOrEmpty的参数值不为null时，Mono序列才产生对应的元素
        Mono<Authentication> monoAuth = Mono.justOrEmpty(authentication);

        // 过滤BearerTokenAuthenticationToken实例对象
        Mono<Authentication> monoAuthInstance = monoAuth.filter(a -> a instanceof BearerTokenAuthenticationToken);

        // 将Authentication强转化为BearerTokenAuthenticationToken
        Mono<BearerTokenAuthenticationToken> monoBearer = monoAuthInstance.cast(BearerTokenAuthenticationToken.class);

        // 遍历Mono<BearerTokenAuthenticationToken>序列中的全部access token
        // “::”表示方法引用，即获取BearerTokenAuthenticationToken.getToken()中的值
        Mono<String> monoAccessToken = monoBearer.map(BearerTokenAuthenticationToken::getToken);

        // 遍历含有access token的monoAccessToken序列
        Mono<OAuth2Authentication> monoOAuth2Auth = monoAccessToken.flatMap((accessToken ->{
            System.out.println("Access Token is : " + accessToken);

            // 根据access token从数据库中查找OAuth2AccessToken
            OAuth2AccessToken oAuth2AccessToken = this.tokenStore.readAccessToken(accessToken);
            System.out.println(this.tokenStore.readRefreshToken(accessToken));

            // 从数据库中找不到OAuth2AccessToken
            if(oAuth2AccessToken == null){
                // 创建含有错误消息通知的序列
                // 注意：此错误信号会被GlobalErrorExceptionHandler捕获
                return Mono.error(new InvalidTokenException("Invalid access token"));
            }else if(oAuth2AccessToken.isExpired()){
                // access token 过期
                return Mono.error(new InvalidTokenException("Access token has expired"));
            }

            // 验证access token的权限
            OAuth2Authentication oAuth2Authentication = this.tokenStore.readAuthentication(accessToken);
            if(oAuth2Authentication == null){
                return Mono.error(new InvalidTokenException("Access token no authentication"));
            }else {
                return Mono.just(oAuth2Authentication);
            }
        }));

        // 强制转换
        return monoOAuth2Auth.cast(Authentication.class);

    }
}
