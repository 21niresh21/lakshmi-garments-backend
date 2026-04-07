package com.lakshmigarments.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lakshmigarments.dto.request.SkillRequest;
import com.lakshmigarments.dto.response.SkillResponse;
import com.lakshmigarments.exception.DuplicateSkillException;
import com.lakshmigarments.exception.SkillNotFoundException;
import com.lakshmigarments.model.Skill;
import com.lakshmigarments.repository.SkillRepository;
import com.lakshmigarments.repository.specification.SkillSpecification;
import com.lakshmigarments.service.SkillService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SkillServiceImpl implements SkillService {

	private static final Logger LOGGER = LoggerFactory.getLogger(SkillServiceImpl.class);

	private final SkillRepository skillRepository;
	private final ModelMapper modelMapper;

	@Override
	@Transactional(readOnly = true)
	public List<SkillResponse> getAllSkills(String search) {
		LOGGER.debug("Fetching all skills matching: {}", search);
		Specification<Skill> spec = SkillSpecification.filterByName(search);
		List<Skill> skills = skillRepository.findAll(spec);

		LOGGER.debug("Found {} skill(s) matching filter", skills.size());
		return skills.stream()
				.map(skill -> modelMapper.map(skill, SkillResponse.class))
				.collect(Collectors.toList());
	}

	@Override
	@Transactional
	public SkillResponse createSkill(SkillRequest skillRequest) {
		LOGGER.debug("Creating skill: {}", skillRequest.getName());
		String skillName = skillRequest.getName().trim();

		validateSkillUniqueness(skillName, null);

		Skill skill = new Skill();
		skill.setName(skillName);

		Skill savedSkill = skillRepository.save(skill);
		LOGGER.info("Skill created successfully with ID: {}", savedSkill.getId());
		return modelMapper.map(savedSkill, SkillResponse.class);
	}

	@Override
	@Transactional
	public SkillResponse updateSkill(Long id, SkillRequest skillRequest) {
		LOGGER.debug("Updating skill with ID: {}", id);
		
		Skill skill = this.getSkillOrThrow(id);
		
		String skillName = skillRequest.getName().trim();

		validateSkillUniqueness(skillName, id);

		skill.setName(skillName);
		
		Skill updatedSkill = skillRepository.save(skill);
		LOGGER.info("Skill updated successfully with ID: {}", updatedSkill.getId());
		return modelMapper.map(updatedSkill, SkillResponse.class);
	}

	private void validateSkillUniqueness(String name, Long id) {
		if (id == null) {
			if (skillRepository.existsByNameIgnoreCase(name)) {
				LOGGER.error("Skill name already exists: {}", name);
				throw new DuplicateSkillException("Skill already exists with name: " + name);
			}
		} else {
			if (skillRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
				LOGGER.error("Skill name already exists for another ID: {}", name);
				throw new DuplicateSkillException("Skill already exists with name: " + name);
			}
		}
	}

	private Skill getSkillOrThrow(Long id) {
		return skillRepository.findById(id).orElseThrow(() -> {
			LOGGER.error("Skill with ID {} not found", id);
			return new SkillNotFoundException("Skill not found with ID: " + id);
		});
	}

}
