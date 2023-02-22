package com.JWT.JWTImplementation.Filters;

import com.JWT.JWTImplementation.Models.JWTToken;
import com.JWT.JWTImplementation.Repositores.JWTTokenRepo;
import com.JWT.JWTImplementation.Services.JWTService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JWTAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JWTService jwtService;
    @Autowired
    private UserDetailsService userDetailsService;
    private final JWTTokenRepo jwtTokenRepo;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwtToken;
        final String userEmail;

        // not a valid token
        if(authHeader == null || !authHeader.startsWith("Bearer")) {
            // don't tell SecurityContextHolder that it is valid and authenticated
            filterChain.doFilter(request, response);
            return;
        }

        jwtToken = authHeader.substring(7);
        // returns us the username from the jwt token
        userEmail = jwtService.extractUsername(jwtToken);
        // second condition means user is not yet authenticated
        if(userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // now verify by getting the user details
            // do this by using UserDetailsService

            UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);
            if(jwtService.isTokenValid(jwtToken, userDetails)) {
                // also check if the token was made expired or was revoked due to some actions such as logout
                // check in the database
                JWTToken tokenFromDatabase = jwtTokenRepo.findByToken(jwtToken).orElseThrow();
                if(tokenFromDatabase.isExpired() || tokenFromDatabase.isRevoked()) {
                    // token is revoked or expired
                    filterChain.doFilter(request, response);
                    return;
                }

                // valid token
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}
