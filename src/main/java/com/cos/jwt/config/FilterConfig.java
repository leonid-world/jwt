package com.cos.jwt.config;

import com.cos.jwt.filter.MyFilter1;
import jakarta.servlet.FilterRegistration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//@Configuration
public class FilterConfig {

//    @Bean
    public FilterRegistrationBean<MyFilter1> filter1() {
        FilterRegistrationBean<MyFilter1> bean = new FilterRegistrationBean<>();

        bean.addUrlPatterns(("/*"));
        bean.setOrder(0);

        return bean;
    }
}
