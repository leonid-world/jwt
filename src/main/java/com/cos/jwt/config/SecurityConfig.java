package com.cos.jwt.config;


import com.cos.jwt.config.jwt.JwtAuthenticationFilter;
import com.cos.jwt.config.jwt.JwtAuthorizationFilter;
import com.cos.jwt.filter.MyFilter1;
import com.cos.jwt.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.filter.CorsFilter;

@EnableWebSecurity
@Configuration
public class SecurityConfig {

    private final CorsFilter corsFilter;
    private final UserMapper userMapper;

    private final AuthenticationConfiguration authenticationConfiguration;

    public SecurityConfig(CorsFilter corsFilter,AuthenticationConfiguration authenticationConfiguration, UserMapper userMapper) {
        this.corsFilter = corsFilter;
        this.authenticationConfiguration = authenticationConfiguration;
        this.userMapper = userMapper;
    }

    @Bean
    public AuthenticationManager authenticationManager() throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

//        http.addFilterBefore( new MyFilter1(), BasicAuthenticationFilter.class);
        
        //일단 6.0 이상 버전에서 가능한 방법 나중에 찾아서 적용
        http.addFilter(new JwtAuthenticationFilter(authenticationManager()));
        http.addFilter(new JwtAuthorizationFilter(authenticationManager(), userMapper));

        http.csrf(AbstractHttpConfigurer::disable);
        http.formLogin(AbstractHttpConfigurer::disable);
        http.httpBasic(AbstractHttpConfigurer::disable);
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));


        /**
         * 모든 요청에 대한 cors 정책을 이 필터로 설정
         * @CrossOrigin 어노테이션과의 차이
         *  - 컨트롤러에 위 어노테이션을 추가하면, 인증이 필요하지 않은 엔드포인트에 한하여 모든 cors 정책을 무시한다.
         */
        http.addFilter(corsFilter);

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/user/**").hasAnyRole("USER","MANAGER","ADMIN")
//                .requestMatchers("/api/v1/user/**").hasAuthority("ROLE_USER")
                .requestMatchers("/api/v1/manager/**").hasAnyRole("MANAGER","ADMIN")
                .requestMatchers("/api/v1/admin/**").hasAuthority("ROLE_ADMIN")
                .anyRequest().permitAll()
        );

        return http.build();
    }
}
