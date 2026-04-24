package edu.gatech.cs6310.powergrid.auth;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Reads the Authorization: Bearer &lt;token&gt; header, looks up the session,
 * and populates the Spring SecurityContext with its roles. Missing or invalid
 * tokens leave the context anonymous — SecurityConfig decides which endpoints
 * require auth.
 */
@Component
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private static final String HEADER = "Authorization";
    private static final String PREFIX = "Bearer ";

    private final UserService users;

    public TokenAuthenticationFilter(UserService users) {
        this.users = users;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String header = req.getHeader(HEADER);
        if (header != null && header.startsWith(PREFIX)) {
            String token = header.substring(PREFIX.length()).trim();
            users.validate(token).ifPresent(session -> {
                List<SimpleGrantedAuthority> auths = session.roles().stream()
                    .map(r -> new SimpleGrantedAuthority(r.authority()))
                    .toList();
                UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(session.username(), token, auths);
                auth.setDetails(session);
                SecurityContextHolder.getContext().setAuthentication(auth);
            });
        }
        chain.doFilter(req, res);
    }
}
