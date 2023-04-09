package com.mason.oauthserver.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
public class UserDetailsServiceInfo implements UserDetailsService {

    // 自动加载WebSecurityConfig中的bcryptPasswordEncoder()方法的返回值BCryptPasswordEncoder对象
    @Autowired
    private PasswordEncoder bcryptPasswordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // (1)根据username查询数据库，找到账号和密码，下面类似查数据库
        if (!"admin".equals(username)){
            // 查不到数据返回null即可
            return null;
        }

        // (2) 对查询的密码进行加密，如果数据库的密码已经加密，此处不做。
        String password = this.bcryptPasswordEncoder.encode("123456");

        // (3) 生成User对象


        // 使用userdetails自带的UserDetails的对象User
        User user = new User("admin",password, AuthorityUtils.commaSeparatedStringToAuthorityList("admin, secretary"));
        return user;

    }

}
