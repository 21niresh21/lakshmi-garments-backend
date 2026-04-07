package com.lakshmigarments.service;

import java.util.List;
import com.lakshmigarments.dto.request.SkillRequest;
import com.lakshmigarments.dto.response.SkillResponse;

public interface SkillService {
	
	SkillResponse createSkill(SkillRequest skillRequest);
	
	List<SkillResponse> getAllSkills(String search);

	SkillResponse updateSkill(Long id, SkillRequest skillRequest);

}
