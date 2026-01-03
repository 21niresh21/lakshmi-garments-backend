package com.lakshmigarments.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.lakshmigarments.model.Invoice;

import jakarta.persistence.Tuple;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long>, JpaSpecificationExecutor<Invoice> {

	@Query(value = "SELECT COUNT(*) FROM invoices i, lorry_receipts lr WHERE "
			+ "i.id = lr.invoice_id AND i.id = ?1", nativeQuery = true)
	Integer findCountOfLorryReceiptsByInvoiceID(Long id);
	
	@Query(value = "SELECT COUNT(*) FROM invoices i, lorry_receipts lr, bales b WHERE "
			+ "i.id = lr.invoice_id AND lr.id = b.lorry_receipt_id AND i.id = ?1", nativeQuery = true)
	Integer findCountOfBalesByInvoiceID(Long id);
	
	@Query(value = "SELECT SUM(b.quantity),SUM(b.price) FROM invoices i, lorry_receipts lr, bales b WHERE "
			+ "i.id = lr.invoice_id AND lr.id = b.lorry_receipt_id AND i.id = ?1", nativeQuery = true)
	Tuple getTotalQuantityAndValue(Long id);
	
	@Query(value = """
		    SELECT DISTINCT c.name
		    FROM invoices i
		    JOIN lorry_receipts lr ON i.id = lr.invoice_id
		    JOIN bales b ON b.lorry_receipt_id = lr.id
		    JOIN sub_categories sc ON b.sub_category_id = sc.id
		    JOIN categories c ON b.category_id = c.id
		    WHERE i.id = ?1
		""", nativeQuery = true)
		List<String> findDistinctCategories(Long invoiceId);

	
	@Query(value = "SELECT DISTINCT(sc.name) FROM invoices i, lorry_receipts lr, bales b, sub_categories sc WHERE "
			+ "i.id = lr.invoice_id AND lr.id = b.lorry_receipt_id AND b.sub_category_id = sc.id AND i.id = ?1", nativeQuery = true)
	List<String> findDistinctSubCategories(Long id);
	
	@Query(value = "SELECT DISTINCT(b.quality) FROM invoices i, lorry_receipts lr, bales b WHERE "
			+ "i.id = lr.invoice_id AND lr.id = b.lorry_receipt_id AND i.id = ?1", nativeQuery = true)
	List<String> findDistinctQualities(Long id);
	
	@Query(value = "SELECT DISTINCT(b.length) FROM invoices i, lorry_receipts lr, bales b WHERE "
			+ "i.id = lr.invoice_id AND lr.id = b.lorry_receipt_id AND i.id = ?1", nativeQuery = true)
	List<Double> findDistinctLengths(Long id);
	
	boolean existsByInvoiceNumberAndSupplierName(String invNo, String supplierName);
	
	
}
