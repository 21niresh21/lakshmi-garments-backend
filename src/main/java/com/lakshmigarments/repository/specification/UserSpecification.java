package com.lakshmigarments.repository.specification;

import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.lakshmigarments.model.Role;
import com.lakshmigarments.model.User;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;

public class UserSpecification {

	public static Specification<User> filterByName(String name) {
		return (root, query, criteriaBuilder) -> criteriaBuilder.like(criteriaBuilder.lower(root.get("name")),
				"%" + name + "%");
	}

	public static Specification<User> filterByRoles(List<String> roleNames) {
		return (root, query, criteriaBuilder) -> {
			Join<User, Role> roles = root.join("role", JoinType.INNER);
			Predicate[] predicates = roleNames.stream()
					.map(roleName -> criteriaBuilder.equal(roles.get("name"), roleName)).toArray(Predicate[]::new);
			return criteriaBuilder.or(predicates);
		};
	}

	public static Specification<User> filterByUserStatus(List<Boolean> userStatuses) {
		return (root, query, criteriaBuilder) -> {
				Predicate[] predicates = userStatuses.stream()
						.map(status -> criteriaBuilder.equal(root.get("isActive"), status)).toArray(Predicate[]::new);
				return criteriaBuilder.or(predicates);
		};
	}

}
