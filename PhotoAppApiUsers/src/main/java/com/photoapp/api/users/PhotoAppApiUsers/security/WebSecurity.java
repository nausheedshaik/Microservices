package com.photoapp.api.users.PhotoAppApiUsers.security;

import com.photoapp.api.users.PhotoAppApiUsers.service.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.expression.WebExpressionAuthorizationManager;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class WebSecurity {

    private Environment environment;
    private UsersService usersService;
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    public WebSecurity(Environment environment, UsersService usersService,
                       BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.environment = environment;
        this.usersService = usersService;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @Bean
    public SecurityFilterChain configure(HttpSecurity http) throws Exception {

        // Configure AuthenticationManagerBuilder
        AuthenticationManagerBuilder authenticationManagerBuilder = http
                .getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.userDetailsService(usersService).passwordEncoder(bCryptPasswordEncoder);

        // Get AuthenticationManager
        AuthenticationManager authenticationManager = authenticationManagerBuilder.build();

        http
                .cors(cors -> {})
                .csrf((csrf) -> csrf.disable())
                .authorizeHttpRequests((authz) -> authz.requestMatchers(HttpMethod.POST, "/users")
                        .access(new WebExpressionAuthorizationManager(
                                "hasIpAddress('" + environment.getProperty("gateway.ip") + "')"))
                        .requestMatchers(HttpMethod.POST, environment.getProperty("login.url.path")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/h2-console/**")).permitAll()
                        .anyRequest()
                        .authenticated())
                .addFilter(getAuthenticationFilter(authenticationManager))
                .addFilter(new AuthorizationFilter(authenticationManager, environment))
                .authenticationManager(authenticationManager)
                .sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.headers((headers) -> headers.frameOptions((frameOptions) -> frameOptions.sameOrigin()));

        return http.build();
    }

    protected AuthenticationFilter getAuthenticationFilter(AuthenticationManager authenticationManager)
            throws Exception {
        final AuthenticationFilter filter = new AuthenticationFilter(usersService, environment, authenticationManager);
        filter.setFilterProcessesUrl(environment.getProperty("login.url.path"));
        return filter;
    }

//	@Override
//	protected void configure(HttpSecurity http) throws Exception {
//		http.csrf().disable();
//		http.authorizeRequests().antMatchers("/**").permitAll()//.hasIpAddress(environment.getProperty("gateway.ip"))
//		.and()
//		.addFilter(getAuthenticationFilter());
//		http.headers().frameOptions().disable();
//	}

//	private AuthenticationFilter getAuthenticationFilter() throws Exception
//	{
//		AuthenticationFilter authenticationFilter = new AuthenticationFilter(usersService, environment, authenticationManager());
//		//authenticationFilter.setAuthenticationManager(authenticationManager());
//		authenticationFilter.setFilterProcessesUrl(environment.getProperty("login.url.path"));
//		return authenticationFilter;
//	}

//    @Override
//    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
//        auth.userDetailsService(usersService).passwordEncoder(bCryptPasswordEncoder);
//    }

}

}