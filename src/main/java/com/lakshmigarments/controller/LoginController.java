package com.lakshmigarments.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import com.lakshmigarments.dto.LoginRequest;
import com.lakshmigarments.model.User;
import com.lakshmigarments.repository.UserRepository;
import com.lakshmigarments.utility.JwtUtil;
@RestController
@RequestMapping("/login")
@CrossOrigin(origins = "*")
public class LoginController {
	
	private UserRepository userRepository;
	private final JwtUtil jwtUtil;
	
	public LoginController(UserRepository userRepository, JwtUtil jwtUtil) {
		this.userRepository = userRepository;
		this.jwtUtil = jwtUtil;
	}

    @PostMapping
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest request) {
    	
    	User user = userRepository.findByName(request.getUsername()).orElse(null);
    	
    	if (user == null) {
    		return ResponseEntity.status(404).body(Map.of("error", "User not found"));
		}
		// } else {
		// 	User authenticatedUser = userRepository.findByNameAndPassword(request.getUsername(), request.getPassword()).orElse(null);
		// 	if (authenticatedUser == null) {
		// 		return ResponseEntity.status(401).body(authenticatedUser);
		// 	} else {
		// 		return ResponseEntity.status(200).body(authenticatedUser);
		// 	}
		// }
		String hashedInputPassword = jwtUtil.hashPassword(request.getPassword());
		if (!hashedInputPassword.equals(user.getPassword())) {
			return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
		}

		Map<String, Object> claims = new HashMap<>();
		claims.put("role", user.getRole().getName());
		String token = jwtUtil.generateToken(user.getName(), claims);

		Map<String, Object> response = new HashMap<>();
		response.put("token", token);
		response.put("user", Map.of(
			"id", user.getId(),
			"name", user.getName(),
			"role", user.getRole().getName()
		));

		return ResponseEntity.ok(response);


    }
}
