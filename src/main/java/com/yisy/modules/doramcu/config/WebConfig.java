package com.yisy.modules.doramcu.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Disable caching for static resources in development to ensure updated files are always served
        registry.addResourceHandler("/**")
                .addResourceLocations("static/")
                .setCachePeriod(0);
    }

    /**
     * Filter that forces no-cache headers on all static resource requests.
     * This ensures browsers always fetch fresh content during development.
     */
    @Bean
    public Filter noCacheFilter() {
        return new Filter() {
            @Override
            public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                    throws IOException, ServletException {
                HttpServletRequest httpRequest = (HttpServletRequest) request;
                HttpServletResponse httpResponse = (HttpServletResponse) response;

                String path = httpRequest.getRequestURI();
                // Apply no-cache headers to static resources
                if (path.startsWith("/static/") || path.endsWith(".html") ||
                    path.endsWith(".css") || path.endsWith(".js") ||
                    path.endsWith(".png") || path.endsWith(".svg") || path.endsWith(".ico")) {
                    httpResponse.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
                    httpResponse.setHeader("Pragma", "no-cache");
                    httpResponse.setDateHeader("Expires", 0);
                }

                chain.doFilter(request, response);
            }
        };
    }
}
