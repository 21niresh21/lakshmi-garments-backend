package com.lakshmigarments.service;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.lakshmigarments.dto.UserDTO;
import com.lakshmigarments.dto.UserResponseDTO;
import com.lakshmigarments.exception.DuplicateUsernameException;
import com.lakshmigarments.exception.RoleNotFoundException;
import com.lakshmigarments.model.Role;
import com.lakshmigarments.model.User;
import com.lakshmigarments.repository.RoleRepository;
import com.lakshmigarments.repository.UserRepository;
import com.lakshmigarments.repository.specification.UserSpecification;
import com.lakshmigarments.utility.JwtUtil;


@Service
public class UserService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);
	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final ModelMapper modelMapper;
	private final JwtUtil jwtUtil;
	
	public UserService(UserRepository userRepository, ModelMapper modelMapper, RoleRepository roleRepository, JwtUtil jwtUtil) {
		this.userRepository = userRepository;
		this.modelMapper = modelMapper;
		this.roleRepository = roleRepository;
		this.jwtUtil = jwtUtil;
	}
	
	// GET ALL
	public Page<UserResponseDTO> getPaginatedUsers(Pageable pageable, String search, List<String> roles,
	       List<Boolean> userStatuses) {

		LOGGER.info("Fetching paginated users");
	    Specification<User> userSpecification = Specification.where(null);

	    // Filter by roles
	    if (roles != null && !roles.isEmpty()) {
	        userSpecification = userSpecification.and(UserSpecification.filterByRoles(roles));
	    }

	    // Filter by user status
	    if (userStatuses != null && !userStatuses.isEmpty()) {
	        userSpecification = userSpecification.and(UserSpecification.filterByUserStatus(userStatuses));
	    }
	    // Search filter
	    if (search != null && !search.isEmpty()) {
	        Specification<User> searchSpecification = Specification.where(null);
	        searchSpecification = searchSpecification.or(UserSpecification.filterByName(search));
	        userSpecification = userSpecification.and(searchSpecification);
	    }

	    Page<User> usersPage = userRepository.findAll(userSpecification, pageable);

	    List<UserResponseDTO> userResponseDTOList = usersPage.getContent().stream()
	            .map(user -> modelMapper.map(user, UserResponseDTO.class))
	            .collect(Collectors.toList());

	    LOGGER.info("Fetched paginated users successfully");
	    return new PageImpl<>(userResponseDTOList, usersPage.getPageable(), usersPage.getTotalElements());
	}
	
	public UserResponseDTO createUser(UserDTO userDTO) {
		//checking for valid role name
		Role role = roleRepository.findByName(userDTO.getRoleName()).orElseThrow(() -> {
			LOGGER.error("Role with name {} not found", userDTO.getRoleName());
			return new RoleNotFoundException("Role not found with name " + userDTO.getRoleName());
		});
		
		if (userRepository.existsByName(userDTO.getName())) {
	        LOGGER.error("User with name {} already exists", userDTO.getName());
	        throw new DuplicateUsernameException("User with name '" + userDTO.getName() + "' already exists");
	    }
		
		Boolean isActive = userDTO.getIsActive() != null ? userDTO.getIsActive() : true;
		
		User user = new User();
		user.setName(userDTO.getName());
		user.setPassword(jwtUtil.hashPassword(userDTO.getPassword()));
		user.setIsActive(isActive);
		user.setRole(role);
		
		User savedUser = userRepository.save(user);
		LOGGER.info("User created successfully with ID: {}", savedUser.getId());
		return modelMapper.map(savedUser, UserResponseDTO.class);
		
	}
	
	public UserResponseDTO updateUser(Long id, UserDTO userDTO) {
		
		User user = userRepository.findById(id).orElseThrow(() -> {
			LOGGER.error("User with ID {} not found", id);
			return new RuntimeException("User not found with ID " + id);
		});
		
//		if (userRepository.existsByNameAndIdNot(userDTO.getName(), id)) {
//			LOGGER.error("User with name {} already exists", userDTO.getName());
//			throw new DuplicateUsernameException("User with name '" + userDTO.getName() + "' already exists");
//		}
		
//		Role role = roleRepository.findByName(userDTO.getRoleName()).orElseThrow(() -> {
//			LOGGER.error("Role with name {} not found", userDTO.getRoleName());
//			return new RoleNotFoundException("Role not found with name " + userDTO.getRoleName());
//		});
		
//		user.setName(userDTO.getName());
//		user.setPassword(userDTO.getPassword());
		user.setIsActive(userDTO.getIsActive() != null ? userDTO.getIsActive() : true);
//		user.setRole(role);
		
		User updatedUser = userRepository.save(user);
		LOGGER.info("User updated successfully with ID: {}", updatedUser.getId());
		return modelMapper.map(updatedUser, UserResponseDTO.class);
	}
}
