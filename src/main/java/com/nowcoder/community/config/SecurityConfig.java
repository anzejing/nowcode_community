package com.nowcoder.community.config;

import com.nowcoder.community.controller.interceptor.LoginTicketInterceptor;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.*;
import jakarta.servlet.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.rememberme.InMemoryTokenRepositoryImpl;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import static org.springframework.security.config.Customizer.withDefaults;
@EnableWebSecurity//(debug = true)
@Configuration
public class SecurityConfig implements CommunityConstant{
    @Autowired
    private UserService userService;
    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private RedisTemplate redisTemplate;
    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http)  throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .addFilterBefore((request, response, chain) -> {
                    HttpServletRequest request1 = (HttpServletRequest) request;
                    HttpServletResponse response1 = (HttpServletResponse) response;
                    if(request1.getMethod().equals("POST") && request1.getServletPath().equals("/login")){
                        String kaptcha=null;
                        String kaptchaOwner = CookieUtil.getValue(request1,"kaptchaOwner");
                        if(StringUtils.isNotBlank(kaptchaOwner)){
                            String redisKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
                            kaptcha = (String) redisTemplate.opsForValue().get(redisKey);
                        }
                        String code = request1.getParameter("code");
                        if(StringUtils.isBlank(kaptcha) ||StringUtils.isBlank(code)||!kaptcha.equalsIgnoreCase(code)){
                            request.setAttribute("codeMsg", "验证码不正确");
                            request.getRequestDispatcher("/login").forward(request, response);
                            return;
                        }
                    }
                    // 让请求继续向下执行.
                    chain.doFilter(request, response);
                }, UsernamePasswordAuthenticationFilter.class)
                // 委托模式: ProviderManager将认证委托给AuthenticationProvider.
                .authenticationProvider(new AuthenticationProvider() {
                    @Override
                    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
                        String username = authentication.getName();
                        String password = (String) authentication.getCredentials();


                        User user = userService.findUserByName(username);
                        if (user == null) {
                            throw new UsernameNotFoundException("usernameMsg");
                        }
                        password = CommunityUtil.md5(password + user.getSalt());
                        if (!user.getPassword().equals(password)) {
                            throw new BadCredentialsException("passwordMsg");
                        }


                        // principal: 主要信息; credentials: 证书; authorities: 权限;
                        return new UsernamePasswordAuthenticationToken(user, user.getPassword(), user.getAuthorities());
                    }

                    // 当前的AuthenticationProvider支持哪种类型的认证.
                    @Override
                    public boolean supports(Class<?> aClass) {
                        // UsernamePasswordAuthenticationToken: Authentication接口的常用的实现类.
                        return UsernamePasswordAuthenticationToken.class.equals(aClass);
                    }
                })
                .formLogin((from)->from
                        .loginPage("/site/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .successHandler((request, response, authentication) -> {

                            String username = request.getParameter("username");
                            String password = request.getParameter("password");
                            boolean rememberme = false;
                            if(request.getParameter("rememberme")!=null
                                    &&!request.getParameter("rememberme").isEmpty()){
                                rememberme=true;
                            }
                            int expiredSeconds = rememberme?REMEMBER_EXPIRED_SECONDS:DEFAULT_EXPIRED_SECONDS;
                            Map<String,Object> map= userService.login(username,password,expiredSeconds);
                            if(map.containsKey("ticket")){
                                Cookie cookie =new Cookie("ticket",map.get("ticket").toString());
                                cookie.setPath(contextPath);
                                cookie.setMaxAge(expiredSeconds);
                                response.addCookie(cookie);
                            }
                            response.sendRedirect(request.getContextPath() + "/index");
                        })
                        .failureHandler(new AuthenticationFailureHandler() {
                            @Override
                            public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
                                if(exception.getMessage().equals("usernameMsg")){
                                    request.setAttribute("usernameMsg",exception.getMessage());
                                }else {
                                    request.setAttribute("passwordMsg",exception.getMessage());
                                }
                                request.getRequestDispatcher("/login").forward(request,response);
                            }
                        })
                        .permitAll())

                .authorizeHttpRequests((authorize) -> authorize
                        .requestMatchers(
                                "/user/setting",
                                "/user/changePassword",
                                "/user/upload",
                                "/discuss/add",
                                "/discuss/add/**",
                                "/comment/add/**",
                                "/letter/**",
                                "/notice/**",
                                "/like",
                                "/follow",
                                "/unfollow"
                        ).hasAnyAuthority(AUTHORITY_USER,AUTHORITY_ADMIN, AUTHORITY_MODERATOR)
                        .requestMatchers(
                                "/discuss/top",
                                "/discuss/wonderful"
                        ).hasAnyAuthority(AUTHORITY_MODERATOR)
                        .requestMatchers(
                                "/discuss/delete"
                        ).hasAnyAuthority(AUTHORITY_ADMIN)
                        .anyRequest().permitAll()
                )

                //记住我
                .rememberMe(remember->remember
                        .tokenRepository(new InMemoryTokenRepositoryImpl())
                        .tokenValiditySeconds(REMEMBER_EXPIRED_SECONDS)
                        .userDetailsService(userService))
                // 异常处理配置
                .exceptionHandling(exceptionHandling ->
                        exceptionHandling
                                //未登录
                                .authenticationEntryPoint((request, response, authException) -> {

                                    String xRequestedWith = request.getHeader("x-requested-with");
                                    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                                    System.out.println(Thread.currentThread().getName()+" parent:"+Thread.currentThread().getThreadGroup().getParent()+" ss");
                                    if (authentication instanceof UsernamePasswordAuthenticationToken) {
                                        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
                                        for (GrantedAuthority authority : authorities) {
                                            String authorityString = authority.getAuthority();
                                            System.out.println("authorityString" + authorityString);
                                            System.out.println("isAuthenticated "+authentication.isAuthenticated());
                                        }
                                    }

                                    if ("XMLHttpRequest".equals(xRequestedWith)){
                                        response.setContentType("application/plain;charset=utf-8");
                                        PrintWriter writer = response.getWriter();
                                        writer.write(CommunityUtil.getJSONString(403,"你还没有登录！"));
                                    }else {
                                        response.sendRedirect(request.getContextPath()+"/login");
                                    }

                                })
                                //权限不足
                                .accessDeniedHandler((request, response, exception) -> {
                                    String xRequestedWith = request.getHeader("x-requested-with");
                                    if ("XMLHttpRequest".equals(xRequestedWith)){
                                        response.setContentType("application/plain;charset=utf-8");
                                        PrintWriter writer = response.getWriter();
                                        writer.write(CommunityUtil.getJSONString(403,"没有访问权限！"));
                                    }else {
                                        response.sendRedirect(request.getContextPath()+"/denied");
                                    }
                                })
                )

                .logout(logoutCustomizer -> logoutCustomizer
                                //注销登录请求url
                                .logoutUrl("/securityLogout")
                );
        return http.build();
    }


    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        //忽略静态资源访问

        return (web) -> web.ignoring().requestMatchers("/resources/**");
    }





}
