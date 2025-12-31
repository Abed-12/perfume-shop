package com.abed.perfumeshop.config.security.filter;

import com.abed.perfumeshop.common.res.Response;
import com.abed.perfumeshop.config.security.service.JwtService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static com.abed.perfumeshop.config.security.SecurityConstants.*;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final MessageSource messageSource;
    private final ObjectMapper objectMapper;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    /**
     * Validates JWT token and authenticates the request
     */
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        String token = getTokenFromRequest(request);

        if (token == null) {
            handleMissingToken(request, response, filterChain);
            return;
        }

        try {
            authenticateRequest(request, token);
            filterChain.doFilter(request, response);
        } catch (ExpiredJwtException ex) {
            log.error("JWT token expired for request: {} - {}", request.getRequestURI(), ex.getMessage());
            sendErrorResponse(response, "auth.token.expired");

        } catch (SignatureException ex) {
            log.error("Invalid JWT signature for request: {} - {}", request.getRequestURI(), ex.getMessage());
            sendErrorResponse(response, "auth.token.invalid");

        } catch (MalformedJwtException ex) {
            log.error("Malformed JWT token for request: {} - {}", request.getRequestURI(), ex.getMessage());
            sendErrorResponse(response, "auth.token.malformed");

        } catch (Exception ex) {
            log.error("Error processing JWT token for request: {} - {}", request.getRequestURI(), ex.getMessage(), ex);
            sendErrorResponse(response, "auth.unauthorized");
        }
    }

    // ========== Private Helper Methods ==========

    /**
     * Extracts JWT token from Authorization header
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String tokenWithBearer = request.getHeader(HEADER_STRING);
        if (tokenWithBearer != null && tokenWithBearer.startsWith(TOKEN_PREFIX)) {
            return tokenWithBearer.substring(TOKEN_PREFIX_LENGTH);
        }
        return null;
    }

    /**
     * Handles requests without token - allows public endpoints, rejects protected ones
     */
    private void handleMissingToken(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws IOException, ServletException {
        if (isPublicEndpoint(request)) {
            filterChain.doFilter(request, response);
        } else {
            log.warn("Missing token for protected endpoint: {}", request.getRequestURI());
            sendErrorResponse(response, "auth.unauthorized");
        }
    }

    /**
     * Checks if the requested endpoint is public
     */
    private boolean isPublicEndpoint(HttpServletRequest request) {
        String requestPath = request.getRequestURI();

        return Arrays.stream(PUBLIC_ENDPOINTS)
                .anyMatch(pattern -> pathMatcher.match(pattern, requestPath));
    }

    /**
     * Sends error response with localized message
     */
    private void sendErrorResponse(HttpServletResponse response, String messageKey) throws IOException {
        Response<?> errorResponse = Response.builder()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .message(messageSource.getMessage(
                        messageKey,
                        null,
                        LocaleContextHolder.getLocale()
                ))
                .build();

        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }

    /**
     * Authenticates user and sets security context
     */
    private void authenticateRequest(HttpServletRequest request, String token) {
        String email = jwtService.getUsernameFromToken(token);
        String role = jwtService.getRoleFromToken(token);

        if (StringUtils.hasText(email) && jwtService.isTokenValid(token, email, role)) {
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    email,
                    null,
                    List.of(new SimpleGrantedAuthority(role))
            );
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }
    }

}