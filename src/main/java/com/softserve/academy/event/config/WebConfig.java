package com.softserve.academy.event.config;

import com.softserve.academy.event.annotation.resolver.PageableDefaultResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@EnableWebMvc
@ComponentScan(basePackages = {"com.softserve.academy.event.config", "com.softserve.academy.event.controller",
        "com.softserve.academy.event.service", "com.softserve.academy.event.exception.handler"})
public class WebConfig implements WebMvcConfigurer {

    private final Environment environment;

    @Autowired
    public WebConfig(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(new PageableDefaultResolver());
    }

    @Bean(name = "multipartResolver")
    public CommonsMultipartResolver getResolver() {
        return new CommonsMultipartResolver();
    }

//    @Override
//    public void addResourceHandlers(ResourceHandlerRegistry registry) {
//        registry.addResourceHandler("swagger-ui.html")
//                .addResourceLocations("classpath:/META-INF/resources/");
//
//        registry.addResourceHandler("/webjars/**")
//                .addResourceLocations("classpath:/META-INF/resources/webjars/");
//    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**").allowedOrigins(environment.getProperty("app.frontend.url")).allowCredentials(true);
    }
}
