package com.lakshmigarments.service.impl;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.lakshmigarments.controller.JobworkController;
import com.lakshmigarments.controller.LoginController;
import com.lakshmigarments.dto.DamageDTO;
import com.lakshmigarments.dto.JobworkDetailDTO;
import com.lakshmigarments.dto.JobworkItemDTO;
import com.lakshmigarments.dto.JobworkReceiptItemDTO;
import com.lakshmigarments.dto.JobworkRequestDTO;
import com.lakshmigarments.dto.JobworkResponseDTO;
import com.lakshmigarments.model.Batch;
import com.lakshmigarments.model.BatchStatus;
import com.lakshmigarments.model.Employee;
import com.lakshmigarments.model.Item;
import com.lakshmigarments.model.Jobwork;
import com.lakshmigarments.model.JobworkItem;
import com.lakshmigarments.model.JobworkOrigin;
import com.lakshmigarments.model.JobworkReceipt;
import com.lakshmigarments.model.JobworkReceiptItem;
import com.lakshmigarments.model.JobworkStatus;
import com.lakshmigarments.model.JobworkItemStatus;
import com.lakshmigarments.model.JobworkType;
import com.lakshmigarments.model.User;
import com.lakshmigarments.exception.BatchNotFoundException;
import com.lakshmigarments.exception.EmployeeNotFoundException;
import com.lakshmigarments.exception.ItemNotFoundException;
import com.lakshmigarments.exception.JobworkNotFoundException;
import com.lakshmigarments.exception.JobworkTypeNotFoundException;
import com.lakshmigarments.exception.UserNotFoundException;
import com.lakshmigarments.repository.BatchRepository;
import com.lakshmigarments.repository.EmployeeRepository;
import com.lakshmigarments.repository.ItemRepository;
import com.lakshmigarments.repository.JobworkItemRepository;
import com.lakshmigarments.repository.JobworkReceiptRepository;
import com.lakshmigarments.repository.JobworkRepository;
import com.lakshmigarments.repository.UserRepository;
import com.lakshmigarments.service.JobworkService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JobworkServiceImpl implements JobworkService {

	private static final Logger LOGGER = LoggerFactory.getLogger(JobworkServiceImpl.class);
	private final JobworkRepository jobworkRepository;
	private final EmployeeRepository employeeRepository;
	private final ItemRepository itemRepository;
	private final ModelMapper modelMapper;
	private final BatchRepository batchRepository;
	private final UserRepository userRepository;
	private final JobworkItemRepository jobworkItemRepository;
	private final JobworkReceiptRepository jobworkReceiptRepository;

	public Page<JobworkResponseDTO> getAllJobworks(Pageable pageable, String search) {

		Page<Jobwork> jobworks;

		// 1. Apply search ONLY on jobworkNumber
		if (search != null && !search.trim().isEmpty()) {
			jobworks = jobworkRepository.findByJobworkNumberContainingIgnoreCase(search.trim(), pageable);
		} else {
			jobworks = jobworkRepository.findAll(pageable);
		}

		List<String> jobworkNumbers = jobworks.getContent().stream().map(Jobwork::getJobworkNumber).toList();

		List<JobworkReceipt> receipts = jobworkReceiptRepository.findByJobworkJobworkNumberIn(jobworkNumbers);

		Map<String, List<JobworkReceipt>> receiptsByJobworkNumber = receipts.stream()
				.collect(Collectors.groupingBy(r -> r.getJobwork().getJobworkNumber()));

		List<JobworkResponseDTO> jobworkResponseDTOs = convertToJobworkResponseDTO(jobworks.getContent(),
				receiptsByJobworkNumber);

		LOGGER.info("Fetched {} jobworks", jobworkResponseDTOs.size());
		return new PageImpl<>(jobworkResponseDTOs, pageable, jobworks.getTotalElements());

	}

	@Override
	@Transactional
	public Jobwork createJobwork(JobworkRequestDTO jobworkRequestDTO) {

		Employee employee = employeeRepository.findById(jobworkRequestDTO.getEmployeeId()).orElseThrow(() -> {
			LOGGER.error("Employee with ID {} not found", jobworkRequestDTO.getEmployeeId());
			return new EmployeeNotFoundException("Employee not found with ID " + jobworkRequestDTO.getEmployeeId());
		});

		User user = userRepository.findById(jobworkRequestDTO.getAssignedBy()).orElseThrow(() -> {
			LOGGER.error("User with ID {} not found", jobworkRequestDTO.getAssignedBy());
			return new UserNotFoundException("User not found with ID " + jobworkRequestDTO.getAssignedBy());
		});

		Batch batch = batchRepository.findBySerialCode(jobworkRequestDTO.getBatchSerialCode()).orElseThrow(() -> {
			LOGGER.error("Batch with serial code {} not found", jobworkRequestDTO.getBatchSerialCode());
			return new BatchNotFoundException(
					"Batch not found with serial code " + jobworkRequestDTO.getBatchSerialCode());
		});

		// CUTTING
		if (jobworkRequestDTO.getJobworkType() == JobworkType.CUTTING) {

			// TODO to return correct object
			if (jobworkRequestDTO.getQuantities().size() <= 0) {
				return null;
			}
			Long quantity = jobworkRequestDTO.getQuantities().get(0);

			batch.setAvailableQuantity(batch.getAvailableQuantity() - quantity);
			batch.setBatchStatus(BatchStatus.ASSIGNED);

			Jobwork jobwork = new Jobwork();
			jobwork.setAssignedBy(user);
			jobwork.setEmployee(employee);
			jobwork.setBatch(batch);
			jobwork.setJobworkType(jobworkRequestDTO.getJobworkType());
			jobwork.setJobworkNumber(jobworkRequestDTO.getJobworkNumber());
			jobwork.setJobworkOrigin(JobworkOrigin.ORIGINAL);
			jobwork.setJobworkStatus(JobworkStatus.IN_PROGRESS);
			jobwork.setRemarks(jobworkRequestDTO.getRemarks());
			Jobwork createdJobwork = jobworkRepository.save(jobwork);

			JobworkItem jobworkItem = new JobworkItem();
			jobworkItem.setJobwork(createdJobwork);
			jobworkItem.setQuantity(quantity);
			jobworkItem.setJobworkStatus(JobworkItemStatus.IN_PROGRESS);
			jobworkItemRepository.save(jobworkItem);

			return createdJobwork;
		}

//        Item item = null;
//        if (jobworkRequestDTO.getItemId() != null) {
//            item = itemRepository.findById(jobworkRequestDTO.getItemId()).orElseThrow(() -> {
//                LOGGER.error("Item with ID {} not found", jobworkRequestDTO.getItemId());
//                return new ItemNotFoundException("Item not found with ID " + jobworkRequestDTO.getItemId());
//            });
//        }

		Jobwork jobwork = new Jobwork();
		jobwork.setBatch(batch);
		jobwork.setEmployee(employee);
		jobwork.setJobworkType(jobworkRequestDTO.getJobworkType());
		jobwork.setJobworkNumber(jobworkRequestDTO.getJobworkNumber());
		jobwork.setJobworkOrigin(JobworkOrigin.ORIGINAL);
		jobwork.setAssignedBy(user);
		jobworkRepository.save(jobwork);

		return null;
	}

	@Override
	public List<String> getJobworkNumbers(String search) {
		LOGGER.debug("Fetching jobwork numbers with search: {}", search);
		return jobworkRepository.findUniqueJobworksByJobworkNumber().stream().map(Jobwork::getJobworkNumber)
				.collect(Collectors.toList());
	}

	@Override
	public JobworkDetailDTO getJobworkDetail(String jobworkNumber) {

		LOGGER.debug("Fetching jobwork detail for jobwork number: {}", jobworkNumber);

		Jobwork jobwork = jobworkRepository.findByJobworkNumber(jobworkNumber).orElseThrow(() -> {
			LOGGER.error("Jobwork with number {} not found", jobworkNumber);
			return new JobworkNotFoundException("Jobwork with number " + jobworkNumber + " not found");
		});

		List<JobworkItem> jobworkItems = jobworkItemRepository.findAllByJobwork(jobwork);

		// ✅ Convert entities → DTOs using ModelMapper
		List<JobworkItemDTO> jobworkItemDTOs = jobworkItems.stream().map(this::jwTojwDTO).toList();
		
		List<JobworkReceipt> receipts = jobworkReceiptRepository.findByJobworkJobworkNumberIn(Arrays.asList(jobworkNumber));
		List<JobworkReceiptItem> receiptItems = new ArrayList<>();
		
		System.out.println(receipts.size());
		
		long returnedQuantity = 0;
		for (JobworkReceipt jobworkReceipt : receipts) {
			List<JobworkReceiptItem> itemReceiptItems = jobworkReceipt.getJobworkReceiptItems();
			for (JobworkReceiptItem jobworkReceiptItem : itemReceiptItems) {
				receiptItems.add(jobworkReceiptItem);
			}
					
		}

		JobworkDetailDTO dto = new JobworkDetailDTO();
		dto.setStartedAt(jobwork.getStartedAt());
		dto.setAssignedBy(jobwork.getAssignedBy().getName());
		dto.setAssignedTo(jobwork.getEmployee().getName());
		dto.setBatchSerialCode(jobwork.getBatch().getSerialCode());
		dto.setJobworkNumber(jobworkNumber);
		dto.setJobworkOrigin(jobwork.getJobworkOrigin().name());
		dto.setJobworkType(jobwork.getJobworkType().name());
		dto.setRemarks(jobwork.getRemarks());
		dto.setJobworkItems(jobworkItemDTOs);
		
		List<JobworkReceiptItemDTO> receiptItemDTOs = receipts.stream()
			    .flatMap(r -> r.getJobworkReceiptItems().stream())
			    .map(this::toReceiptItemDTO)
			    .toList();
		
		dto.setJobworkReceiptItems(receiptItemDTOs);
		dto.setJobworkStatus(jobwork.getJobworkStatus().toString());

		return dto;
	}
	
	private JobworkReceiptItemDTO toReceiptItemDTO(JobworkReceiptItem item) {
	    JobworkReceiptItemDTO dto = new JobworkReceiptItemDTO();

	    dto.setItemName(item.getItem().getName());
	    dto.setReturnedQuantity(item.getReceivedQuantity());
	    dto.setPurchasedQuantity(item.getPurchaseQuantity());
	    dto.setPurchaseCost(item.getPurchaseRate());
	    dto.setWage(item.getWagePerItem());
	    dto.setDamagedQuantity(item.getDamagedQuantity());
	    return dto;
	}


	public String getNextJobworkNumber() {

		// Format: yyyyMMdd (e.g. 20251228)
		String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

		Jobwork lastJobwork = jobworkRepository.findTop1ByOrderByJobworkNumberDesc().orElse(null);

		int nextSequence = 1;

		if (lastJobwork != null) {
			String lastNumber = lastJobwork.getJobworkNumber();

			// Expected format: JW-yyyyMMdd-XXX
			String[] parts = lastNumber.split("-");

			if (parts.length == 3 && parts[1].equals(today)) {
				nextSequence = Integer.parseInt(parts[2]) + 1;
			}
		}

		return "JW-" + today + "-" + String.format("%03d", nextSequence);
	}

////    private JobworkDetailDTO convertToJobworkDetailDTO(List<Jobwork> jobworks) {
////
////        List<String> items = new ArrayList<>();
////        List<Long> quantity = new ArrayList<>();
////
////        for (Jobwork jobwork : jobworks) {
////            if (jobwork.getItem() != null) {
////                items.add(jobwork.getItem().getName());
////            }
////            if (jobwork.getQuantity() != null) {
////                quantity.add(jobwork.getQuantity());
////            }
////        }
////
////        JobworkDetailDTO jobworkDetailDTO = new JobworkDetailDTO();
////        jobworkDetailDTO.setJobworkNumber(jobworks.get(0).getJobworkNumber());
////        jobworkDetailDTO.setStartedAt(jobworks.get(0).getStartedAt());
////        jobworkDetailDTO.setJobworkType(jobworks.get(0).getJobworkType());
////        jobworkDetailDTO.setBatchSerialCode(jobworks.get(0).getBatch().getSerialCode());
////        jobworkDetailDTO.setAssignedTo(jobworks.get(0).getEmployee().getName());
////        jobworkDetailDTO.setItems(items);
////        jobworkDetailDTO.setQuantity(quantity);
////        return jobworkDetailDTO;
////
////    }
//
	private List<JobworkResponseDTO> convertToJobworkResponseDTO(List<Jobwork> jobworks,
			Map<String, List<JobworkReceipt>> receiptsByJobworkNumber) {

		return jobworks.stream().map(jobwork -> mapToDTO(jobwork,
				receiptsByJobworkNumber.getOrDefault(jobwork.getJobworkNumber(), List.of()))).toList();
	}

	private JobworkResponseDTO mapToDTO(Jobwork jobwork, List<JobworkReceipt> receipts) {
		JobworkResponseDTO jobworkResponseDTO = new JobworkResponseDTO();
		jobworkResponseDTO.setId(jobwork.getId());
		jobworkResponseDTO.setAssignedTo(jobwork.getEmployee().getName());
		jobworkResponseDTO.setBatchSerial(jobwork.getBatch().getSerialCode());
		jobworkResponseDTO.setJobworkType(jobwork.getJobworkType().toString());
		jobworkResponseDTO.setJobworkNumber(jobwork.getJobworkNumber());
		jobworkResponseDTO.setStartedAt(jobwork.getStartedAt());

		List<JobworkItem> jobworkItems = jobwork.getJobworkItems();
		long totalIssuedQty = jobwork.getJobworkItems().stream().mapToLong(JobworkItem::getQuantity).sum();

		JobworkStatus status = JobworkStatus.COMPLETED;
		int completedCount = 0, inProgressCount = 0;
		for (JobworkItem jobworkItem : jobworkItems) {
			if (jobworkItem.getJobworkStatus() == JobworkItemStatus.IN_PROGRESS) {
				inProgressCount += 1;
			} else if (jobworkItem.getJobworkStatus() == JobworkItemStatus.COMPLETED) {
				completedCount += 1;
			}
		}
		
		// deduct the submitted quantities for the jobwork
		long returnedQuantity = 0;
		for (JobworkReceipt jobworkReceipt : receipts) {
			List<JobworkReceiptItem> receiptItems = jobworkReceipt.getJobworkReceiptItems();
			for (JobworkReceiptItem jobworkReceiptItem : receiptItems) {
				returnedQuantity += jobworkReceiptItem.getDamagedQuantity() + 
						jobworkReceiptItem.getPurchaseQuantity() + jobworkReceiptItem.getReceivedQuantity();
			}
					
		}

		// evaluate jobwork status
		if (completedCount == jobworkItems.size()) {
			status = JobworkStatus.COMPLETED;
		} else if (completedCount < jobworkItems.size() && completedCount != 0) {
			status = JobworkStatus.PENDING_RETURN;
		} else if (completedCount == 0) {
			status = JobworkStatus.IN_PROGRESS;
		}

		jobworkResponseDTO.setTotalQuantitesIssued(totalIssuedQty);
		jobworkResponseDTO.setStatus(jobwork.getJobworkStatus().toString());
		jobworkResponseDTO.setPendingQuantity(totalIssuedQty - returnedQuantity);

		return jobworkResponseDTO;
	}

	private JobworkItemDTO jwTojwDTO(JobworkItem item) {

		JobworkItemDTO dto = new JobworkItemDTO();
		dto.setId(item.getId());
		dto.setQuantity(item.getQuantity());
		dto.setStatus(item.getJobworkStatus().name());

		if (item.getItem() != null) {
			dto.setItemName(item.getItem().getName());
		}

		if (item.getJobwork() != null) {
			dto.setJobworkNumber(item.getJobwork().getJobworkNumber());
		}

		return dto;
	}

	@Override
	@Transactional
	public Jobwork reAssignJobwork(String jobworkNumber, Long employeeId) {

	    Jobwork jobwork = jobworkRepository
	        .findByJobworkNumber(jobworkNumber)
	        .orElseThrow(() -> new JobworkNotFoundException(
	            "Jobwork with number " + jobworkNumber + " not found"));

	    Employee employee = employeeRepository
	        .findById(employeeId)
	        .orElseThrow(() -> new EmployeeNotFoundException(
	            "Employee not found with ID " + employeeId));

	    // 🔹 Create new Jobwork
	    Jobwork reAssignedJobwork = new Jobwork();
	    reAssignedJobwork.setAssignedBy(jobwork.getAssignedBy());
	    reAssignedJobwork.setBatch(jobwork.getBatch());
	    reAssignedJobwork.setEmployee(employee);
	    reAssignedJobwork.setJobworkNumber(getNextJobworkNumber());
	    reAssignedJobwork.setJobworkOrigin(JobworkOrigin.REASSIGNED);
	    reAssignedJobwork.setJobworkStatus(JobworkStatus.IN_PROGRESS);
	    reAssignedJobwork.setJobworkType(jobwork.getJobworkType());
	    reAssignedJobwork.setRemarks(jobwork.getRemarks());
	    reAssignedJobwork.setReworkJobwork(jobwork);

	    // 🔹 CLONE jobwork items
	    List<JobworkItem> newItems = new ArrayList<>();
	    for (JobworkItem oldItem : jobwork.getJobworkItems()) {
	        JobworkItem newItem = new JobworkItem();
	        newItem.setItem(oldItem.getItem());
	        newItem.setQuantity(oldItem.getQuantity());
	        newItem.setJobwork(reAssignedJobwork); // 🔥 IMPORTANT
	        newItem.setJobworkStatus(oldItem.getJobworkStatus());
	        
	        JobworkItem createdJobworkItem = jobworkItemRepository.save(newItem);
	        newItems.add(createdJobworkItem);
	    }

	    reAssignedJobwork.setJobworkItems(newItems);

	    Jobwork createdJobwork = jobworkRepository.save(reAssignedJobwork);

	    // 🔹 Mark old jobwork as reassigned
	    jobwork.setJobworkStatus(JobworkStatus.REASSIGNED);
	    jobworkRepository.save(jobwork);

	    return createdJobwork;
	}


//	@Override
//	public List<String> getUnfinishedJobworks(String employeeName, String jobworkNumber) {
//		// TODO Auto-generated method stub
//		return null;
//	}

}
