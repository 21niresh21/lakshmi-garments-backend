package com.lakshmigarments.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lakshmigarments.model.Skill;

@Repository
public interface SkillRepository extends JpaRepository<Skill, Long> {

}
