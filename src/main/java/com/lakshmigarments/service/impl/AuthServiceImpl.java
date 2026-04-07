package com.lakshmigarments.service.impl;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.lakshmigarments.dto.request.LoginRequest;
import com.lakshmigarments.dto.response.LoginResponse;
import com.lakshmigarments.security.JwtUtils;
import com.lakshmigarments.service.AuthService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class AuthServiceImpl implements AuthService {
	
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    public LoginResponse authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), 
            		loginRequest.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        String jwt = jwtUtils.generateJwtToken(authentication);
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        ZonedDateTime tokenExpiryDate =
        	    jwtUtils.getExpirationDateFromToken(jwt)
        	            .toInstant()
        	            .atZone(ZoneId.of("Asia/Kolkata"));

        return new LoginResponse(jwt, "Bearer", userDetails.getUsername(), 
        		roles, tokenExpiryDate);
    }
}