package com.huan.huanpicture.config;

import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Component
public class RepeatedlyReadableFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest) {
            request = new RequestWrapper((HttpServletRequest) request);
        }
        chain.doFilter(request, response);
    }
}
