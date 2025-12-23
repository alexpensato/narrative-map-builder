package com.manus.gamemap.security;

import com.manus.gamemap.service.SessionService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class SessionFilter extends OncePerRequestFilter {

    @Autowired
    private SessionService sessionService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        
        // Only check session for protected API endpoints, excluding auth endpoints
        // Also skip OPTIONS requests (preflight)
        if (path.startsWith("/api/v1/") && !path.startsWith("/api/v1/auth/") && !"OPTIONS".equalsIgnoreCase(request.getMethod())) {
            String sessionId = request.getHeader("X-Session-ID");
            
            if (sessionId == null || !sessionService.isValidSession(sessionId)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Invalid or missing session");
                return;
            }
        }

        chain.doFilter(request, response);
    }
}
