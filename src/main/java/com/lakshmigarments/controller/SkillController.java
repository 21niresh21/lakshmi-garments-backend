package com.lakshmigarments.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lakshmigarments.dto.request.SkillRequest;
import com.lakshmigarments.dto.response.SkillResponse;
import com.lakshmigarments.service.SkillService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/skills")
@RequiredArgsConstructor
public class SkillController {

	private static final Logger LOGGER = LoggerFactory.getLogger(SkillController.class);

	private final SkillService skillService;

	@GetMapping
	public ResponseEntity<List<SkillResponse>> getAllSkills(@RequestParam(required = false) String search) {
		LOGGER.info("Received request to fetch all skills matching: {}", search);
		List<SkillResponse> skills = skillService.getAllSkills(search);
		return ResponseEntity.ok(skills);
	}

	@PostMapping
	public ResponseEntity<SkillResponse> createSkill(
			@RequestBody @Valid SkillRequest request) {
		LOGGER.info("Received request to create skill: {}", request.getName());
		SkillResponse response = skillService.createSkill(request);
		return new ResponseEntity<>(response, HttpStatus.CREATED);
	}

	@PutMapping("/{id}")
	public ResponseEntity<SkillResponse> updateSkill(@PathVariable Long id,
			@RequestBody @Valid SkillRequest request) {
		LOGGER.info("Received request to update skill ID: {}", id);
		SkillResponse response = skillService.updateSkill(id, request);
		return ResponseEntity.ok(response);
	}

}
