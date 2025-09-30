package com.lakshmigarments.controller;

import com.lakshmigarments.dto.SkillRequestDTO;
import com.lakshmigarments.dto.SkillResponseDTO;
import com.lakshmigarments.service.SkillService;

import jakarta.validation.Valid;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/skills")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class SkillController {

    private static final Logger LOGGER = LoggerFactory.getLogger(SkillController.class);

    private final SkillService skillService;

    public SkillController(SkillService skillService) {
        this.skillService = skillService;
    }

    @PostMapping
    public ResponseEntity<SkillResponseDTO> createSkill(@Valid @RequestBody SkillRequestDTO skillRequestDTO) {
        LOGGER.info("Received request to create a new skill: {}", skillRequestDTO.getName());

        SkillResponseDTO skillResponseDTO = skillService.createSkill(skillRequestDTO);

        LOGGER.info("Skill created successfully with ID: {}", skillResponseDTO.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(skillResponseDTO);
    }
    
    @GetMapping
    public ResponseEntity<List<SkillResponseDTO>> getAllSkills(@RequestParam(required = false) String search) {
        LOGGER.info("Received request to fetch all skills with search: {}", search);

        List<SkillResponseDTO> skills = skillService.getAllSkills(search);

        LOGGER.info("Returning {} skill(s)", skills.size());

        return ResponseEntity.ok(skills);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SkillResponseDTO> updateSkill(@PathVariable Long id, @RequestBody SkillRequestDTO skillRequestDTO) {
        LOGGER.info("Received request to update skill with ID: {}", id);
        SkillResponseDTO skillResponseDTO = skillService.updateSkill(id, skillRequestDTO);
        LOGGER.info("Skill updated successfully with ID: {}", id);
        return ResponseEntity.ok(skillResponseDTO);
    }

}
