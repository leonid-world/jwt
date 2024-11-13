package com.cos.jwt.config.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.cos.jwt.config.auth.PrincipalDetails;
import com.cos.jwt.mapper.UserMapper;
import com.cos.jwt.model.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import java.io.IOException;

/**
 *  시큐리티가 filter를 가지고 있는데 그 필터중에 BasicAuthenticationFilter 라는 것이 있음.
 *  권한이나 인증이 필요한 특정 주소를 요청했을 때 위 필터를 무조건 타게 되어있음.
 *  만약 권한이나 인증이 필요한 주소가 아니라면 이 필터를 타지 않는다.
  */

public class JwtAuthorizationFilter extends BasicAuthenticationFilter {

    private UserMapper userMapper;

    public JwtAuthorizationFilter(AuthenticationManager authenticationManager, UserMapper userMapper) {
        super(authenticationManager);
        this.userMapper = userMapper;
    }

    /**
     * 인증이나 권한이 필요한 주소요청이 있을 때 이 필터를 타게 된다.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
//        super.doFilterInternal(request, response, chain);
        System.out.println("인증이나 권한이 필요한 주소 요청이 됨.");

        String jwtHeader = request.getHeader(JwtProperties.HEADER_STRING);
        System.out.println(jwtHeader);
        System.out.println("=====================1");

        //JWT 토큰을 검증해 정상적인 사용자인지 확인
        if(jwtHeader != null && jwtHeader.startsWith(JwtProperties.TOKEN_PREFIX)) {
            System.out.println("=====================2");
            String jwtToken = jwtHeader.substring(7);
            String username = JWT.require(Algorithm.HMAC512(JwtProperties.SECRET)).build().verify(jwtToken).getClaim("username").asString();

            System.out.println("jwtToken : " + jwtToken);
            System.out.println("username : " + username);
            //서명이 정상적으로 되었음
            if(username != null){
                User user = userMapper.selectByUsername(username);

                System.out.println("=====================3");
                System.out.println("User : " + user);

                PrincipalDetails principalDetails = new PrincipalDetails(user);

                //JWT 토큰 서명을 통해서 서명이 정상이면 authentication 객체를 만들어준다.
                Authentication authentication = new UsernamePasswordAuthenticationToken(principalDetails, null, principalDetails.getAuthorities());

                //시큐리티 세션공간(getContext())에 접근하여 Authentication 객체를 저장한다.
                SecurityContextHolder.getContext().setAuthentication(authentication);

            }


            chain.doFilter(request, response);
        } else {
            chain.doFilter(request, response);
        }
    }
}

