package pawa_be.infrastructure.jwt.filter;

import java.io.IOException;

import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import pawa_be.infrastructure.jwt.JwtUtil;
import pawa_be.infrastructure.jwt.config.UserAuthConfig;

@Component
public class AuthRequestFilter extends OncePerRequestFilter {

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        // Skip authentication for Swagger UI and API docs
        String requestPath = request.getRequestURI();
        if (requestPath.contains("/swagger-ui/") || requestPath.contains("/v3/api-docs/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = null;
        boolean isValidToken = false;

        if (request.getCookies() != null) {
            for (var cookie : request.getCookies()) {
                if (UserAuthConfig.USER_AUTH_COOKIE_NAME.equals(cookie.getName())) {
                    jwt = cookie.getValue();
                    isValidToken = jwtUtil.verifyJwtSignature(jwt);
                    break;
                }
            }
        }

        if (jwt != null && isValidToken && SecurityContextHolder.getContext().getAuthentication() == null) {
            final String email = jwtUtil.extractUsername(jwt);
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            if (userDetails != null) {
                UsernamePasswordAuthenticationToken emailAuthToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                emailAuthToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(emailAuthToken);
            }
        }
        filterChain.doFilter(request, response);
    }
}
