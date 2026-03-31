package com.reco.demo.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Get the Authorization Header from the request
        String authHeader = request.getHeader("Authorization");

        // 2. Check if the header contains a Bearer token
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            // 3. Validate the token using our JwtUtils
            if (jwtUtils.validateToken(token)) {
                String username = jwtUtils.getUsernameFromToken(token);

                // 4. Create an Authentication Object for Spring Security
                // We pass 'null' for credentials because the token IS the credential.
                // 'new ArrayList<>()' represents an empty list of roles/authorities.
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(username, null, new ArrayList<>());

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 5. CRITICAL: Set the Security Context
                // This is what tells Spring "This user is officially logged in for this request"
                SecurityContextHolder.getContext().setAuthentication(authentication);

                // Keep your custom attribute for easy access in RecommendationController
                request.setAttribute("validatedUsername", username);
            }
        }

        // 6. Continue the filter chain
        filterChain.doFilter(request, response);
    }
}
