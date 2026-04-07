package com.lakshmigarments.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.lakshmigarments.model.User;

public interface UserRepository extends JpaRepository<User, Long>, 
	JpaSpecificationExecutor<User> {
	
	Optional<User> findByUsername(String username);
	
	Boolean existsByUsername(String username);
	
	Boolean existsByUsernameAndIdNot(String username, Long id);
	
	long countByRoleNameAndIsActiveTrue(String roleName);
	
}
