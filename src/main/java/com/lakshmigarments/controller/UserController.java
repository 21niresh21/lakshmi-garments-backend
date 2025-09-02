package com.lakshmigarments.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lakshmigarments.dto.UserDTO;
import com.lakshmigarments.dto.UserResponseDTO;
import com.lakshmigarments.service.UserService;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "*")
public class UserController {

	private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);
	private final UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	// get the list of users of with pagination and sorting
	@GetMapping
	public Page<UserResponseDTO> getUsers(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "7") int size, @RequestParam(defaultValue = "name") String sortBy,
			@RequestParam(defaultValue = "desc") String order, @RequestParam(required = false) String search,
			@RequestParam(name = "role", required = false) List<String> roles,
			@RequestParam(name = "isActive", required = false) List<Boolean> userStatuses) {
		LOGGER.info("Fetching all users");
		Sort sort;
		if ("role".equals(sortBy)) {
			sort = order.equals("asc") ? Sort.by("role.name").ascending() : Sort.by("role.name").descending();
		} else {
			sort = order.equals("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
		}

		Pageable pageable = PageRequest.of(page, size, sort);
		return userService.getPaginatedUsers(pageable, search, roles, userStatuses);
	}

	@PostMapping
	public ResponseEntity<UserResponseDTO> createUser(@RequestBody @Validated UserDTO userDTO) {
		LOGGER.info("Creating user with name: {}", userDTO.getName());
		return new ResponseEntity<>(userService.createUser(userDTO), HttpStatus.CREATED);
	}

	@PatchMapping("/{id}")
	public ResponseEntity<UserResponseDTO> updateUser(@PathVariable Long id, @RequestBody @Validated UserDTO userDTO) {
		LOGGER.info("Updating user with id: {}", id);
		return new ResponseEntity<>(userService.updateUser(id, userDTO), HttpStatus.OK);
	}
}
