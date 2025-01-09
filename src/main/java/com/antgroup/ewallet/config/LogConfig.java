//package com.antgroup.ewallet.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.filter.CommonsRequestLoggingFilter;
//import org.springframework.boot.web.servlet.FilterRegistrationBean;
//import javax.servlet.Filter;
//
//@Configuration
//public class LogConfig {
//
//    @Bean
//    public CommonsRequestLoggingFilter requestLoggingFilter() {
//        CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter();
//        filter.setIncludeQueryString(true);
//        filter.setIncludePayload(true);
//        filter.setMaxPayloadLength(10000);
//        filter.setIncludeHeaders(true);
//        filter.setAfterMessagePrefix("REQUEST DATA: ");
//        return filter;
//    }
//
//    @Bean
//    public FilterRegistrationBean<Filter> loggingFilterRegistration() {
//        FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>();
//        registration.setFilter(requestLoggingFilter());
//        registration.addUrlPatterns("/pay/*", "/initiatePay/*");
//        registration.setName("requestLoggingFilter");
//        registration.setOrder(1);
//        return registration;
//    }
//}