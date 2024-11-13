package com.cos.jwt.config.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.cos.jwt.config.auth.PrincipalDetails;
import com.cos.jwt.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 *  스프링 시큐리티에서 UsernamePasswordAuthenticationFilter 가 있는데
 *  /login 요청이 들어오면 이 필터가 동작함.
 *  formLogin 을 disable 처리했기때문에 현재 동작하지 않기때문에 필터를 재작성하여 securityFilter 에 등록해야 한다.
 */
//@RequiredArgsConstructor
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;

    public JwtAuthenticationFilter(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    /**
     * 동작시점
     * /login 요청을 하면 로그인 시도를 위해서 실행되는 함수
     */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        System.out.println("JwtAuthenticationFilter : 로그인 시도");

        /**
         * 1. username, password 받아서
         * 2. 정상인지 로그인 시도 -> authenticationManager로 로그인 시도하면 PrincipalDetailsService가 자동 호출
         * loadUserByUsername() 함수 실행됨
         *
         * 3. PrincipalDetails 를 세션에 담고 (세션에 담지 않으면 권한관리가 안됨)
         *
         * 4. JWT 토큰을 만들어 응답해주면 됨.
         */
        try {

//            BufferedReader br = request.getReader();
//            String input = null;
//            while((input = br.readLine()) != null) {
//                System.out.println(input);
//            }

            ObjectMapper om = new ObjectMapper();
            User user = om.readValue(request.getInputStream(), User.class);

            UsernamePasswordAuthenticationToken authRequestToken = new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword());


            /**
             *  PrincipalDetailsService의 loadUserByUsername() 함수가 실행되고 정상이면 authentication이 리턴된다.
             *  DB의 username과 password가 일치한다.
             *  authentication 에는 내 로그인 정보가 담긴다.
              */
            Authentication authentication = authenticationManager.authenticate(authRequestToken);

            /**
             * authentication 객체가 session 영역에 저장됨
             * => 로그인이 되었다는 뜻.
             */
            PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();

            System.out.println(principalDetails.getUser());

            //세션에 authentication 객체가 저장됨
            return authentication;
        } catch (IOException e) {

//            return null;
            throw new RuntimeException(e);
        }
    }

    // attemptAuthentication 실행 후 인증이 정상적으로 되었으면 이 함수가 실행
    // JWT 토큰을 만들어서 request 요청한 사용자에게 JWT 토큰을 response 해주면 됨.
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {

        System.out.println("successful auth : " + authResult);
        PrincipalDetails principalDetails = (PrincipalDetails) authResult.getPrincipal();
        
        // RSA 방식이 아닌 Hash 암호방식
        String jwtToken = JWT.create()
                .withSubject("cos토큰") //큰 의미는 없음
                .withExpiresAt(new Date(System.currentTimeMillis()+(60000 * 10))) //만료시간
                .withClaim("id",principalDetails.getUser().getId())
                .withClaim("username", principalDetails.getUser().getUsername())
                .sign(Algorithm.HMAC512(JwtProperties.SECRET)); //서버만 알고있는 secret key 를 갖고 있어야 함.


        /**
         * 정리
         * JWT토큰을 생성 후 클라이언트로 JWT토큰을 응답.
         *
         * 요청 시 JWT토큰으로 요청
         * 서버는 유효한지 판단한다. (유효성 검사하는 필터 필요)
         */
        response.addHeader(JwtProperties.HEADER_STRING, JwtProperties.TOKEN_PREFIX + jwtToken);

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("username", principalDetails.getUser().getUsername());
        responseBody.put("roles", principalDetails.getUser().getRoles());
        response.setContentType("application/json");
        new ObjectMapper().writeValue(response.getWriter(), responseBody);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\": \"Authentication failed\"}");
    }
}
