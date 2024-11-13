package com.cos.jwt.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

public class MyFilter1 implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        req.setCharacterEncoding("UTF-8");

        
        if(req.getMethod().equals("POST")){
            String headerAuth = req.getHeader("Authorization");

            if(headerAuth.equals("cos")){
                chain.doFilter(request, response);
            } else {
                PrintWriter out = res.getWriter();
                out.println("인증안됨");
                
            }
        }
        chain.doFilter(request, response);
    }
}