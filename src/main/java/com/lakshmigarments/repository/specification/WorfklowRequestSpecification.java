package com.lakshmigarments.repository.specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.lakshmigarments.model.WorkflowRequest;
import com.lakshmigarments.model.WorkflowRequestStatus;
import com.lakshmigarments.model.WorkflowRequestType;

public class WorfklowRequestSpecification {

	public static Specification<WorkflowRequest> filterByRequestedNames(List<String> requestedByNames) {
		return (root, query, criteriaBuilder) -> {
			if (requestedByNames != null && !requestedByNames.isEmpty()) {
				return root.get("createdBy").in(requestedByNames);
			}
			return criteriaBuilder.conjunction();
		};
	}

	public static Specification<WorkflowRequest> filterByRequestType(List<WorkflowRequestType> types) {
		return (root, query, criteriaBuilder) -> {
			if (types != null && !types.isEmpty()) {
				return root.get("workflowRequestType").in(types);
			}
			return criteriaBuilder.conjunction();
		};
	}

	public static Specification<WorkflowRequest> filterByStatus(List<WorkflowRequestStatus> statuses) {
		return (root, query, criteriaBuilder) -> {
			if (statuses != null && !statuses.isEmpty()) {
				return root.get("workflowRequestStatus").in(statuses);
			}
			return criteriaBuilder.conjunction();
		};
	}

	public static Specification<WorkflowRequest> filterByDateRange(LocalDate startDate, LocalDate endDate) {
		return (root, query, criteriaBuilder) -> {
			if (startDate != null && endDate != null) {
				return criteriaBuilder.between(root.get("createdAt"), startDate, endDate);
			} else if (startDate != null) {
				return criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), startDate);
			} else if (endDate != null) {
				return criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), endDate);
			}
			return criteriaBuilder.conjunction();
		};
	}

}

