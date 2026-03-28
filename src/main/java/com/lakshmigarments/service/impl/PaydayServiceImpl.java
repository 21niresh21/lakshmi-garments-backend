package com.lakshmigarments.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.lakshmigarments.dto.PaydayDTO;
import com.lakshmigarments.model.Damage;
import com.lakshmigarments.model.DamageType;
import com.lakshmigarments.model.Employee;
import com.lakshmigarments.model.JobworkReceipt;
import com.lakshmigarments.model.JobworkReceiptItem;
import com.lakshmigarments.repository.EmployeeRepository;
import com.lakshmigarments.repository.JobworkReceiptRepository;
import com.lakshmigarments.repository.JobworkRepository;
import com.lakshmigarments.service.PaydayService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaydayServiceImpl implements PaydayService {

	private final Logger LOGGER = LoggerFactory.getLogger(PaydayServiceImpl.class);

	private final JobworkReceiptRepository receiptRepository;
	private final JobworkRepository jobworkRepository;
	private final EmployeeRepository employeeRepository;

	@Override
	public Page<PaydayDTO> getAllPayday(String employeeName, LocalDateTime fromDate, LocalDateTime toDate,
			Pageable pageable) {

		LOGGER.info("Fetching payday summary | employeeName={}, fromDate={}, toDate={}", employeeName, fromDate,
				toDate);

		// Normalize employeeName - convert empty string to null
		String employeeNameFilter = (employeeName != null && employeeName.trim().isEmpty()) ? null : employeeName;

		// Fetch all employees (or specific employee if filter provided)
		List<Employee> employees;
		if (employeeNameFilter != null) {
			Employee emp = employeeRepository.findByName(employeeNameFilter).orElse(null);
			if (emp != null) {
				employees = List.of(emp);
			} else {
				LOGGER.warn("Employee not found: {}", employeeNameFilter);
				employees = new ArrayList<>();
			}
		} else {
			// Get all employees
			employees = employeeRepository.findAll();
		}

		LOGGER.debug("Found {} employees in system", employees.size());

		// Fetch all jobwork receipts for CLOSED jobworks
		List<JobworkReceipt> receipts = receiptRepository.getJobworkReceipts(employeeNameFilter);
		LOGGER.debug("Fetched {} jobwork receipts for closed jobworks", receipts.size());

		// Filter by date range if provided
		if (fromDate != null || toDate != null) {
			receipts = receipts.stream()
					.filter(r -> {
						LocalDateTime createdAt = r.getCreatedAt();
						if (fromDate != null && createdAt.isBefore(fromDate)) {
							return false;
						}
						if (toDate != null && createdAt.isAfter(toDate)) {
							return false;
						}
						return true;
					})
					.collect(Collectors.toList());
			LOGGER.debug("After date filtering: {} receipts", receipts.size());
		}

		// Group receipts by employee name
		Map<String, List<JobworkReceipt>> receiptsByEmployee = receipts.stream()
				.collect(Collectors.groupingBy(r -> r.getJobwork().getAssignedTo() != null ? r.getJobwork().getAssignedTo().getName() : "Unassigned"));

		LOGGER.debug("Grouped receipts into {} employees with receipts", receiptsByEmployee.size());

		// Calculate payday data for ALL employees (including those with 0 wages)
		List<PaydayDTO> paydayList = new ArrayList<>();

		for (Employee employee : employees) {
			String empName = employee.getName();
			List<JobworkReceipt> empReceipts = receiptsByEmployee.getOrDefault(empName, new ArrayList<>());

			PaydayDTO payday = calculateEmployeePayday(empName, empReceipts);
			paydayList.add(payday);

			LOGGER.debug("Employee: {}, Completed Jobworks: {}, Pending Jobworks: {}, Net Wage: {}",
					empName, payday.getCompletedJobworkCount(), payday.getPendingJobworkCount(), payday.getNetWage());
		}

		// Apply sorting and pagination
		paydayList = applySorting(paydayList, pageable);

		int start = (int) pageable.getOffset();
		int end = Math.min((start + pageable.getPageSize()), paydayList.size());

		List<PaydayDTO> pageContent = paydayList.subList(start, end);

		LOGGER.info("Returning {} employees with payday data (page {} of {}), Total employees: {}",
				pageContent.size(), pageable.getPageNumber() + 1,
				(int) Math.ceil((double) paydayList.size() / pageable.getPageSize()),
				paydayList.size());
		
		LOGGER.debug("check dog {}", paydayList.size());
		return new PageImpl<>(pageContent, pageable, paydayList.size());
	}

	private PaydayDTO calculateEmployeePayday(String employeeName, List<JobworkReceipt> receipts) {
		PaydayDTO payday = new PaydayDTO();
		payday.setEmployeeName(employeeName);
		payday.setCompletedJobworkCount((long) receipts.size());

		// Get pending jobwork count (not CLOSED or REASSIGNED)
		Long pendingCount = jobworkRepository.countPendingJobworksByEmployeeName(employeeName);
		payday.setPendingJobworkCount(pendingCount != null ? pendingCount : 0L);

		// Initialize counters
		long totalAcceptedQuantity = 0;
		double grossWage = 0.0;
		long salesQuantity = 0;
		double salesDeduction = 0.0;
		long unrepairableDamageQty = 0;
		double unrepairableDamageDeduction = 0.0;
		long repairableDamageQty = 0;
		long supplierDamageQty = 0;

		// Calculate totals from all receipt items
		for (JobworkReceipt receipt : receipts) {
			for (JobworkReceiptItem item : receipt.getJobworkReceiptItems()) {
				// Accepted quantity and gross wage
				Long acceptedQty = item.getAcceptedQuantity() != null ? item.getAcceptedQuantity() : 0L;
				Double wagePerItem = item.getWagePerItem() != null ? item.getWagePerItem() : 0.0;
				totalAcceptedQuantity += acceptedQty;
				grossWage += acceptedQty * wagePerItem;

				// Sales deduction
				Long salesQty = item.getSalesQuantity() != null ? item.getSalesQuantity() : 0L;
				Double salesPrice = item.getSalesPrice() != null ? item.getSalesPrice() : 0.0;
				salesQuantity += salesQty;
				salesDeduction += salesQty * salesPrice;

				// Process damages
				for (Damage damage : item.getDamages()) {
					Long damageQty = damage.getQuantity() != null ? damage.getQuantity() : 0L;
					
					if (damage.getDamageType() == DamageType.UNREPAIRABLE) {
						unrepairableDamageQty += damageQty;
						// Deduct unrepairable damages at sales price
						unrepairableDamageDeduction += damageQty * salesPrice;
					} else if (damage.getDamageType() == DamageType.REPAIRABLE) {
						repairableDamageQty += damageQty;
					} else if (damage.getDamageType() == DamageType.SUPPLIER_DAMAGE) {
						supplierDamageQty += damageQty;
					}
				}
			}
		}

		// Calculate net wage
		double netWage = grossWage - salesDeduction - unrepairableDamageDeduction;

		// Set all values
		payday.setTotalAcceptedQuantity(totalAcceptedQuantity);
		payday.setGrossWage(grossWage);
		payday.setSalesQuantity(salesQuantity);
		payday.setSalesDeduction(salesDeduction);
		payday.setUnrepairableDamageQuantity(unrepairableDamageQty);
		payday.setUnrepairableDamageDeduction(unrepairableDamageDeduction);
		payday.setRepairableDamageQuantity(repairableDamageQty);
		payday.setSupplierDamageQuantity(supplierDamageQty);
		payday.setNetWage(netWage);

		return payday;
	}

	private List<PaydayDTO> applySorting(List<PaydayDTO> paydayList, Pageable pageable) {
		if (pageable.getSort().isUnsorted()) {
			return paydayList;
		}

		return paydayList.stream()
				.sorted((p1, p2) -> {
					String sortProperty = pageable.getSort().iterator().next().getProperty();
					boolean isAscending = pageable.getSort().iterator().next().isAscending();

					int comparison = 0;
					switch (sortProperty) {
						case "e.name":
						case "employeeName":
							comparison = p1.getEmployeeName().compareTo(p2.getEmployeeName());
							break;
						case "netWage":
							comparison = Double.compare(p1.getNetWage(), p2.getNetWage());
							break;
						case "grossWage":
							comparison = Double.compare(p1.getGrossWage(), p2.getGrossWage());
							break;
						case "completedJobworkCount":
							comparison = Long.compare(p1.getCompletedJobworkCount(), p2.getCompletedJobworkCount());
							break;
						case "pendingJobworkCount":
							comparison = Long.compare(p1.getPendingJobworkCount(), p2.getPendingJobworkCount());
							break;
						default:
							comparison = p1.getEmployeeName().compareTo(p2.getEmployeeName());
					}

					return isAscending ? comparison : -comparison;
				})
				.collect(Collectors.toList());
	}
}
