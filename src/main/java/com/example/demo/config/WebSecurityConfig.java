package com.example.demo.config;

import com.example.demo.src.user.CustomOAuth2UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration      // 스프링 설정 클래스를 선언하는 어노테이션
@EnableWebSecurity  // SpringSecurity 사용을 위한 어노테이션, 기본적으로 CSRF 활성화
// SpringSecurity란, Spring기반의 애플리케이션의 보안(인증, 권한, 인가 등)을 담당하는 Spring 하위 프레임워크
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    CustomOAuth2UserService oAuth2UserService;

    /**
     * SpringSecurity 설정
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
//                .authorizeRequests()
//                .antMatchers("/").permitAll()
//                .antMatchers("/login").permitAll()
//                .antMatchers("/user").hasRole("USER")
//                .anyRequest().authenticated()
//                .and()
//                .exceptionHandling().accessDeniedPage("/accessDenied")
//                .and()
//                .logout().logoutUrl("/logout")
//                .logoutSuccessUrl("/").permitAll()
//                .and()
//                .oauth2Login().loginPage("/login")
//                .userInfoEndpoint()
//                .userService(oAuth2UserService)
                ;
    }
}
