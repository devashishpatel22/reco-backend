package com.reco.demo;

import com.reco.demo.security.JwtFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    // Inject the JwtFilter we created in Step 4
    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())

                // 1. Configure CORS for React (Port 3000)
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration config = new CorsConfiguration();
                    config.setAllowedOrigins(List.of("http://localhost:3000","reco-frontend-sigma.vercel.app"));
                    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                    config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With"));
                    config.setAllowCredentials(true);
                    return config;
                }))

                // 2. Define Access Rules
                .authorizeHttpRequests(auth -> auth
                        // Public: Allow anyone to Login or Register
                        .requestMatchers("/api/auth/**").permitAll()

                        // Public: Allow anyone to view the recommendations list
                        .requestMatchers(HttpMethod.GET, "/api/recommendations/**").permitAll()

                        // Secure: Everything else (POST, LIKE, etc.) requires a valid JWT
                        .anyRequest().authenticated()
                )

                // 3. Stateless Session (No Cookies)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 4. THE BRIDGE: Tell Spring to run our JwtFilter BEFORE the login filter
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}