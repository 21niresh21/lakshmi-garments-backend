package com.lakshmigarments.repository.specification;

import java.util.List;
import java.util.Date;

import org.springframework.data.jpa.domain.Specification;

import com.lakshmigarments.model.Batch;
import com.lakshmigarments.model.BatchStatus;
import com.lakshmigarments.model.Category;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;

public class BatchSpecification {

    public static Specification<Batch> filterBySerialCode(String serialCode) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(root.get("serialCode"), "%" + serialCode + "%");
    }

    public static Specification<Batch> filterByBatchStatusName(List<String> batchStatusNames) {

        return (root, query, cb) -> {
            if (batchStatusNames == null || batchStatusNames.isEmpty()) {
                return cb.conjunction();
            }

            List<BatchStatus> statuses = batchStatusNames.stream()
                    .map(String::toUpperCase)
                    .map(BatchStatus::valueOf)
                    .toList();

            return root.get("batchStatus").in(statuses);
        };
    }


    public static Specification<Batch> filterByCategoryName(List<String> categoryNames) {
        return (root, query, criteriaBuilder) -> {
            if (categoryNames != null && !categoryNames.isEmpty()) {
                Join<Batch, Category> categoryJoin = root.join("category", JoinType.INNER);
                Predicate[] predicates = categoryNames.stream()
                        .map(categoryName -> criteriaBuilder.equal(categoryJoin.get("name"), categoryName))
                        .toArray(Predicate[]::new);
                return criteriaBuilder.or(predicates);
            }
            return criteriaBuilder.conjunction();
        };
    }

    public static Specification<Batch> filterByIsUrgent(List<Boolean> isUrgents) {
        return (root, query, criteriaBuilder) -> {
            if (isUrgents != null && !isUrgents.isEmpty()) {
                Predicate[] predicates = isUrgents.stream()
                        .map(isUrgent -> criteriaBuilder.equal(root.get("isUrgent"), isUrgent))
                        .toArray(Predicate[]::new);
                return criteriaBuilder.or(predicates);
            }
            return criteriaBuilder.conjunction();
        };
    }

    // filter by remarks
    public static Specification<Batch> filterByRemarks(String remarks) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(root.get("remarks"), "%" + remarks + "%");
    }

    // date range filters, inclusive range of dates including start and end dates
    public static Date endOfDay(Date date) {
        return new Date(date.getTime() + 24 * 60 * 60 * 1000 - 1); // add 1 day minus 1 ms
    }

    // include what will happen if any of the dates are null
    // if startDate is null, return the specification from jan 1st 1970 till the endDate
    // if endDate is null, return the specification from the startDate till the current date
    // if both are null, return the specification from jan 1st 1970 till the current date
    public static Specification<Batch> filterByDateRange(Date startDate, Date endDate) {
        return (root, query, cb) -> {
            if (startDate == null && endDate != null) {
                return cb.between(root.get("createdAt"), new Date(0), endOfDay(endDate));
            }
            if (startDate != null && endDate == null) {
                return cb.between(root.get("createdAt"), startDate, new Date());
            }
            if (startDate != null && endDate != null) {
                return cb.between(root.get("createdAt"), startDate, endOfDay(endDate));
            }
            return cb.conjunction();
        };
    }

}
