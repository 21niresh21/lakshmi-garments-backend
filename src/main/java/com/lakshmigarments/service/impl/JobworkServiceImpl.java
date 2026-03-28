package com.lakshmigarments.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.lakshmigarments.dto.JobworkItemDTO;
import com.lakshmigarments.dto.JobworkResponseDTO;
import com.lakshmigarments.dto.request.CreateCuttingJobworkRequest;
import com.lakshmigarments.dto.request.CreateJobworkRequest;
import com.lakshmigarments.dto.request.CreateItemBasedJobworkRequest;
import com.lakshmigarments.dto.response.EmployeeJobworkResponse;
import com.lakshmigarments.dto.response.ItemResponse;
import com.lakshmigarments.dto.response.DetailedEmployeeJobworkResponse;
import com.lakshmigarments.dto.response.EmployeeJobworkReportResponse;
import com.lakshmigarments.dto.response.JobworkDetailDTO;
import com.lakshmigarments.dto.response.JobworkItemResponse;
import com.lakshmigarments.dto.JobworkTimelineResponse;
import com.lakshmigarments.dto.TimelineEventType;
import com.lakshmigarments.dto.TimelineItemDetail;
import com.lakshmigarments.dto.response.JobworkResponse;
import com.lakshmigarments.model.Batch;
import com.lakshmigarments.model.BatchItem;
import com.lakshmigarments.model.BatchStatus;
import com.lakshmigarments.model.Damage;
import com.lakshmigarments.model.DamageType;
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
import com.lakshmigarments.model.SubCategory;
import com.lakshmigarments.exception.BatchItemNotFoundException;
import com.lakshmigarments.exception.BatchNotFoundException;
import com.lakshmigarments.exception.EmployeeNotFoundException;
import com.lakshmigarments.exception.ItemNotFoundException;
import com.lakshmigarments.exception.JobworkNotFoundException;
import com.lakshmigarments.exception.JobworkTypeNotFoundException;
import com.lakshmigarments.exception.InsufficientBatchQuantityException;
import com.lakshmigarments.repository.BatchItemRepository;
import com.lakshmigarments.repository.BatchRepository;
import com.lakshmigarments.repository.DamageRepository;
import com.lakshmigarments.repository.EmployeeRepository;
import com.lakshmigarments.repository.ItemRepository;
import com.lakshmigarments.repository.JobworkItemRepository;
import com.lakshmigarments.repository.JobworkReceiptRepository;
import com.lakshmigarments.repository.JobworkRepository;
import com.lakshmigarments.repository.specification.JobworkSpecification;
import com.lakshmigarments.service.BatchService;
import com.lakshmigarments.service.JobworkService;
import com.lakshmigarments.service.validation.JobworkCreationValidator;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JobworkServiceImpl implements JobworkService<CreateJobworkRequest> {

	private static final Logger LOGGER = LoggerFactory.getLogger(JobworkServiceImpl.class);
	private final JobworkRepository jobworkRepository;
	private final EmployeeRepository employeeRepository;
	private final ItemRepository itemRepository;
	private final ModelMapper modelMapper;
	private final BatchRepository batchRepository;
	private final JobworkItemRepository jobworkItemRepository;
	private final JobworkReceiptRepository jobworkReceiptRepository;
	private final BatchItemRepository batchItemRepository;
	private final DamageRepository damageRepository;
	private final JobworkCreationValidator jobworkCreationValidator;
	private final BatchService batchService;

	@Override
	public Page<JobworkResponseDTO> getAllJobworks(Pageable pageable, String search, List<String> assignedToNames,
			List<JobworkStatus> statuses, List<JobworkType> types, List<String> batchSerialCodes,
			LocalDateTime startDate, LocalDateTime endDate) {

		// 1. Build Dynamic Specification
		Specification<Jobwork> spec = Specification.where(null);

		// Global Search (Case-insensitive Jobwork Number)
		if (search != null && !search.trim().isEmpty()) {
			spec = spec.and(JobworkSpecification.filterUniqueByJobworkNumber(search.trim()));
		}

		// Filter by Multiple Employee Names
		if (assignedToNames != null && !assignedToNames.isEmpty()) {
			spec = spec.and(JobworkSpecification.assignedToNamesIn(assignedToNames));
		}

		// Filter by Multiple Statuses
		if (statuses != null && !statuses.isEmpty()) {
			spec = spec.and(JobworkSpecification.hasStatuses(statuses));
		}

		// Filter by Multiple Jobwork Types
		if (types != null && !types.isEmpty()) {
			spec = spec.and(JobworkSpecification.hasJobworkTypes(types));
		}

		// Filter by Multiple Batch Serial Codes
		if (batchSerialCodes != null && !batchSerialCodes.isEmpty()) {
			spec = spec.and(JobworkSpecification.batchSerialCodesIn(batchSerialCodes));
		}

		// Date Range Filter
		if (startDate != null || endDate != null) {
			spec = spec.and(JobworkSpecification.assignedBetween(startDate, endDate));
		}

		// 2. Fetch Jobworks using Specification
		Page<Jobwork> jobworks = jobworkRepository.findAll(spec, pageable);

		// 3. Optimized Receipt Fetching (Batching to prevent N+1)
		List<String> jobworkNumbers = jobworks.getContent().stream().map(Jobwork::getJobworkNumber).toList();

		Map<String, List<JobworkReceipt>> receiptsByJobworkNumber = new HashMap<>();
		if (!jobworkNumbers.isEmpty()) {
			List<JobworkReceipt> receipts = jobworkReceiptRepository.findByJobworkJobworkNumberIn(jobworkNumbers);
			receiptsByJobworkNumber = receipts.stream()
					.collect(Collectors.groupingBy(r -> r.getJobwork().getJobworkNumber()));
		}

		// 4. Convert to DTO
		List<JobworkResponseDTO> jobworkResponseDTOs = convertToJobworkResponseDTO(jobworks.getContent(),
				receiptsByJobworkNumber);

		LOGGER.info("Fetched {} filtered jobworks on page {}", jobworkResponseDTOs.size(), pageable.getPageNumber());
		return new PageImpl<>(jobworkResponseDTOs, pageable, jobworks.getTotalElements());
	}

	@Override
	@Transactional
	public JobworkResponse createJobwork(CreateJobworkRequest request) {
		LOGGER.debug("Creating a new jobwork");

		Employee employee = getEmployeeOrThrow(request.getAssignedTo());
		Batch batch = getBatchOrThrow(request.getBatchSerialCode());

		if (request instanceof CreateCuttingJobworkRequest cuttingRequest) {

			LOGGER.debug("Cutting jobwork creation request received for batch {}", cuttingRequest.getBatchSerialCode());

			// Validate batch has enough available quantity
			Long availableQuantity = batch.getAvailableQuantity();
			Long requestedQuantity = cuttingRequest.getQuantity();
			
			if (availableQuantity < requestedQuantity) {
				LOGGER.error("Insufficient batch quantity. Available: {}, Requested: {}", availableQuantity, requestedQuantity);
				throw new InsufficientBatchQuantityException(
					"Insufficient quantity in batch. Available: " + availableQuantity + ", Requested: " + requestedQuantity);
			}

			LOGGER.debug("Validated cutting jobwork - Batch has sufficient quantity");
			return this.createCuttingJobwork(cuttingRequest, employee, batch);

		} else if (request instanceof CreateItemBasedJobworkRequest itemJobworkRequest) {

			LOGGER.debug("{} jobwork creation request received", itemJobworkRequest.getJobworkType());

			for (int i = 0; i < itemJobworkRequest.getItemNames().size(); i++) {

				String itemName = itemJobworkRequest.getItemNames().get(i);
				Long requestedQuantity = itemJobworkRequest.getQuantities().get(i);

				BatchItem batchItem = this.getBatchItemOrThrow(itemJobworkRequest.getBatchSerialCode(), itemName);

				Long batchItemQuantity = batchItem.getQuantity();

				Long assignedQuantity = jobworkRepository.getAssignedQuantities(itemJobworkRequest.getBatchSerialCode(),
						itemJobworkRequest.getJobworkType().name(), itemName);

				Long repairableDamages = damageRepository.getDamagedQuantity(itemJobworkRequest.getBatchSerialCode(),
						DamageType.REPAIRABLE.name(), itemJobworkRequest.getJobworkType().name(), itemName);

				jobworkCreationValidator.validateItemQuantityAvailability(batchItemQuantity, assignedQuantity,
						repairableDamages, requestedQuantity, itemName, itemJobworkRequest.getJobworkType());

				LOGGER.debug("Validated item {} successfully", itemName);
			}

			return this.createItemBasedJobwork(itemJobworkRequest, employee, batch);
		}

		LOGGER.error("Unsupported jobwork type {}", request.getJobworkType());
		throw new JobworkTypeNotFoundException("Unsupported Jobwork Type");
	}

	private Employee getEmployeeOrThrow(String employeeName) {
		return employeeRepository.findByName(employeeName).orElseThrow(() -> {
			LOGGER.error("Employee with name {} not found", employeeName);
			return new EmployeeNotFoundException("Employee not found with name " + employeeName);
		});
	}

	private Batch getBatchOrThrow(String serialCode) {
		return batchRepository.findBySerialCode(serialCode).orElseThrow(() -> {
			LOGGER.error("Batch with serial code {} not found", serialCode);
			return new BatchNotFoundException("Batch not found with serial code " + serialCode);
		});
	}

	private Item getItemOrThrow(String itemName) {
		return itemRepository.findByName(itemName).orElseThrow(() -> {
			LOGGER.error("Item with name {} not found", itemName);
			return new ItemNotFoundException("Item not found with name " + itemName);
		});
	}

	private BatchItem getBatchItemOrThrow(String batchSerialCode, String itemName) {
		return batchItemRepository.findByBatchSerialCodeAndItemName(batchSerialCode, itemName).orElseThrow(() -> {
			LOGGER.error("Batch item not found for batch {} and item {}", batchSerialCode, itemName);
			return new BatchItemNotFoundException("Batch item not found for batch " + batchSerialCode + " and item " + itemName);
		});
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

		List<JobworkReceipt> receipts = jobworkReceiptRepository
				.findByJobworkJobworkNumberIn(Arrays.asList(jobworkNumber));
		List<JobworkReceiptItem> receiptItems = new ArrayList<>();



		long returnedQuantity = 0;
		for (JobworkReceipt jobworkReceipt : receipts) {
			List<JobworkReceiptItem> itemReceiptItems = jobworkReceipt.getJobworkReceiptItems();
			for (JobworkReceiptItem jobworkReceiptItem : itemReceiptItems) {
				receiptItems.add(jobworkReceiptItem);
			}

		}

		JobworkDetailDTO dto = new JobworkDetailDTO();
		dto.setStartedAt(jobwork.getCreatedAt());
		dto.setAssignedBy(jobwork.getCreatedBy());
		dto.setAssignedTo(jobwork.getAssignedTo() != null ? jobwork.getAssignedTo().getName() : "Unassigned");
		dto.setBatchSerialCode(jobwork.getBatch().getSerialCode());
		dto.setJobworkNumber(jobworkNumber);
		dto.setJobworkOrigin(jobwork.getJobworkOrigin().name());
		dto.setJobworkType(jobwork.getJobworkType().name());
		dto.setRemarks(jobwork.getRemarks());
		dto.setJobworkItems(jobworkItemDTOs);

		List<JobworkItemResponse> receiptItemDTOs = receipts.stream().flatMap(r -> r.getJobworkReceiptItems().stream())
				.map(this::toReceiptItemDTO).toList();

		dto.setJobworkReceiptItems(receiptItemDTOs);
		dto.setJobworkStatus(jobwork.getJobworkStatus().toString());

		return dto;
	}

	private JobworkItemResponse toReceiptItemDTO(JobworkReceiptItem item) {
		JobworkItemResponse jobworkItemResponse = new JobworkItemResponse();

		jobworkItemResponse.setItemName(item.getItem() != null ? item.getItem().getName() : "Unknown");
		jobworkItemResponse.setAcceptedQuantity(item.getAcceptedQuantity());
		jobworkItemResponse.setSalesQuantity(item.getSalesQuantity());
		jobworkItemResponse.setSalesPrice(item.getSalesPrice());
		jobworkItemResponse.setWagePerItem(item.getWagePerItem());
		jobworkItemResponse.setDamagedQuantity(item.getDamagedQuantity());
		return jobworkItemResponse;
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


	private List<JobworkResponseDTO> convertToJobworkResponseDTO(List<Jobwork> jobworks,
			Map<String, List<JobworkReceipt>> receiptsByJobworkNumber) {

		return jobworks.stream().map(jobwork -> mapToDTO(jobwork,
				receiptsByJobworkNumber.getOrDefault(jobwork.getJobworkNumber(), List.of()))).toList();
	}

	private JobworkResponseDTO mapToDTO(Jobwork jobwork, List<JobworkReceipt> receipts) {
		JobworkResponseDTO jobworkResponseDTO = new JobworkResponseDTO();
		jobworkResponseDTO.setId(jobwork.getId());
		jobworkResponseDTO.setAssignedTo(jobwork.getAssignedTo() != null ? jobwork.getAssignedTo().getName() : "Unassigned");
		jobworkResponseDTO.setBatchSerial(jobwork.getBatch().getSerialCode());
		jobworkResponseDTO.setJobworkType(jobwork.getJobworkType().toString());
		jobworkResponseDTO.setJobworkNumber(jobwork.getJobworkNumber());
		jobworkResponseDTO.setStartedAt(jobwork.getCreatedAt());

		List<JobworkItem> jobworkItems = jobwork.getJobworkItems();
		long totalIssuedQty = jobwork.getJobworkItems().stream().mapToLong(JobworkItem::getQuantity).sum();

		JobworkStatus status = JobworkStatus.CLOSED;
		int completedCount = 0, inProgressCount = 0;
		for (JobworkItem jobworkItem : jobworkItems) {
			if (jobworkItem.getJobworkItemStatus() == JobworkItemStatus.IN_PROGRESS) {
				inProgressCount += 1;
			} else if (jobworkItem.getJobworkItemStatus() == JobworkItemStatus.CLOSED) {
				completedCount += 1;
			}
		}

		// deduct the submitted quantities for the jobwork
		long returnedQuantity = 0;
		for (JobworkReceipt jobworkReceipt : receipts) {
			List<JobworkReceiptItem> receiptItems = jobworkReceipt.getJobworkReceiptItems();
			for (JobworkReceiptItem jobworkReceiptItem : receiptItems) {
				returnedQuantity += jobworkReceiptItem.getDamagedQuantity() + jobworkReceiptItem.getSalesQuantity()
						+ jobworkReceiptItem.getAcceptedQuantity();
			}

		}

		// evaluate jobwork status
		if (completedCount == jobworkItems.size()) {
			status = JobworkStatus.CLOSED;
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
		dto.setStatus(item.getJobworkItemStatus().name());

		// For CUTTING jobwork, item will be null and subCategory will be set
		// For STITCHING/PACKAGING jobwork, item will be set and subCategory will be null
		if (item.getItem() != null) {
			dto.setItemName(item.getItem().getName());
		} else if (item.getSubCategory() != null) {
			dto.setItemName(item.getSubCategory().getName());
		}

		if (item.getJobwork() != null) {
			dto.setJobworkNumber(item.getJobwork().getJobworkNumber());
		}

		return dto;
	}

	@Override
	@Transactional
	public Jobwork reAssignJobwork(String jobworkNumber, String employeeName) {
		LOGGER.debug("Reassigning jobwork {}", jobworkNumber);
		Jobwork oldJobwork = this.getJobworkOrThrow(jobworkNumber);
		Employee employee = this.getEmployeeOrThrow(employeeName);

		Jobwork newJobwork = new Jobwork();
		newJobwork.setBatch(oldJobwork.getBatch());
		newJobwork.setAssignedTo(employee);
		newJobwork.setJobworkNumber(this.getNextJobworkNumber());
		newJobwork.setJobworkOrigin(JobworkOrigin.REASSIGNED);
		newJobwork.setJobworkStatus(JobworkStatus.IN_PROGRESS);
		newJobwork.setJobworkType(oldJobwork.getJobworkType());
		newJobwork.setRemarks(oldJobwork.getRemarks());
		newJobwork.setParentJobwork(oldJobwork);

		// 🔹 CLONE jobwork items
		List<JobworkItem> clonedItems = oldJobwork.getJobworkItems().stream().map(oldItem -> {
			JobworkItem item = new JobworkItem();
			item.setItem(oldItem.getItem());
			item.setSubCategory(oldItem.getSubCategory());
			item.setQuantity(oldItem.getQuantity());
			item.setJobworkItemStatus(oldItem.getJobworkItemStatus());
			item.setJobwork(newJobwork); // parent set
			return item;
		}).toList();

		newJobwork.setJobworkItems(clonedItems);

		Jobwork savedJobwork = jobworkRepository.save(newJobwork);

		// 🔹 Mark old jobwork as reassigned
		oldJobwork.setJobworkStatus(JobworkStatus.REASSIGNED);
		jobworkRepository.save(oldJobwork);
		LOGGER.debug("Marked the reassigned jobwork {} to {}", jobworkNumber, JobworkStatus.REASSIGNED);

		return savedJobwork;
	}

	private JobworkResponse createCuttingJobwork(CreateCuttingJobworkRequest request, Employee employee, Batch batch) {

		Jobwork jobwork = new Jobwork();
		jobwork.setJobworkNumber(request.getJobworkNumber());
		jobwork.setJobworkType(request.getJobworkType());
		jobwork.setAssignedTo(employee);
		jobwork.setBatch(batch);
		jobwork.setRemarks(request.getRemarks());
		jobwork.setJobworkOrigin(JobworkOrigin.ORIGINAL);
		jobwork.setJobworkStatus(JobworkStatus.IN_PROGRESS);
		Jobwork createdJobwork = jobworkRepository.save(jobwork);
		LOGGER.debug("Created new jobwork {}", createdJobwork.getJobworkNumber());

		// Create a single jobwork item with the quantity (no subcategory for simplified cutting)
		JobworkItem jobworkItem = new JobworkItem();
		jobworkItem.setJobwork(createdJobwork);
		jobworkItem.setQuantity(request.getQuantity());
		jobworkItem.setJobworkItemStatus(JobworkItemStatus.IN_PROGRESS);
		jobworkItemRepository.save(jobworkItem);
		LOGGER.debug("Created new jobwork item for cutting - Qty: {}", request.getQuantity());

		// Update batch available quantity
		batch.setAvailableQuantity(batch.getAvailableQuantity() - request.getQuantity());
		batch.setBatchStatus(BatchStatus.ASSIGNED);
		batchRepository.save(batch);
		LOGGER.debug("Batch {} status changed to ASSIGNED, available quantity reduced by {}", batch.getSerialCode(), request.getQuantity());

		JobworkResponse mappedResponse = modelMapper.map(createdJobwork, JobworkResponse.class);
		mappedResponse.setAssignedTo(createdJobwork.getAssignedTo() != null ? createdJobwork.getAssignedTo().getName() : "Unassigned");
		mappedResponse.setBatchSerialCode(createdJobwork.getBatch().getSerialCode());
		return mappedResponse;
	}

	private JobworkResponse createItemBasedJobwork(CreateItemBasedJobworkRequest request, Employee employee,
			Batch batch) {

		Jobwork jobwork = new Jobwork();
		jobwork.setJobworkNumber(request.getJobworkNumber());
		jobwork.setJobworkType(request.getJobworkType());
		jobwork.setAssignedTo(employee);
		jobwork.setBatch(batch);
		jobwork.setRemarks(request.getRemarks());
		jobwork.setJobworkOrigin(JobworkOrigin.ORIGINAL);
		jobwork.setJobworkStatus(JobworkStatus.IN_PROGRESS);
		Jobwork createdJobwork = jobworkRepository.save(jobwork);
		LOGGER.debug("Created new jobwork {}", createdJobwork.getJobworkNumber());

		int i = 0;
		for (String itemName : request.getItemNames()) {
			Item item = this.getItemOrThrow(itemName);
			JobworkItem jobworkItem = new JobworkItem();
			jobworkItem.setJobwork(createdJobwork);
			jobworkItem.setItem(item);
			jobworkItem.setQuantity(request.getQuantities().get(i));
			jobworkItem.setJobworkItemStatus(JobworkItemStatus.IN_PROGRESS);
			jobworkItemRepository.save(jobworkItem);

			LOGGER.debug("Created new jobwork item for jobwork {}, item {}", createdJobwork.getJobworkNumber(),
					itemName);
			i += 1;
		}

		batch.setBatchStatus(BatchStatus.ASSIGNED);
		batchRepository.save(batch);
		LOGGER.debug("Batch {} status changed to ASSIGNED", batch.getSerialCode());

		JobworkResponse mappedResponse = modelMapper.map(createdJobwork, JobworkResponse.class);
		mappedResponse.setAssignedTo(createdJobwork.getAssignedTo() != null ? createdJobwork.getAssignedTo().getName() : "Unassigned");
		mappedResponse.setBatchSerialCode(createdJobwork.getBatch().getSerialCode());
		return mappedResponse;
	}

	@Override
	public JobworkResponse closeJobwork(String jobworkNumber) {
		LOGGER.debug("Closing jobwork {}", jobworkNumber);

		Jobwork jobwork = this.getJobworkOrThrow(jobworkNumber);
		jobwork.setJobworkStatus(JobworkStatus.CLOSED);
		Jobwork savedJobwork = jobworkRepository.save(jobwork);
		LOGGER.debug("Closed jobwork {}", jobworkNumber);

		jobwork.getJobworkItems().forEach(jobworkItem -> jobworkItem.setJobworkItemStatus(JobworkItemStatus.CLOSED));

		batchService.recalculateBatchStatus(jobwork.getBatch());

		JobworkResponse mappedJobworkResponse = modelMapper.map(savedJobwork, JobworkResponse.class);
		mappedJobworkResponse.setBatchSerialCode(savedJobwork.getBatch().getSerialCode());
		mappedJobworkResponse.setAssignedTo(savedJobwork.getAssignedTo() != null ? savedJobwork.getAssignedTo().getName() : "Unassigned");
		return mappedJobworkResponse;

	}

	@Override
	public JobworkResponse reopenJobwork(String jobworkNumber) {
		LOGGER.debug("Reopening jobwork {}", jobworkNumber);

		Jobwork jobwork = this.getJobworkOrThrow(jobworkNumber);
		jobwork.setJobworkStatus(JobworkStatus.AWAITING_CLOSE);
		Jobwork savedJobwork = jobworkRepository.save(jobwork);
		LOGGER.debug("Reopened jobwork {}", jobworkNumber);

		jobwork.getJobworkItems()
				.forEach(jobworkItem -> jobworkItem.setJobworkItemStatus(JobworkItemStatus.AWAITING_CLOSE));

		batchService.recalculateBatchStatus(jobwork.getBatch());

		JobworkResponse mappedJobworkResponse = modelMapper.map(savedJobwork, JobworkResponse.class);
		mappedJobworkResponse.setBatchSerialCode(savedJobwork.getBatch().getSerialCode());
		mappedJobworkResponse.setAssignedTo(savedJobwork.getAssignedTo() != null ? savedJobwork.getAssignedTo().getName() : "Unassigned");
		return mappedJobworkResponse;

	}

	@Override
	public List<ItemResponse> getItemsForJobwork(String jobworkNumber) {

		Jobwork jobwork = this.getJobworkOrThrow(jobworkNumber);
		List<JobworkItem> jobworkItems = jobwork.getJobworkItems();

		List<ItemResponse> itemResponses = new ArrayList<>();

		long i = 1;
		for (JobworkItem jobworkItem : jobworkItems) {
			ItemResponse itemResponse = new ItemResponse();
			itemResponse.setId(i);
			// For CUTTING jobwork, item will be null - use subCategory name instead
			itemResponse.setName(getJobworkItemName(jobworkItem));
			i += 1;
			itemResponses.add(itemResponse);
		}

		return itemResponses;
	}

	@Override
	public EmployeeJobworkReportResponse getDetailedJobworksByEmployee(String employeeName, LocalDateTime startDate,
			LocalDateTime endDate) {
		LOGGER.info("Generating detailed jobwork report for employee: {} from {} to {}", employeeName, startDate, endDate);

		// 1. Build Specification for filtering
		Specification<Jobwork> spec = Specification.where(JobworkSpecification.assignedToNamesIn(List.of(employeeName)));
		if (startDate != null || endDate != null) {
			spec = spec.and(JobworkSpecification.assignedBetween(startDate, endDate));
		}

		// 2. Fetch Jobworks
		List<Jobwork> jobworks = jobworkRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "createdAt"));
		LOGGER.debug("Found {} jobworks for filtering criteria", jobworks.size());

		if (jobworks.isEmpty()) {
			return EmployeeJobworkReportResponse.builder()
					.jobworks(new ArrayList<>())
					.stats(EmployeeJobworkReportResponse.OverallStats.builder()
							.totalJobworks(0L)
							.totalIssuedQuantity(0L)
							.totalAcceptedQuantity(0L)
							.totalDamagedQuantity(0L)
							.totalSalesQuantity(0L)
							.damageBreakdown(new HashMap<>())
							.build())
					.build();
		}

		// 3. Optimized Receipt & Damage Fetching
		List<String> jobworkNumbers = jobworks.stream().map(Jobwork::getJobworkNumber).toList();
		List<JobworkReceipt> receipts = jobworkReceiptRepository.findByJobworkJobworkNumberIn(jobworkNumbers);
		
		Map<String, List<JobworkReceiptItem>> receiptItemsByJobworkNumber = receipts.stream()
				.flatMap(r -> r.getJobworkReceiptItems().stream())
				.collect(Collectors.groupingBy(ri -> ri.getJobworkReceipt().getJobwork().getJobworkNumber()));

		// 4. Transform to Detailed Responses and Calculate Stats
		List<DetailedEmployeeJobworkResponse> detailedResponses = new ArrayList<>();
		
		long overallIssued = 0, overallAccepted = 0, overallDamaged = 0, overallSales = 0;
		Map<String, Long> overallDamageBreakdown = new HashMap<>();

		for (Jobwork jw : jobworks) {
			String jwNum = jw.getJobworkNumber();
			List<JobworkReceiptItem> riList = receiptItemsByJobworkNumber.getOrDefault(jwNum, List.of());
			
			// Group receipt items by item name for easier consolidation
			Map<String, List<JobworkReceiptItem>> riByItem = riList.stream()
					.collect(Collectors.groupingBy(ri -> ri.getItem() != null ? ri.getItem().getName() : jw.getJobworkType().name()));

			List<DetailedEmployeeJobworkResponse.ItemDetail> itemDetails = new ArrayList<>();
			
			for (JobworkItem jwi : jw.getJobworkItems()) {
				String itemName = getJobworkItemName(jwi);
				List<JobworkReceiptItem> itemReceipts = riByItem.getOrDefault(itemName, List.of());
				
				long issued = jwi.getQuantity();
				long accepted = itemReceipts.stream().mapToLong(JobworkReceiptItem::getAcceptedQuantity).sum();
				long damaged = itemReceipts.stream().mapToLong(JobworkReceiptItem::getDamagedQuantity).sum();
				long sales = itemReceipts.stream().mapToLong(JobworkReceiptItem::getSalesQuantity).sum();
				
				Map<String, Long> itemDamageBreakdown = new HashMap<>();
				itemReceipts.stream()
						.flatMap(ri -> ri.getDamages().stream())
						.forEach(d -> {
							String type = d.getDamageType().name();
							itemDamageBreakdown.put(type, itemDamageBreakdown.getOrDefault(type, 0L) + d.getQuantity());
							overallDamageBreakdown.put(type, overallDamageBreakdown.getOrDefault(type, 0L) + d.getQuantity());
						});

				itemDetails.add(DetailedEmployeeJobworkResponse.ItemDetail.builder()
						.itemName(itemName)
						.issuedQuantity(issued)
						.acceptedQuantity(accepted)
						.damagedQuantity(damaged)
						.salesQuantity(sales)
						.salesPrice(itemReceipts.isEmpty() ? 0.0 : itemReceipts.get(0).getSalesPrice())
						.wagePerItem(itemReceipts.isEmpty() ? 0.0 : itemReceipts.get(0).getWagePerItem())
						.status(jwi.getJobworkItemStatus().name())
						.damageBreakdown(itemDamageBreakdown)
						.build());
				
				overallIssued += issued;
				overallAccepted += accepted;
				overallDamaged += damaged;
				overallSales += sales;
			}

			detailedResponses.add(DetailedEmployeeJobworkResponse.builder()
					.jobworkNumber(jwNum)
					.jobworkType(jw.getJobworkType().name())
					.jobworkStatus(jw.getJobworkStatus().name())
					.batchSerialCode(jw.getBatch().getSerialCode())
					.startedAt(jw.getCreatedAt())
					.lastUpdatedAt(jw.getLastModifiedAt())
					.remarks(jw.getRemarks())
					.items(itemDetails)
					.build());
		}

		EmployeeJobworkReportResponse response = EmployeeJobworkReportResponse.builder()
				.jobworks(detailedResponses)
				.stats(EmployeeJobworkReportResponse.OverallStats.builder()
						.totalJobworks((long) jobworks.size())
						.totalIssuedQuantity(overallIssued)
						.totalAcceptedQuantity(overallAccepted)
						.totalDamagedQuantity(overallDamaged)
						.totalSalesQuantity(overallSales)
						.damageBreakdown(overallDamageBreakdown)
						.build())
				.build();

		LOGGER.info("Successfully generated report with {} jobworks and {} total pieces issued", jobworks.size(), overallIssued);
		return response;
	}

	@Override
	public List<EmployeeJobworkResponse> getJobworksByEmployeeName(String employeeName) {
		LOGGER.info("Fetching all jobworks for employee: {}", employeeName);

		// Fetch all jobworks assigned to the employee
		List<Jobwork> jobworks = jobworkRepository.findByAssignedToNameOrderByCreatedAtDesc(employeeName);
		LOGGER.debug("Found {} jobworks for employee: {}", jobworks.size(), employeeName);

		if (jobworks.isEmpty()) {
			LOGGER.warn("No jobworks found for employee: {}", employeeName);
			return new ArrayList<>();
		}

		// Convert to response DTO
		List<EmployeeJobworkResponse> responses = new ArrayList<>();

		for (Jobwork jobwork : jobworks) {
			EmployeeJobworkResponse response = new EmployeeJobworkResponse();
			response.setJobworkNumber(jobwork.getJobworkNumber());
			response.setJobworkType(jobwork.getJobworkType() != null ? jobwork.getJobworkType().name() : null);
			response.setJobworkStatus(jobwork.getJobworkStatus() != null ? jobwork.getJobworkStatus().name() : null);
			response.setBatchSerialCode(jobwork.getBatch() != null ? jobwork.getBatch().getSerialCode() : null);
			response.setStartedAt(jobwork.getCreatedAt());
			response.setUpdatedAt(jobwork.getLastModifiedAt());
			response.setRemarks(jobwork.getRemarks());

			// Fetch jobwork items (pieces/items issued to this jobwork)
			List<JobworkItem> jobworkItems = jobwork.getJobworkItems();
			List<EmployeeJobworkResponse.JobworkItemDetail> itemDetails = new ArrayList<>();

			for (JobworkItem jobworkItem : jobworkItems) {
				EmployeeJobworkResponse.JobworkItemDetail itemDetail = new EmployeeJobworkResponse.JobworkItemDetail();
				itemDetail.setItemName(getJobworkItemName(jobworkItem));
				itemDetail.setQuantity(jobworkItem.getQuantity());
				itemDetails.add(itemDetail);
			}

			response.setItems(itemDetails);
			responses.add(response);

			LOGGER.debug("Processed jobwork {} with {} items", jobwork.getJobworkNumber(), itemDetails.size());
		}

		LOGGER.info("Successfully fetched {} jobworks for employee: {}", responses.size(), employeeName);
		return responses;
	}
	@Override
	public JobworkTimelineResponse getJobworkTimeline(String jobworkNumber) {
		LOGGER.info("Fetching timeline for jobwork number: {}", jobworkNumber);
		Jobwork jobwork = getJobworkOrThrow(jobworkNumber);

		List<JobworkItem> jobworkItems = jobworkItemRepository.findAllByJobwork(jobwork);
		List<JobworkReceipt> receipts = jobworkReceiptRepository.findByJobworkJobworkNumberIn(Arrays.asList(jobworkNumber));
		receipts.sort(Comparator.comparing(JobworkReceipt::getCreatedAt));

		// ─── Build Item Summary (Cumulative Progress) ─────────
		Map<String, JobworkTimelineResponse.JobworkItemSummary> itemProgress = new HashMap<>();
		
		// Initialize with issued quantities
		for (JobworkItem ji : jobworkItems) {
			String name = getJobworkItemName(ji);
			itemProgress.put(name, JobworkTimelineResponse.JobworkItemSummary.builder()
				.itemName(name)
				.issuedQuantity(ji.getQuantity())
				.acceptedQuantity(0L)
				.damagedQuantity(0L)
				.salesQuantity(0L)
				.pendingQuantity(ji.getQuantity())
				.status(ji.getJobworkItemStatus().toString())
				.build());
		}

		// Update with receipt data
		long totalAccepted = 0, totalDamaged = 0, totalSales = 0;
		double totalWages = 0.0, totalSalesAmt = 0.0;
		List<JobworkTimelineResponse.ReceiptDetail> receiptDetails = new ArrayList<>();
		List<JobworkTimelineResponse.TimelineEvent> timelineEvents = new ArrayList<>();

		// 1. Initial Assignment Event
		long totalAssignedQty = jobworkItems.stream().mapToLong(ji -> ji.getQuantity() != null ? ji.getQuantity() : 0L).sum();
		String currentMsg;
		if (jobwork.getJobworkOrigin() == JobworkOrigin.REASSIGNED && jobwork.getParentJobwork() != null) {
			String parentEmployee = jobwork.getParentJobwork().getAssignedTo() != null ? jobwork.getParentJobwork().getAssignedTo().getName() : "Unknown";
			currentMsg = String.format("Jobwork reassigned from %s to %s for %s with %d items", 
				parentEmployee, jobwork.getAssignedTo().getName(), jobwork.getJobworkType(), totalAssignedQty);
		} else {
			currentMsg = String.format("Jobwork assigned for %s to %s with %d items", 
				jobwork.getJobworkType(), jobwork.getAssignedTo().getName(), totalAssignedQty);
		}

		timelineEvents.add(JobworkTimelineResponse.TimelineEvent.builder()
			.eventType(TimelineEventType.JOBWORK_ASSIGNED)
			.message(currentMsg)
			.performedAt(jobwork.getCreatedAt())
			.performedBy(jobwork.getCreatedBy())
			.stage(jobwork.getJobworkStatus().toString())
			.timeTakenFromPrevious("N/A")
			.quantityAffected(totalAssignedQty)
			.items(jobworkItems.stream().map(ji -> {
				TimelineItemDetail tid = new TimelineItemDetail();
				tid.setItemName(getJobworkItemName(ji));
				tid.setQuantity(ji.getQuantity());
				return tid;
			}).collect(Collectors.toList()))
			.build());

		// 2. Process Receipts
		for (JobworkReceipt receipt : receipts) {
			long rAccepted = 0, rDamaged = 0, rSales = 0;
			double rWages = 0.0;
			List<JobworkTimelineResponse.ReceiptItemDetail> rItems = new ArrayList<>();

			for (JobworkReceiptItem jwri : receipt.getJobworkReceiptItems()) {
				String name = jwri.getItem() != null ? jwri.getItem().getName() : "Unknown";
				long acc = jwri.getAcceptedQuantity() != null ? jwri.getAcceptedQuantity() : 0L;
				long dmg = jwri.getDamagedQuantity() != null ? jwri.getDamagedQuantity() : 0L;
				long sal = jwri.getSalesQuantity() != null ? jwri.getSalesQuantity() : 0L;
				
				rAccepted += acc; rDamaged += dmg; rSales += sal;
				if (jwri.getWagePerItem() != null) rWages += (jwri.getWagePerItem() * acc);
				if (jwri.getSalesPrice() != null) totalSalesAmt += (jwri.getSalesPrice() * sal);

				// Deduct for unrepairable damages
				double itemDeduction = 0.0;
				if (jwri.getSalesPrice() != null && jwri.getDamages() != null) {
					for (Damage d : jwri.getDamages()) {
						if (d.getDamageType() == DamageType.UNREPAIRABLE) {
							itemDeduction += d.getQuantity() * jwri.getSalesPrice();
						}
					}
				}
				rWages -= itemDeduction;

				// Update cumulative progress
				if (itemProgress.containsKey(name)) {
					JobworkTimelineResponse.JobworkItemSummary summary = itemProgress.get(name);
					summary.setAcceptedQuantity(summary.getAcceptedQuantity() + acc);
					summary.setDamagedQuantity(summary.getDamagedQuantity() + dmg);
					summary.setSalesQuantity(summary.getSalesQuantity() + sal);
					summary.setPendingQuantity(summary.getPendingQuantity() - (acc + dmg + sal));
				}

				// Build damage details
				List<JobworkTimelineResponse.DamageDetail> dDetails = jwri.getDamages().stream().map(d -> 
					JobworkTimelineResponse.DamageDetail.builder()
						.quantity(d.getQuantity())
						.damageType(d.getDamageType().toString())
						.reworkJobworkNumber(d.getReworkJobWork() != null ? d.getReworkJobWork().getJobworkNumber() : null)
						.build()
				).collect(Collectors.toList());

				rItems.add(JobworkTimelineResponse.ReceiptItemDetail.builder()
					.itemName(name)
					.acceptedQuantity(acc).damagedQuantity(dmg).salesQuantity(sal)
					.wagePerItem(jwri.getWagePerItem()).salesPrice(jwri.getSalesPrice())
					.damages(dDetails)
					.build());
			}

			totalAccepted += rAccepted; totalDamaged += rDamaged; totalSales += rSales; totalWages += rWages;

			receiptDetails.add(JobworkTimelineResponse.ReceiptDetail.builder()
				.receiptId(receipt.getId())
				.receivedAt(receipt.getCreatedAt())
				.recordedBy(receipt.getCreatedBy())
				.receiptItems(rItems)
				.totalAccepted(rAccepted).totalDamaged(rDamaged).totalSales(rSales).receiptWages(rWages)
				.build());

			timelineEvents.add(JobworkTimelineResponse.TimelineEvent.builder()
				.eventType(TimelineEventType.JOBWORK_RECEIPT)
				.message(String.format("Submission received: Accepted %d, Damaged %d, Sales %d (Recorded by %s)", 
					rAccepted, rDamaged, rSales, receipt.getCreatedBy()))
				.performedAt(receipt.getCreatedAt())
				.performedBy(receipt.getCreatedBy())
				.stage("SUBMITTED")
				.quantityAffected(rAccepted + rDamaged + rSales)
				.timeTakenFromPrevious(com.lakshmigarments.utility.TimeDifferenceUtil.formatDuration(
					timelineEvents.get(timelineEvents.size() - 1).getPerformedAt(), receipt.getCreatedAt()))
				.items(rItems.stream().map(ri -> {
					TimelineItemDetail tid = new TimelineItemDetail();
					tid.setItemName(ri.getItemName());
					tid.setAcceptedQuantity(ri.getAcceptedQuantity());
					tid.setDamagedQuantity(ri.getDamagedQuantity());
					tid.setSalesQuantity(ri.getSalesQuantity());
					return tid;
				}).collect(Collectors.toList()))
				.build());
		}

		// 3. Final Status Change Event
		if (!jobwork.getJobworkStatus().equals(JobworkStatus.IN_PROGRESS)) {
			String action;
			JobworkStatus currentStat = jobwork.getJobworkStatus();
			if (currentStat == JobworkStatus.CLOSED) action = "CLOSED";
			else if (currentStat == JobworkStatus.REASSIGNED) action = "REASSIGNED";
			else if (currentStat == JobworkStatus.AWAITING_CLOSE) action = "PENDING APPROVAL";
			else action = currentStat.toString();

			timelineEvents.add(JobworkTimelineResponse.TimelineEvent.builder()
				.eventType(TimelineEventType.JOBWORK_COMPLETED)
				.message(String.format("Jobwork %s by %s", action.toLowerCase(), jobwork.getLastModifiedBy()))
				.performedAt(jobwork.getLastModifiedAt())
				.performedBy(jobwork.getLastModifiedBy())
				.stage(currentStat.toString())
				.timeTakenFromPrevious(com.lakshmigarments.utility.TimeDifferenceUtil.formatDuration(
					timelineEvents.get(timelineEvents.size() - 1).getPerformedAt(), jobwork.getLastModifiedAt()))
				.build());
		}

		// Sort timeline chronologically
		timelineEvents.sort(Comparator.comparing(JobworkTimelineResponse.TimelineEvent::getPerformedAt));

		// ─── Metrics ──────────────────────────────────────────
		long totalReturned = totalAccepted + totalDamaged + totalSales;
		String completion = totalAssignedQty > 0 ? (Math.round((double)totalReturned / totalAssignedQty * 100)) + "%" : "0%";

		JobworkTimelineResponse.JobworkMetrics metrics = JobworkTimelineResponse.JobworkMetrics.builder()
			.totalIssued(totalAssignedQty)
			.totalAccepted(totalAccepted)
			.totalDamaged(totalDamaged)
			.totalSales(totalSales)
			.totalPending(totalAssignedQty - totalReturned)
			.totalWagesEarned(totalWages)
			.totalSalesDeduction(totalSalesAmt)
			.completionPercentage(completion)
			.build();

		return JobworkTimelineResponse.builder()
			.id(jobwork.getId())
			.jobworkNumber(jobworkNumber)
			.jobworkType(jobwork.getJobworkType().toString())
			.jobworkStatus(jobwork.getJobworkStatus().toString())
			.jobworkOrigin(jobwork.getJobworkOrigin().toString())
			.batchSerialCode(jobwork.getBatch().getSerialCode())
			.assignedTo(jobwork.getAssignedTo() != null ? jobwork.getAssignedTo().getName() : "Unassigned")
			.remarks(jobwork.getRemarks())
			.createdBy(jobwork.getCreatedBy())
			.createdAt(jobwork.getCreatedAt())
			.lastModifiedBy(jobwork.getLastModifiedBy())
			.lastModifiedAt(jobwork.getLastModifiedAt())
			.parentJobworkNumber(jobwork.getParentJobwork() != null ? jobwork.getParentJobwork().getJobworkNumber() : null)
			// .childJobworkNumbers(jobworkRepository.findByParentJobwork(jobwork).stream().map(Jobwork::getJobworkNumber).collect(Collectors.toList()))
			.items(new ArrayList<>(itemProgress.values()))
			.metrics(metrics)
			.receipts(receiptDetails)
			.timeline(timelineEvents)
			.build();
	}

	/**
	 * Helper method to get the name of a JobworkItem.
	 * For CUTTING jobwork, the item will be null and subCategory will be set.
	 * For STITCHING/PACKAGING jobwork, the item will be set and subCategory will be null.
	 */
	private String getJobworkItemName(JobworkItem ji) {
		if (ji.getItem() != null) {
			return ji.getItem().getName();
		} else if (ji.getSubCategory() != null) {
			return ji.getSubCategory().getName();
		}
		return "Unknown";
	}

	private Jobwork getJobworkOrThrow(String jobworkNumber) {
		return jobworkRepository.findByJobworkNumber(jobworkNumber).orElseThrow(() -> {
			LOGGER.error("Jobwork not found: {}", jobworkNumber);
			return new JobworkNotFoundException("Jobwork not found: " + jobworkNumber);
		});
	}

}
