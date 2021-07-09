package tech.iloveit.luehningcli.authority.auth2;

import tech.iloveit.luehningcli.authority.config.CloseAuthorityEvironment;
import tech.iloveit.luehningcli.authority.config.CustomDaoAuthenticationProvider;
import tech.iloveit.luehningcli.authority.filter.CustomAuthenticationFilter;
import tech.iloveit.luehningcli.authority.handler.GlobalAccessDeniedHandler;
import tech.iloveit.luehningcli.authority.handler.GlobalAuthenticationEntryPoint;
import tech.iloveit.luehningcli.authority.handler.GlobalAuthenticationFailureHandler;
import tech.iloveit.luehningcli.authority.handler.GlobalAuthenticationSuccessHandler;
import tech.iloveit.luehningcli.authority.mobile.SmsAuthenticationConfig;
import tech.iloveit.luehningcli.authority.mobile.SmsCodeAuthenticationFilter;
import tech.iloveit.luehningcli.authority.service.AbstractCheckSmsCode;
import tech.iloveit.luehningcli.authority.service.SecurityConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Collections;

/**
 * 资源服务配置
 * 添加自定义登录配置 短信、第三方
 */
@Slf4j
@Configuration
@EnableResourceServer
public class OAuth2ResourceServerConfig extends ResourceServerConfigurerAdapter {
    @Autowired
    private AuthorizationServerTokenServices authorizationServerTokenServices;

    @Autowired
    private ClientDetailsService clientDetailsService;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private AbstractCheckSmsCode abstractCheckSmsCode;

    @Autowired
    private UserDetailsService userDetailsService;

    private GlobalAuthenticationFailureHandler globalAuthenticationFailureHandler = new GlobalAuthenticationFailureHandler();

    private GlobalAccessDeniedHandler globalAccessDeniedHandler = new GlobalAccessDeniedHandler();

    /**
     * 如果要让某种运行环境下关闭权限校验，请重写该方法
     * @return
     */
    protected CloseAuthorityEvironment customCloseAuthorityEvironment(){
        return null;
    }

    /**
     * 用户自定义配置，子类可覆盖自定义实现
     * @param http
     * @throws Exception
     */
    protected HttpSecurity customConfigure(HttpSecurity http) throws Exception{
        http.cors().and().csrf().disable().authorizeRequests()
                .anyRequest().authenticated();
        return http;
    }

    @Override
    public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
        super.configure(resources);
        resources.authenticationEntryPoint(new AuthExceptionEntryPoint());
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        boolean isCloseAuth;

        CloseAuthorityEvironment closeAuthority = customCloseAuthorityEvironment();
        if(closeAuthority ==null || closeAuthority.getCloseAuthEnvironment() == null || closeAuthority.getCurrentRunEnvironment()==null){
            isCloseAuth = false;
        }else{
            isCloseAuth = closeAuthority.getCloseAuthEnvironment().equals(closeAuthority.getCurrentRunEnvironment());
        }

        //关闭权限
        if(isCloseAuth){
            http
                    .authorizeRequests()
                    .anyRequest().permitAll();
        }else {
            customConfigure(http);
        }

        //配置短信验证码过滤器
        http.addFilterBefore(new SmsCodeAuthenticationFilter(abstractCheckSmsCode,globalAuthenticationFailureHandler), UsernamePasswordAuthenticationFilter.class);

        //表单登录登录配置
        http.formLogin()
                .loginProcessingUrl(SecurityConstants.DEFAULT_LOGIN_URL_USERNAME_PASSWORD)
                .successHandler(new GlobalAuthenticationSuccessHandler(authorizationServerTokenServices,clientDetailsService,bCryptPasswordEncoder))
                .failureHandler(globalAuthenticationFailureHandler);

        //添加自定义json登陆处理、短信登陆配置
        http.addFilter(customAuthenticationFilter())
                .apply(new SmsAuthenticationConfig(userDetailsService,authorizationServerTokenServices,clientDetailsService,bCryptPasswordEncoder));

        //访问异常以及权限异常处理器配置
        http.exceptionHandling()
                .accessDeniedHandler(globalAccessDeniedHandler)
                .authenticationEntryPoint(new GlobalAuthenticationEntryPoint());

        // 禁用 SESSION、JSESSIONID
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }

    /**
     * 自定义用户名密码json登录过滤器
     * @return
     * @throws Exception
     */
    @Bean
    CustomAuthenticationFilter customAuthenticationFilter() throws Exception {
        CustomAuthenticationFilter filter = new CustomAuthenticationFilter();
        filter.setAuthenticationSuccessHandler(new GlobalAuthenticationSuccessHandler(authorizationServerTokenServices,clientDetailsService,bCryptPasswordEncoder));
        filter.setAuthenticationFailureHandler(globalAuthenticationFailureHandler);
        ProviderManager providerManager =
                new ProviderManager(Collections.singletonList(customDaoAuthenticationProvider()));
        filter.setAuthenticationManager(providerManager);
        return filter;
    }

    @Bean
    CustomDaoAuthenticationProvider customDaoAuthenticationProvider(){
        return new CustomDaoAuthenticationProvider(userDetailsService,bCryptPasswordEncoder);
    }
}

