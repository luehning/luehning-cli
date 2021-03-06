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
 * ??????????????????
 * ??????????????????????????? ??????????????????
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
     * ????????????????????????????????????????????????????????????????????????
     * @return
     */
    protected CloseAuthorityEvironment customCloseAuthorityEvironment(){
        return null;
    }

    /**
     * ??????????????????????????????????????????????????????
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

        //????????????
        if(isCloseAuth){
            http
                    .authorizeRequests()
                    .anyRequest().permitAll();
        }else {
            customConfigure(http);
        }

        //??????????????????????????????
        http.addFilterBefore(new SmsCodeAuthenticationFilter(abstractCheckSmsCode,globalAuthenticationFailureHandler), UsernamePasswordAuthenticationFilter.class);

        //????????????????????????
        http.formLogin()
                .loginProcessingUrl(SecurityConstants.DEFAULT_LOGIN_URL_USERNAME_PASSWORD)
                .successHandler(new GlobalAuthenticationSuccessHandler(authorizationServerTokenServices,clientDetailsService,bCryptPasswordEncoder))
                .failureHandler(globalAuthenticationFailureHandler);

        //???????????????json?????????????????????????????????
        http.addFilter(customAuthenticationFilter())
                .apply(new SmsAuthenticationConfig(userDetailsService,authorizationServerTokenServices,clientDetailsService,bCryptPasswordEncoder));

        //?????????????????????????????????????????????
        http.exceptionHandling()
                .accessDeniedHandler(globalAccessDeniedHandler)
                .authenticationEntryPoint(new GlobalAuthenticationEntryPoint());

        // ?????? SESSION???JSESSIONID
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }

    /**
     * ????????????????????????json???????????????
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

