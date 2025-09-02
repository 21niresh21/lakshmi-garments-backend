package com.lakshmigarments.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lakshmigarments.model.Role;
import com.lakshmigarments.service.RoleService;

@RestController
@RequestMapping("/roles")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class RoleController{
	
	private static final Logger LOGGER = LoggerFactory.getLogger(RoleController.class);

    @Autowired
    private RoleService roleService;

    @GetMapping
    public ResponseEntity<List<Role>> getAllRoles() {
    	LOGGER.info("Fetching all roles");
        List<Role> roles = roleService.getAllRoles();
        return new ResponseEntity<>(roles, HttpStatus.OK);
    }

}