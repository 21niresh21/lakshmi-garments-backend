package com.lakshmigarments.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.lakshmigarments.dto.SkillRequestDTO;
import com.lakshmigarments.dto.SkillResponseDTO;

@Service
public interface SkillService {
	
	SkillResponseDTO createSkill(SkillRequestDTO skillRequestDTO);
	
	List<SkillResponseDTO> getAllSkills(String search);

	SkillResponseDTO updateSkill(Long id, SkillRequestDTO skillRequestDTO);

}
