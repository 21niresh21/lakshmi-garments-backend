package com.lakshmigarments.repository.specification;

import com.lakshmigarments.model.*;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;
import java.time.LocalDateTime;
import java.util.List;

public class JobworkSpecification {

	public static Specification<Jobwork> filterUniqueByJobworkNumber(String jobworkNumber) {

		return (root, query, criteriaBuilder) -> {
			query.distinct(true); // ensures distinct results
			String pattern = "%" + jobworkNumber.toLowerCase().trim() + "%";
			return criteriaBuilder.like(criteriaBuilder.lower(root.get("jobworkNumber")), pattern);

		};

	}

	// 1. Filter by assignedTo (Employee ID)
	public static Specification<Jobwork> hasAssignedTo(Long employeeId) {
		return (root, query, cb) -> {
			if (employeeId == null)
				return cb.conjunction();
			return cb.equal(root.get("assignedTo").get("name"), employeeId);
		};
	}

	// Filter for multiple statuses
	public static Specification<Jobwork> hasStatuses(List<JobworkStatus> statuses) {
	    return (root, query, cb) -> {
	        if (statuses == null || statuses.isEmpty()) return cb.conjunction();
	        return root.get("jobworkStatus").in(statuses);
	    };
	}

	// Filter for multiple names (requires a join)
	public static Specification<Jobwork> assignedToNamesIn(List<String> names) {
	    return (root, query, cb) -> {
	        if (names == null || names.isEmpty()) return cb.conjunction();
	        return root.join("assignedTo").get("name").in(names);
	    };
	}

	// Filter for multiple batch codes
	public static Specification<Jobwork> batchSerialCodesIn(List<String> codes) {
	    return (root, query, cb) -> {
	        if (codes == null || codes.isEmpty()) return cb.conjunction();
	        return root.join("batch").get("serialCode").in(codes);
	    };
	}
	
	public static Specification<Jobwork> hasJobworkTypes(List<JobworkType> types) {
	    return (root, query, cb) -> {
	        // If no types are selected, don't filter by type (return all)
	        if (types == null || types.isEmpty()) {
	            return cb.conjunction();
	        }
	        // Generates: WHERE jobwork_type IN ('STITCHING', 'WASHING')
	        return root.get("jobworkType").in(types);
	    };
	}

	// 5. Date Range Filter (Assigned At / Created At)
	// Assuming "assigned at" corresponds to the creation timestamp in BaseAuditable
	public static Specification<Jobwork> assignedBetween(LocalDateTime start, LocalDateTime end) {
		return (root, query, cb) -> {
			if (start == null && end == null)
				return cb.conjunction();
			if (start != null && end == null) {
				return cb.greaterThanOrEqualTo(root.get("createdAt"), start);
			}
			if (start == null && end != null) {
				return cb.lessThanOrEqualTo(root.get("createdAt"), end);
			}
			return cb.between(root.get("createdAt"), start, end);
		};
	}
}