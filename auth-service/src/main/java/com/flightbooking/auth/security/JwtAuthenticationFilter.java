package com.flightbooking.auth.security;

import com.flightbooking.auth.model.User;
import com.flightbooking.auth.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            try {
                String token = header.substring(7);
                var claims = jwtService.parse(token);
                Long userId = Long.valueOf(claims.getSubject());
                
                User user = userRepository.findById(userId).orElse(null);
                if (user != null && user.isActive()) {
                    String roleStr = claims.get("role", String.class);
                    if (roleStr == null) {
                        roleStr = user.getRole().name();
                    }
                    String authority = roleStr.startsWith("ROLE_") ? roleStr : "ROLE_" + roleStr;
                    var auth = new UsernamePasswordAuthenticationToken(
                            claims.getSubject(), null,
                            java.util.List.of(new SimpleGrantedAuthority(authority)));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } catch (Exception ignored) {
                // Invalid token remains unauthenticated.
            }
        }
        chain.doFilter(request, response);
    }
}
