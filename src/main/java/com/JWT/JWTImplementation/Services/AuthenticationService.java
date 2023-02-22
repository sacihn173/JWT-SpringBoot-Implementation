package com.JWT.JWTImplementation.Services;

import com.JWT.JWTImplementation.Models.*;
import com.JWT.JWTImplementation.Repositores.CustomerRepo;
import com.JWT.JWTImplementation.Repositores.JWTTokenRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final PasswordEncoder passwordEncoder;
    private final CustomerRepo customerRepo;
    private final JWTService jwtService;
    private final JWTTokenRepo jwtTokenRepo;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthenticationResponse register(RegisterRequest request) {
        // save the user to database and generate token and return it
        var customer = Customer.builder()
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .email(request.getEmail())
                .pass(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();
        customerRepo.save(customer);
        var token = jwtService.generateToken(customer);
        // save the created token in database and revoke all existing tokens for that customer
        revokeAllTokens(customer);
        var jwtToken = JWTToken.builder()
                .token(token)
                .expired(false)
                .revoked(false)
                .customer(customer)
                .build();
        jwtTokenRepo.save(jwtToken);
        return AuthenticationResponse.builder()
                .token(token)
                .build();
    }

    @Transactional
    public AuthenticationResponse authenticate(AuthenticationRequest request) throws Exception {
        // username and password is provided, verify it and generate jwt token
        // AuthenticationManager provides method to do it, which is a bean created by us
        // in ApplicationConfig Class
        // not matching password correctly don't know why
//        authenticationManager.authenticate(
//                new UsernamePasswordAuthenticationToken(
//                        request.getEmail(),
//                        request.getPass()
//                )
//        );
        // if we reach here details are correct, else exception is thrown * by above worked
        var customer = customerRepo.findByEmail(request.getEmail())
                .orElseThrow();
        // matching the password manually
        if(!passwordEncoder.matches(request.getPass(), customer.getPass()))
                throw new Exception("Invalid password or username");
        var generatedToken = jwtService.generateToken(customer);
        // save the generated token to database and delete all the existing tokens for that customer
        revokeAllTokens(customer);
        var jwtToken = JWTToken.builder()
                .token(generatedToken)
                .expired(false)
                .revoked(false)
                .customer(customer)
                .build();
        jwtTokenRepo.save(jwtToken);
        return AuthenticationResponse.builder()
                .token(generatedToken)
                .build();
    }

    @Transactional
    public void revokeAllTokens(Customer customer) {
        List<JWTToken> allTokens = jwtTokenRepo.findAllTokensForACustomer(customer.getId());
        for(var token : allTokens) {
            token.setRevoked(true);
            token.setExpired(true);
        }
    }

}
