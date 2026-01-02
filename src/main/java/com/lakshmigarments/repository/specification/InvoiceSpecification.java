package com.lakshmigarments.repository.specification;

import com.lakshmigarments.model.Bale;
import com.lakshmigarments.model.Invoice;
import com.lakshmigarments.model.LorryReceipt;
import com.lakshmigarments.model.Supplier;
import com.lakshmigarments.model.Transport;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.Date;
import java.util.List;

public class InvoiceSpecification {
	

    public static Specification<Invoice> filterByInvoiceNumber(String invoiceNumber) {
        return (root, query, criteriaBuilder) -> {
            if (invoiceNumber != null && !invoiceNumber.isEmpty()) {
                return criteriaBuilder.like(root.get("invoiceNumber"), "%" + invoiceNumber + "%");
            }
            return criteriaBuilder.conjunction();
        };
    }

    public static Specification<Invoice> filterBySupplierNames(List<String> supplierNames) {
        return (root, query, criteriaBuilder) -> {
            if (supplierNames != null && !supplierNames.isEmpty()) {
                Join<Invoice, Supplier> supplierJoin = root.join("supplier", JoinType.INNER);
                Predicate[] predicates = supplierNames.stream()
                        .map(supplierName -> criteriaBuilder.equal(supplierJoin.get("name"), supplierName))
                        .toArray(Predicate[]::new);
                return criteriaBuilder.or(predicates);
            }
            return criteriaBuilder.conjunction();
        };
    }
    
    public static Specification<Invoice> filterByTransportNames(List<String> transportNames) {
        return (root, query, criteriaBuilder) -> {
            if (transportNames != null && !transportNames.isEmpty()) {
                Join<Invoice, Transport> supplierJoin = root.join("transport", JoinType.INNER);
                Predicate[] predicates = transportNames.stream()
                        .map(transportName -> criteriaBuilder.equal(supplierJoin.get("name"), transportName))
                        .toArray(Predicate[]::new);
                return criteriaBuilder.or(predicates);
            }
            return criteriaBuilder.conjunction();
        };
    }

    public static Specification<Invoice> filterByIsPaid(List<Boolean> isPaidList) {
        return (root, query, criteriaBuilder) -> {
            if (isPaidList != null && !isPaidList.isEmpty()) {
                // Create a predicate for each value in the list and combine them using OR
                Predicate[] predicates = isPaidList.stream()
                        .map(isPaid -> criteriaBuilder.equal(root.get("isPaid"), isPaid))
                        .toArray(Predicate[]::new);
                return criteriaBuilder.or(predicates); // Use OR to match any of the values
            }
            return criteriaBuilder.conjunction(); // Return true if no values are provided
        };
    }


    public static Specification<Invoice> filterByInvoiceDateBetween(Date startDate, Date endDate) {
        return (root, query, criteriaBuilder) -> {
            if (startDate != null && endDate != null) {
                return criteriaBuilder.between(root.get("invoiceDate"), startDate, endDate);
            }
            return criteriaBuilder.conjunction(); // No filter applied if dates are null
        };
    }
    
    public static Specification<Invoice> filterByReceivedDateBetween(Date startDate, Date endDate) {
        return (root, query, criteriaBuilder) -> {
            if (startDate != null && endDate != null) {
                return criteriaBuilder.between(root.get("receivedDate"), startDate, endDate);
            }
            return criteriaBuilder.conjunction(); // No filter applied if dates are null
        };
    }
    
    public static Specification<Invoice> filterByBaleNumber(String baleNumber) {
        return (root, query, cb) -> {
            if (baleNumber == null || baleNumber.isEmpty()) return null;

            // Join invoice -> lorryReceipts
            Join<Invoice, LorryReceipt> lrJoin = root.join("lorryReceipts", JoinType.LEFT);

            // Join lorryReceipt -> bales
            Join<LorryReceipt, Bale> baleJoin = lrJoin.join("bales", JoinType.LEFT);

            // Make query distinct to avoid duplicates
            query.distinct(true);

            return cb.like(cb.lower(baleJoin.get("baleNumber")), "%" + baleNumber.toLowerCase() + "%");
        };
    }

}
