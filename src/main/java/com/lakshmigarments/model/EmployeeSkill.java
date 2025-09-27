package com.lakshmigarments.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "employee_skills", 
	uniqueConstraints = @UniqueConstraint(columnNames = { "employee_id", "skill_id" }))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeSkill {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "employee_id", nullable = false)
	Employee employee;

	@ManyToOne
	@JoinColumn(name = "skill_id", nullable = false)
	Skill skill;

}
