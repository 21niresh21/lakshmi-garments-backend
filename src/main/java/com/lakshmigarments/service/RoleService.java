package com.lakshmigarments.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.lakshmigarments.model.Role;
import com.lakshmigarments.repository.RoleRepository;


@Service
public class RoleService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(RoleService.class);

	@Autowired
	private RoleRepository roleRepository;

	// GET all the roles
	public List<Role> getAllRoles() {
		
		Sort sort = Sort.by(Sort.Order.asc("name"));
		List<Role> roles = roleRepository.findAll(sort);
		LOGGER.info("Retrieved {} roles", roles.size());
		return roles;
	}

}
