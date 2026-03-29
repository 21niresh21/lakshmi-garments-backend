package com.lakshmigarments.service.impl;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;

import java.util.List;
import java.util.Objects;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.lakshmigarments.context.UserContext;
import com.lakshmigarments.context.UserInfo;

import com.lakshmigarments.dto.BatchDetailDTO;
import com.lakshmigarments.dto.BatchRequestDTO;
import com.lakshmigarments.dto.BatchUpdateDTO;
import com.lakshmigarments.dto.BatchSubCategoryRequestDTO;
import com.lakshmigarments.dto.BatchTimelineResponse;
import com.lakshmigarments.dto.BatchResponseDTO;
import com.lakshmigarments.dto.BatchSerialDTO;
import com.lakshmigarments.dto.TimelineEventType;
import com.lakshmigarments.dto.TimelineItemDetail;
import com.lakshmigarments.dto.BatchResponseDTO.BatchSubCategoryResponseDTO;
import com.lakshmigarments.interceptor.UserContextInterceptor;
import com.lakshmigarments.model.Batch;
import com.lakshmigarments.model.BatchItem;
import com.lakshmigarments.model.BatchStatus;
import com.lakshmigarments.model.BatchSubCategory;
import com.lakshmigarments.model.Category;
import com.lakshmigarments.model.Damage;
import com.lakshmigarments.model.DamageType;
import com.lakshmigarments.model.Employee;
import com.lakshmigarments.model.Jobwork;
import com.lakshmigarments.model.JobworkItem;
import com.lakshmigarments.model.JobworkReceipt;
import com.lakshmigarments.model.JobworkReceiptItem;
import com.lakshmigarments.model.JobworkStatus;
import com.lakshmigarments.model.JobworkType;
import com.lakshmigarments.model.LedgerDirection;
import com.lakshmigarments.model.MaterialInventoryLedger;
import com.lakshmigarments.model.MovementType;
import com.lakshmigarments.model.ReferenceType;
import com.lakshmigarments.model.SubCategory;
import com.lakshmigarments.model.User;
import com.lakshmigarments.exception.BatchNotFoundException;
import com.lakshmigarments.exception.BatchStatusNotFoundException;
import com.lakshmigarments.exception.CategoryNotFoundException;
import com.lakshmigarments.exception.EmployeeNotFoundException;
import com.lakshmigarments.exception.InsufficientInventoryException;
import com.lakshmigarments.exception.InventoryNotFoundException;
import com.lakshmigarments.exception.SubCategoryNotFoundException;
import com.lakshmigarments.exception.UserNotFoundException;
import com.lakshmigarments.repository.BatchRepository;
import com.lakshmigarments.repository.CategoryRepository;
import com.lakshmigarments.repository.BatchSubCategoryRepository;
import com.lakshmigarments.repository.DamageRepository;
import com.lakshmigarments.repository.JobworkReceiptRepository;
import com.lakshmigarments.repository.JobworkRepository;
import com.lakshmigarments.repository.MaterialLedgerRepository;
import com.lakshmigarments.repository.SubCategoryRepository;
import com.lakshmigarments.repository.BatchItemRepository;
import com.lakshmigarments.repository.UserRepository;
import com.lakshmigarments.repository.specification.BatchSpecification;
import com.lakshmigarments.service.BatchService;

import com.lakshmigarments.service.PdfGenerator;
import com.lakshmigarments.utility.DateUtil;
import com.lakshmigarments.utility.TimeDifferenceUtil;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BatchServiceImpl implements BatchService {

	private final PdfGenerator pdfGenerator;

	private final Logger LOGGER = LoggerFactory.getLogger(BatchServiceImpl.class);
	private final BatchRepository batchRepository;
	private final JobworkRepository jobworkRepository;
	private final JobworkReceiptRepository receiptRepository;
	private final BatchSubCategoryRepository batchSubCategoryRepository;
	private final DamageRepository damageRepository;
	private final CategoryRepository categoryRepository;
	private final SubCategoryRepository subCategoryRepository;
	private final UserRepository userRepository;
	private final ModelMapper modelMapper;
	private final MaterialLedgerRepository ledgerRepository;
	private final BatchItemRepository batchItemRepository;

	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy, hh:mm a");

	@Override
	@Transactional
	public void createBatch(BatchRequestDTO batchRequestDTO) {

		Category category = categoryRepository.findByName(batchRequestDTO.getCategoryName()).orElseThrow(() -> {
			LOGGER.error("Category not found with name {}", batchRequestDTO.getCategoryName());
			return new CategoryNotFoundException("Category not found with name " + batchRequestDTO.getCategoryName());
		});



		BatchStatus batchStatus = batchRequestDTO.getBatchStatus() != null ? batchRequestDTO.getBatchStatus()
				: BatchStatus.CREATED;

		List<BatchSubCategory> batchSubCategories = validateBatchSubCategories(batchRequestDTO.getSubCategories());

		Batch batch = new Batch();
		batch.setCategory(category);
		batch.setBatchStatus(batchStatus);
		batch.setSerialCode(batchRequestDTO.getSerialCode());
		batch.setIsUrgent(batchRequestDTO.getIsUrgent());
		batch.setRemarks(batchRequestDTO.getRemarks());
		batch.setQuantity(batchRequestDTO.getTotalQuantity());
		batch.setAvailableQuantity(batchRequestDTO.getTotalQuantity());

		Batch createdBatch = batchRepository.save(batch);

		for (BatchSubCategory batchSubCategory : batchSubCategories) {
			batchSubCategory.setBatch(batch);
			batchSubCategory.setAvailableQuantity(batchSubCategory.getQuantity());
			batchSubCategoryRepository.save(batchSubCategory);

			// detect the quantities from inventory
//			Inventory cachedInventory = inventoryRepository.findBySubCategoryNameAndCategoryName(
//					batchSubCategory.getSubCategory().getName(), category.getName()).orElse(null);
//			if (cachedInventory.getCount() < batchSubCategory.getQuantity()) {
//				throw new InsufficientInventoryException("Stock not available");
//			} else {
//				cachedInventory.setCount(cachedInventory.getCount() - batchSubCategory.getQuantity());
//				inventoryRepository.save(cachedInventory);
//			}

			MaterialInventoryLedger inventory;
			inventory = new MaterialInventoryLedger();
			inventory.setDirection(LedgerDirection.OUT);
			inventory.setMovementType(MovementType.BATCH_CREATION);
			inventory.setReferenceType(ReferenceType.BATCH);
			inventory.setReference_id(createdBatch.getId());
			inventory.setUnit("piece(s)");
			inventory.setQuantity(batchSubCategory.getQuantity());
			inventory.setSubCategory(batchSubCategory.getSubCategory());
			inventory.setCategory(batch.getCategory());

			ledgerRepository.save(inventory);


		}

		return;
	}

	@Override
	public Page<BatchResponseDTO> getAllBatches(Integer pageNo, Integer pageSize, String sortBy, String sortOrder,
			String search, List<String> batchStatusNames, List<String> categoryNames, List<Boolean> isUrgents,
			Date startDate, Date endDate) {

		if (pageNo == null) {
			pageNo = 0;
		}
		if (pageSize == null || pageSize == 0) {
			pageSize = 10;
		}

		Sort sort = sortOrder.equals("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();

		Pageable pageable = PageRequest.of(pageNo, pageSize, sort);

		Specification<Batch> specification = Specification
				.where(BatchSpecification.filterByBatchStatusName(batchStatusNames))
				.and(BatchSpecification.filterByCategoryName(categoryNames))
				.and(BatchSpecification.filterByIsUrgent(isUrgents));

		if (search != null && !search.isEmpty()) {
			Specification<Batch> searchSpecification = Specification.where(null);
			searchSpecification = searchSpecification.or(BatchSpecification.filterBySerialCode(search))
					.or(BatchSpecification.filterByRemarks(search));
			specification = specification.and(searchSpecification);
		}

		if (startDate != null || endDate != null) {
			specification = specification.and(BatchSpecification.filterByDateRange(startDate, endDate));
		}

		Page<Batch> batches = batchRepository.findAll(specification, pageable);

		return batches.map(this::convertToBatchResponseDTO);
	}

	@Override
	public List<BatchSerialDTO> getUnpackagedBatches() {
		LOGGER.info("Fetching unpackaged batches");
		List<Batch> unpackagedBatches = batchRepository.findAllExceptPackagedWithoutRepairableDamages();
		List<BatchSerialDTO> batchSerialDTOs = unpackagedBatches.stream()
				.map(batch -> modelMapper.map(batch, BatchSerialDTO.class)).collect(Collectors.toList());
		LOGGER.info("Found {} unpackaged batches", batchSerialDTOs.size());
		return batchSerialDTOs;
	}

	@Override
	public BatchTimelineResponse getBatchTimeline(Long batchId) {
		Batch batch = batchRepository.findById(batchId).orElseThrow(() -> {
			LOGGER.error("Batch not found with id {}", batchId);
			return new BatchNotFoundException("Batch not found with id " + batchId);
		});

		List<BatchItem> batchItems = batchItemRepository.findByBatchId(batch.getId());
		List<BatchSubCategory> batchSubCategories = batchSubCategoryRepository.findByBatchId(batch.getId());
		List<Jobwork> jobworks = jobworkRepository.findByBatchSerialCode(batch.getSerialCode());
		List<JobworkReceipt> allReceipts = receiptRepository.findByJobworkBatchSerialCode(batch.getSerialCode());

		jobworks.sort(Comparator.comparing(Jobwork::getCreatedAt));
		allReceipts.sort(Comparator.comparing(JobworkReceipt::getCreatedAt));

		// ─── Build Batch Items ────────────────────────────────
		List<BatchTimelineResponse.BatchItemSummary> itemSummaries = batchItems.stream().map(bi ->
			BatchTimelineResponse.BatchItemSummary.builder()
				.itemId(bi.getId())
				.itemName(bi.getItem() != null ? bi.getItem().getName() : "Piece(s)")
				.quantity(bi.getQuantity())
				.build()
		).collect(Collectors.toList());

		// ─── Build Sub-Categories ─────────────────────────────
		List<BatchTimelineResponse.SubCategorySummary> subCatSummaries = batchSubCategories.stream().map(bsc ->
			BatchTimelineResponse.SubCategorySummary.builder()
				.id(bsc.getId())
				.subCategoryName(bsc.getSubCategory() != null ? bsc.getSubCategory().getName() : "Piece(s)")
				.originalQuantity(bsc.getQuantity())
				.availableQuantity(bsc.getAvailableQuantity())
				.build()
		).collect(Collectors.toList());

		// ─── Aggregate Quantity Flow ──────────────────────────
		long totalPreCuttingQuantity = batchSubCategories.stream().mapToLong(bsc -> bsc.getQuantity() != null ?  bsc.getQuantity() : 0L).sum();
		long preCuttingAssigned = 0;
		long preCuttingConsumed = 0;

		long totalPostCuttingQuantity = batchItems.stream().mapToLong(bi -> bi.getQuantity() != null ? bi.getQuantity() : 0L).sum();
		long postCuttingAssigned = 0;
		long postCuttingAccepted = 0;
		long postCuttingDamaged = 0;
		long postCuttingSales = 0;
		long postCuttingRepairable = 0, postCuttingUnrepairable = 0, postCuttingSupplierDamage = 0;

		double totalWages = 0.0, totalSalesAmount = 0.0;
		int totalReceiptCount = 0;

		long cuttingCompleted = 0;
		long embroideryCompleted = 0;
		long stitchingCompleted = 0;
		long packagingCompleted = 0;
		LocalDateTime cuttingStartedAt = null;
		LocalDateTime embroideryStartedAt = null;
		LocalDateTime stitchingStartedAt = null;
		LocalDateTime packagingStartedAt = null;

		// ─── Build Jobwork Summaries + Timeline Events ────────
		List<BatchTimelineResponse.JobworkSummary> jobworkSummaries = new ArrayList<>();
		List<BatchTimelineResponse.TimelineEvent> timelineEvents = new ArrayList<>();

		// 1. BATCH CREATED event
		BatchTimelineResponse.TimelineEvent creationEvent = BatchTimelineResponse.TimelineEvent.builder()
			.eventType(TimelineEventType.BATCH_CREATED)
			.message("Batch created with serial code " + batch.getSerialCode() + " by " + batch.getCreatedBy())
			.performedAt(batch.getCreatedAt())
			.performedBy(batch.getCreatedBy())
			.stage("CREATED")
			.timeTakenFromPrevious("N/A")
			.totalQuantity(totalPreCuttingQuantity)
			.items(batchSubCategories.stream().map(bsc -> {
				TimelineItemDetail tid = new TimelineItemDetail();
				tid.setItemName(bsc.getSubCategory() != null ? bsc.getSubCategory().getName() : "Piece(s)");
				tid.setQuantity(bsc.getQuantity());
				return tid;
			}).collect(Collectors.toList()))
			.build();
		timelineEvents.add(creationEvent);

		// 2. BATCH DISCARDED event (if applicable)
		if (batch.getBatchStatus() == BatchStatus.DISCARDED) {
			BatchTimelineResponse.TimelineEvent discardEvent = BatchTimelineResponse.TimelineEvent.builder()
				.eventType(TimelineEventType.BATCH_DISCARDED)
				.message("Batch discarded by " + batch.getLastModifiedBy())
				.performedAt(batch.getLastModifiedAt())
				.performedBy(batch.getLastModifiedBy())
				.stage(BatchStatus.DISCARDED.toString())
				.timeTakenFromPrevious(TimeDifferenceUtil.formatDuration(batch.getCreatedAt(), batch.getLastModifiedAt()))
				.build();
			timelineEvents.add(discardEvent);
		}
		
		LocalDateTime firstItemReceipt =null;

		// 3. Process each Jobwork and its Receipts
		for (Jobwork jw : jobworks) {
			long jwQty = jw.getJobworkItems().stream()
				.mapToLong(ji -> ji.getQuantity() != null ? ji.getQuantity() : 0L).sum();
			
			if (jw.getJobworkType() == JobworkType.CUTTING) {
				if (cuttingStartedAt == null || jw.getCreatedAt().isBefore(cuttingStartedAt)) cuttingStartedAt = jw.getCreatedAt();
				preCuttingAssigned += jwQty;
				if (jw.getJobworkStatus() == JobworkStatus.CLOSED || jw.getJobworkStatus() == JobworkStatus.AWAITING_CLOSE) {
					preCuttingConsumed += jwQty;
				}
			} else {
				postCuttingAssigned += jwQty;
				if (jw.getJobworkType() == JobworkType.EMBROIDERY) {
					if (embroideryStartedAt == null || jw.getCreatedAt().isBefore(embroideryStartedAt)) embroideryStartedAt = jw.getCreatedAt();
				} else if (jw.getJobworkType() == JobworkType.STITCHING) {
					if (stitchingStartedAt == null || jw.getCreatedAt().isBefore(stitchingStartedAt)) stitchingStartedAt = jw.getCreatedAt();
				} else if (jw.getJobworkType() == JobworkType.PACKAGING) {
					if (packagingStartedAt == null || jw.getCreatedAt().isBefore(packagingStartedAt)) packagingStartedAt = jw.getCreatedAt();
				}
			}

			// Build assigned items
			List<BatchTimelineResponse.JobworkItemDetail> assignedItems = jw.getJobworkItems().stream().map(ji ->
				BatchTimelineResponse.JobworkItemDetail.builder()
					.itemName(ji.getItem() != null ? ji.getItem().getName() : (ji.getSubCategory() != null ? ji.getSubCategory().getName() : "Piece(s)"))
					.quantity(ji.getQuantity())
					.itemStatus(ji.getJobworkItemStatus() != null ? ji.getJobworkItemStatus().toString() : null)
					.build()
			).collect(Collectors.toList());

			// Build assignment timeline event
			String employeeName = jw.getAssignedTo() != null ? jw.getAssignedTo().getName() : "Unassigned";
			BatchTimelineResponse.TimelineEvent assignEvent = BatchTimelineResponse.TimelineEvent.builder()
				.eventType(TimelineEventType.JOBWORK_ASSIGNED)
				.message(String.format("Batch assigned for %s to %s (Jobwork #%s) — %d items",
					jw.getJobworkType(), employeeName, jw.getJobworkNumber(), jwQty))
				.performedAt(jw.getCreatedAt())
				.performedBy(jw.getCreatedBy())
				.stage(jw.getJobworkStatus().toString())
				.jobworkNumber(jw.getJobworkNumber())
				.jobworkType(jw.getJobworkType() != null ? jw.getJobworkType().toString() : null)
				.employeeName(employeeName)
				.totalQuantity(jwQty)
				.timeTakenFromPrevious(TimeDifferenceUtil.formatDuration(
					timelineEvents.get(timelineEvents.size() - 1).getPerformedAt(), jw.getCreatedAt()))
				.items(jw.getJobworkItems().stream().map(ji -> {
					TimelineItemDetail tid = new TimelineItemDetail();
					tid.setItemName(ji.getItem() != null ? ji.getItem().getName() : (ji.getSubCategory() != null ? ji.getSubCategory().getName() : "Piece(s)"));
					tid.setQuantity(ji.getQuantity());
					return tid;
				}).collect(Collectors.toList()))
				.build();
			timelineEvents.add(assignEvent);

			// Process receipts for this jobwork
			List<JobworkReceipt> jwReceipts = allReceipts.stream()
				.filter(r -> r.getJobwork() != null && r.getJobwork().getId().equals(jw.getId()))
				.collect(Collectors.toList());

			long jwAccepted = 0, jwDamaged = 0, jwSales = 0;
			long jwUnrepairable = 0, jwSupplierDamage = 0;
			double jwWages = 0.0, jwSalesAmt = 0.0;
			List<BatchTimelineResponse.ReceiptSummary> receiptSummaries = new ArrayList<>();

			for (JobworkReceipt receipt : jwReceipts) {
				totalReceiptCount++;
				long rAccepted = 0, rDamaged = 0, rSales = 0;
				double rWages = 0.0, rSalesAmt = 0.0;
				List<BatchTimelineResponse.ReceiptItemDetail> receiptItemDetails = new ArrayList<>();

				if (receipt.getJobworkReceiptItems() != null) {
					for (JobworkReceiptItem jwri : receipt.getJobworkReceiptItems()) {
						long acc = jwri.getAcceptedQuantity() != null ? jwri.getAcceptedQuantity() : 0L;
						long dmg = jwri.getDamagedQuantity() != null ? jwri.getDamagedQuantity() : 0L;
						long sal = jwri.getSalesQuantity() != null ? jwri.getSalesQuantity() : 0L;
						rAccepted += acc;
						rDamaged += dmg;
						rSales += sal;

						if (jwri.getWagePerItem() != null) {
							rWages += jwri.getWagePerItem() * acc;
						}
						if (jwri.getSalesPrice() != null) {
							rSalesAmt += jwri.getSalesPrice() * sal;
						}

						// Build damage details
						List<BatchTimelineResponse.DamageDetail> damageDetails = new ArrayList<>();
						double itemDeduction = 0.0;
						if (jwri.getDamages() != null) {
							for (Damage damage : jwri.getDamages()) {
								if (jw.getJobworkType() != JobworkType.CUTTING) {
									if (damage.getDamageType() == DamageType.REPAIRABLE) postCuttingRepairable += damage.getQuantity();
									else if (damage.getDamageType() == DamageType.UNREPAIRABLE) {
										postCuttingUnrepairable += damage.getQuantity();
										jwUnrepairable += damage.getQuantity();
									}
									else if (damage.getDamageType() == DamageType.SUPPLIER_DAMAGE) {
										postCuttingSupplierDamage += damage.getQuantity();
										jwSupplierDamage += damage.getQuantity();
									}
								}

								if (damage.getDamageType() == DamageType.UNREPAIRABLE && jwri.getSalesPrice() != null) {
									itemDeduction += damage.getQuantity() * jwri.getSalesPrice();
								}

								damageDetails.add(BatchTimelineResponse.DamageDetail.builder()
									.quantity(damage.getQuantity())
									.damageType(damage.getDamageType().toString())
									.reworkJobworkNumber(damage.getReworkJobWork() != null ?
										damage.getReworkJobWork().getJobworkNumber() : null)
									.build());
							}
						}
						rWages -= itemDeduction;

						receiptItemDetails.add(BatchTimelineResponse.ReceiptItemDetail.builder()
							.itemName(jwri.getItem() != null ? jwri.getItem().getName() : "Piece(s)")
							.acceptedQuantity(acc)
							.damagedQuantity(dmg)
							.salesQuantity(sal)
							.salesPrice(jwri.getSalesPrice())
							.wagePerItem(jwri.getWagePerItem())
							.damages(damageDetails)
							.build());
					}
				}

				jwAccepted += rAccepted;
				jwDamaged += rDamaged;
				jwSales += rSales;
				jwWages += rWages;
				jwSalesAmt += rSalesAmt;

				receiptSummaries.add(BatchTimelineResponse.ReceiptSummary.builder()
					.receiptId(receipt.getId())
					.receivedAt(receipt.getCreatedAt())
					.receivedBy(receipt.getCreatedBy())
					.receiptItems(receiptItemDetails)
					.totalAccepted(rAccepted)
					.totalDamaged(rDamaged)
					.totalSales(rSales)
					.totalWages(rWages)
					.totalSalesAmount(rSalesAmt)
					.build());
				
				// get the first item receipt time
				
				firstItemReceipt = firstItemReceipt == null ? receipt.getCreatedAt() : null;

				// Receipt timeline event
				BatchTimelineResponse.TimelineEvent receiptEvent = BatchTimelineResponse.TimelineEvent.builder()
					.eventType(TimelineEventType.JOBWORK_RECEIPT)
					.message(String.format("Receipt from %s (Jobwork #%s): Accepted %d, Damaged %d, Sales %d",
						employeeName, jw.getJobworkNumber(), rAccepted, rDamaged, rSales))
					.performedAt(receipt.getCreatedAt())
					.performedBy(receipt.getCreatedBy())
					.stage("SUBMITTED")
					.jobworkNumber(jw.getJobworkNumber())
					.jobworkType(jw.getJobworkType() != null ? jw.getJobworkType().toString() : null)
					.employeeName(employeeName)
					.totalQuantity(rAccepted + rDamaged + rSales)
					.acceptedQuantity(rAccepted)
					.damagedQuantity(rDamaged)
					.salesQuantity(rSales)
					.timeTakenFromPrevious(TimeDifferenceUtil.formatDuration(
						timelineEvents.get(timelineEvents.size() - 1).getPerformedAt(), receipt.getCreatedAt()))
					.items(receiptItemDetails.stream().map(ri -> {
						TimelineItemDetail tid = new TimelineItemDetail();
						tid.setItemName(ri.getItemName());
						tid.setAcceptedQuantity(ri.getAcceptedQuantity());
						tid.setDamagedQuantity(ri.getDamagedQuantity());
						tid.setSalesQuantity(ri.getSalesQuantity());
						return tid;
					}).collect(Collectors.toList()))
					.build();
				timelineEvents.add(receiptEvent);
			}

			if (jw.getJobworkType() != JobworkType.CUTTING) {
				postCuttingAccepted += jwAccepted;
				postCuttingDamaged += jwDamaged;
				postCuttingSales += jwSales;
				if (jw.getJobworkType() == JobworkType.EMBROIDERY) {
					// Embroidery completed = accepted + unrepairable + supplier damage (all processed items)
					embroideryCompleted += (jwAccepted + jwUnrepairable + jwSupplierDamage);
				} else if (jw.getJobworkType() == JobworkType.STITCHING) {
					// Stitching completed = accepted + unrepairable + supplier damage (all processed items)
					stitchingCompleted += (jwAccepted + jwUnrepairable + jwSupplierDamage);
				} else if (jw.getJobworkType() == JobworkType.PACKAGING) {
					// Packaging completed = accepted + unrepairable + supplier damage (all processed items)
					packagingCompleted += (jwAccepted + jwUnrepairable + jwSupplierDamage);
				}
			} else {
				// Cutting consumed = accepted from cutting (which becomes post-cutting quantity)
				cuttingCompleted += jwAccepted;
			}
			totalWages += jwWages;
			totalSalesAmount += jwSalesAmt;

			jobworkSummaries.add(BatchTimelineResponse.JobworkSummary.builder()
				.jobworkId(jw.getId())
				.jobworkNumber(jw.getJobworkNumber())
				.jobworkType(jw.getJobworkType() != null ? jw.getJobworkType().toString() : null)
				.jobworkOrigin(jw.getJobworkOrigin() != null ? jw.getJobworkOrigin().toString() : null)
				.jobworkStatus(jw.getJobworkStatus() != null ? jw.getJobworkStatus().toString() : null)
				.assignedTo(employeeName)
				.remarks(jw.getRemarks())
				.assignedAt(jw.getCreatedAt())
				.createdBy(jw.getCreatedBy())
				.parentJobworkNumber(jw.getParentJobwork() != null ? jw.getParentJobwork().getJobworkNumber() : null)
				.assignedItems(assignedItems)
				.receipts(receiptSummaries)
				.totalAssignedQuantity(jwQty)
				.totalAcceptedQuantity(jwAccepted)
				.totalDamagedQuantity(jwDamaged)
				.totalSalesQuantity(jwSales)
				.build());
		}

		// ─── Quantity Flow ────────────────────────────────────
		BatchTimelineResponse.QuantityFlow quantityFlow = BatchTimelineResponse.QuantityFlow.builder()
			.preCutting(BatchTimelineResponse.PreCuttingFlow.builder()
				.totalQuantity(totalPreCuttingQuantity)
				.assignedQuantity(preCuttingAssigned)
				.consumedQuantity(preCuttingConsumed)
				.build())
			.postCutting(BatchTimelineResponse.PostCuttingFlow.builder()
				.totalQuantity(totalPostCuttingQuantity)
				.assignedQuantity(postCuttingAssigned)
				.acceptedQuantity(postCuttingAccepted)
				.damagedQuantity(postCuttingDamaged)
				.salesQuantity(postCuttingSales)
				.repairableDamage(postCuttingRepairable)
				.unrepairableDamage(postCuttingUnrepairable)
				.supplierDamage(postCuttingSupplierDamage)
				.build())
			.currentAvailableQuantity(batch.getAvailableQuantity())
			.build();

		// ─── Sort timeline chronologically ────────────────────
		timelineEvents.sort(Comparator.comparing(BatchTimelineResponse.TimelineEvent::getPerformedAt,
			Comparator.nullsLast(Comparator.naturalOrder())));

		// ─── Stats ────────────────────────────────────────────
		LocalDateTime firstEvent = !timelineEvents.isEmpty() ? timelineEvents.get(0).getPerformedAt() : null;
		LocalDateTime lastEvent = !timelineEvents.isEmpty() ? timelineEvents.get(timelineEvents.size() - 1).getPerformedAt() : null;

		BatchTimelineResponse.TimelineStats stats = BatchTimelineResponse.TimelineStats.builder()
			.totalEvents(timelineEvents.size())
			.totalJobworks(jobworks.size())
			.totalReceipts(totalReceiptCount)
			.totalDurationFromCreation(TimeDifferenceUtil.formatDuration(firstEvent, LocalDateTime.now()))
			.firstEventAt(firstEvent)
			.lastEventAt(lastEvent)
			.totalDurationFromItemCreation(TimeDifferenceUtil.formatDuration(firstItemReceipt, LocalDateTime.now()))
			.cuttingJobworkCount((int) jobworks.stream().filter(jw -> jw.getJobworkType() == JobworkType.CUTTING).count())
//			.embroideryJobworkCount((int) jobworks.stream().filter(jw -> jw.getJobworkType() == JobworkType.EMBROIDERY).count())
			.stitchingJobworkCount((int) jobworks.stream().filter(jw -> jw.getJobworkType() == JobworkType.STITCHING).count())
			.packagingJobworkCount((int) jobworks.stream().filter(jw -> jw.getJobworkType() == JobworkType.PACKAGING).count())
			.uniqueEmployeesAssigned((int) jobworks.stream().map(jw -> jw.getAssignedTo()).filter(e -> e != null).map(e -> e.getName()).distinct().count())
			.averageTimeBetweenJobworks(calculateAvgTimeBetweenJobworks(jobworks))
			.averageTimeBetweenReceipts(calculateAvgTimeBetweenReceipts(allReceipts))
			.totalWagesPaid(totalWages)
			.totalSalesRevenue(totalSalesAmount)
			.totalCostOfProduction(totalWages - totalSalesAmount)
			.totalItemsProduced(totalPostCuttingQuantity)
			.totalItemsAccepted(postCuttingAccepted)
			.totalItemsDamaged(postCuttingDamaged)
			.totalItemsSold(postCuttingSales)
			.overallAcceptanceRate(calculateRate(postCuttingAccepted, postCuttingAccepted + postCuttingDamaged + postCuttingSales))
			.overallDamageRate(calculateRate(postCuttingDamaged, postCuttingAccepted + postCuttingDamaged + postCuttingSales))
			.overallSalesRate(calculateRate(postCuttingSales, postCuttingAccepted + postCuttingDamaged + postCuttingSales))
			.productionEfficiencyScore(calculateProductionEfficiencyScore(postCuttingAccepted, postCuttingDamaged, postCuttingSales))
			.totalReworkCount(calculateReworkCount(allReceipts))
			.estimatedCompletionTime("N/A")
			.build();

		// ─── Stage Progress ───────────────────────────────────
		// For cutting: progress = consumed / total pre-cutting quantity
		BatchTimelineResponse.StageProgress cuttingProgress = BatchTimelineResponse.StageProgress.builder()
			.totalQuantity(totalPreCuttingQuantity)
			.completedQuantity(preCuttingConsumed)
			.firstStartedAt(cuttingStartedAt)
			.progressPercentage(totalPreCuttingQuantity > 0 ? (Math.round((double) preCuttingConsumed / totalPreCuttingQuantity * 100)) + "%" : "0%")
			.build();

		// For stitching & packaging: progress = (accepted + unrepairable + supplier damage) / total post-cutting quantity
		// These represent all items that have been processed (successfully or terminated)
		BatchTimelineResponse.StageProgress embroideryProgress = BatchTimelineResponse.StageProgress.builder()
				.totalQuantity(totalPostCuttingQuantity)
				.completedQuantity(embroideryCompleted)
				.firstStartedAt(embroideryStartedAt)
				.progressPercentage(totalPostCuttingQuantity > 0 ? (Math.round((double) embroideryCompleted / totalPostCuttingQuantity * 100)) + "%" : "0%")
				.build();

		BatchTimelineResponse.StageProgress stitchingProgress = BatchTimelineResponse.StageProgress.builder()
			.totalQuantity(totalPostCuttingQuantity)
			.completedQuantity(stitchingCompleted)
			.firstStartedAt(stitchingStartedAt)
			.progressPercentage(totalPostCuttingQuantity > 0 ? (Math.round((double) stitchingCompleted / totalPostCuttingQuantity * 100)) + "%" : "0%")
			.build();

		BatchTimelineResponse.StageProgress packagingProgress = BatchTimelineResponse.StageProgress.builder()
			.totalQuantity(totalPostCuttingQuantity)
			.completedQuantity(packagingCompleted)
			.firstStartedAt(packagingStartedAt)
			.progressPercentage(totalPostCuttingQuantity > 0 ? (Math.round((double) packagingCompleted / totalPostCuttingQuantity * 100)) + "%" : "0%")
			.build();

		// ─── Build Final Response ─────────────────────────────
		return BatchTimelineResponse.builder()
			.batchId(batch.getId())
			.serialCode(batch.getSerialCode())
			.categoryName(batch.getCategory() != null ? batch.getCategory().getName() : null)
			.batchStatus(batch.getBatchStatus() != null ? batch.getBatchStatus().getValue() : null)
			.isUrgent(batch.getIsUrgent())
			.remarks(batch.getRemarks())
			.createdBy(batch.getCreatedBy())
			.createdAt(batch.getCreatedAt())
			.lastModifiedBy(batch.getLastModifiedBy())
			.lastModifiedAt(batch.getLastModifiedAt())
			.items(itemSummaries)
			.subCategories(subCatSummaries)
			.cuttingProgress(cuttingProgress)
			.embroideryProgress(embroideryProgress)
			.stitchingProgress(stitchingProgress)
			.packagingProgress(packagingProgress)
			.quantityFlow(quantityFlow)
			.jobworks(jobworkSummaries)
			.timeline(timelineEvents)
			.stats(stats)
			.build();
	}




	private List<BatchSubCategory> validateBatchSubCategories(List<BatchSubCategoryRequestDTO> batchSubCategories) {
		List<BatchSubCategory> validatedBatchSubCategories = new ArrayList<>();
		for (BatchSubCategoryRequestDTO batchSubCategoryRequestDTO : batchSubCategories) {
			SubCategory subCategory = subCategoryRepository.findByName(batchSubCategoryRequestDTO.getSubCategoryName())
					.orElseThrow(() -> {
						LOGGER.error("Sub category not found with id {}",
								batchSubCategoryRequestDTO.getSubCategoryName());
						return new SubCategoryNotFoundException(
								"Sub category not found with name " + batchSubCategoryRequestDTO.getSubCategoryName());
					});
			BatchSubCategory batchSubCategory = new BatchSubCategory();
			batchSubCategory.setSubCategory(subCategory);
			batchSubCategory.setQuantity(batchSubCategoryRequestDTO.getQuantity());
			validatedBatchSubCategories.add(batchSubCategory);
		}
		return validatedBatchSubCategories;
	}

	// map the subcategories to the batch response dto
	private BatchResponseDTO convertToBatchResponseDTO(Batch batch) {
		BatchResponseDTO batchResponseDTO = modelMapper.map(batch, BatchResponseDTO.class);
		List<BatchSubCategory> batchSubCategories = batchSubCategoryRepository.findByBatchId(batch.getId());
		List<BatchSubCategoryResponseDTO> batchSubCategoryResponseDTOs = batchSubCategories.stream()
				.map(batchSubCategory -> modelMapper.map(batchSubCategory, BatchSubCategoryResponseDTO.class))
				.collect(Collectors.toList());
		batchResponseDTO.setSubCategories(batchSubCategoryResponseDTOs);

		List<BatchItem> batchItems = batchItemRepository.findByBatchId(batch.getId());
		List<BatchResponseDTO.BatchItemResponse> itemResponseDTOs = batchItems.stream().map(batchItem -> {
			BatchResponseDTO.BatchItemResponse itemResponse = new BatchResponseDTO.BatchItemResponse();
			itemResponse.setId(batchItem.getId());
			itemResponse.setItemName(batchItem.getItem() != null ? batchItem.getItem().getName() : null);
			itemResponse.setQuantity(batchItem.getQuantity());
			return itemResponse;
		}).collect(Collectors.toList());
		batchResponseDTO.setItems(itemResponseDTOs);

		return batchResponseDTO;
	}

	@Override
	@Transactional
	public void updateBatch(Long batchId, BatchUpdateDTO batchUpdateDTO) {
		Batch batch = batchRepository.findById(batchId).orElseThrow(() -> {
			LOGGER.error("Batch not found with id {}", batchId);
			return new BatchNotFoundException("Batch not found with id " + batchId);
		});

		if (batchUpdateDTO.getSerialCode() != null) {
			batch.setSerialCode(batchUpdateDTO.getSerialCode());
		}
		if (batchUpdateDTO.getCategoryName() != null) {
			Category category = categoryRepository.findByName(batchUpdateDTO.getCategoryName()).orElseThrow(() -> {
				LOGGER.error("Category not found with name {}", batchUpdateDTO.getCategoryName());
				return new CategoryNotFoundException(
						"Category not found with name " + batchUpdateDTO.getCategoryName());
			});
			batch.setCategory(category);
		}
		if (batchUpdateDTO.getIsUrgent() != null) {
			batch.setIsUrgent(batchUpdateDTO.getIsUrgent());
		}
		if (batchUpdateDTO.getRemarks() != null) {
			batch.setRemarks(batchUpdateDTO.getRemarks());
		}
		if (batchUpdateDTO.getSubCategories() != null) {
			List<BatchSubCategory> batchSubCategories = validateBatchSubCategories(batchUpdateDTO.getSubCategories());
			for (BatchSubCategory batchSubCategory : batchSubCategories) {
				batchSubCategory.setBatch(batch);
				batchSubCategoryRepository.save(batchSubCategory);
			}
		}
		if (batchUpdateDTO.getBatchStatusName() != null) {
			try {
				BatchStatus batchStatus = BatchStatus.valueOf(batchUpdateDTO.getBatchStatusName().toUpperCase());

				if (batchStatus == BatchStatus.DISCARDED) {
					batch.setBatchStatus(batchStatus);
				}

			} catch (IllegalArgumentException ex) {
				LOGGER.error("Invalid batch status {}", batchUpdateDTO.getBatchStatusName());
				throw new BatchStatusNotFoundException(
						"Batch status not found with name " + batchUpdateDTO.getBatchStatusName());
			}
		}

		batchRepository.save(batch);
	}

	// TODO
	@Override
	public List<JobworkType> getAllowedJobworkTypes(String batchSerialCode) {
		List<JobworkType> allowedJobworkTypes = new ArrayList<>();
		Batch batch = this.getBatchOrThrow(batchSerialCode);

		// logic to include CUTTING
		LOGGER.debug("Fetching available quantities for cutting for batch {}", batchSerialCode);
		Long availableQuantity = this.getAvailableQuantitiesForCutting(batchSerialCode);
		if (availableQuantity > 0) {
			LOGGER.debug("Cutting allowed for batch {}", batchSerialCode);
			allowedJobworkTypes.add(JobworkType.CUTTING);
		}

		// conditions for adding embroidery (optional step after cutting)
		List<JobworkReceipt> cuttingJobworkReceipts = receiptRepository
				.findByJobworkBatchSerialCodeAndJobworkJobworkType(batchSerialCode, JobworkType.CUTTING);
		LOGGER.debug("Fetched {} jobwork receipts for CUTTING of batch {}", cuttingJobworkReceipts.size(),
				batchSerialCode);

		Long totalAcceptedQuantityFromCutting = !cuttingJobworkReceipts.isEmpty() ? cuttingJobworkReceipts.stream()
				.flatMap(receipt -> receipt.getJobworkReceiptItems().stream())
				.map(JobworkReceiptItem::getAcceptedQuantity).filter(Objects::nonNull).mapToLong(Long::longValue).sum()
				: 0L;
		LOGGER.debug("Accepted quantities received for batch {} from CUTTING jobs : {}", batchSerialCode,
				totalAcceptedQuantityFromCutting);

		// EMBROIDERY is optional - can be allowed if cutting output is available
		Long assignedEmbroideryQuantities = jobworkRepository.getAssignedQuantities(batchSerialCode,
				JobworkType.EMBROIDERY.name());
		Long damagedRepairableEmbroideryQuantities = damageRepository.getDamagedQuantity(batchSerialCode,
				DamageType.REPAIRABLE.name(), JobworkType.EMBROIDERY.name());
		long availableForEmbroidery = totalAcceptedQuantityFromCutting - assignedEmbroideryQuantities
				+ damagedRepairableEmbroideryQuantities;

		if (availableForEmbroidery > 0) {
			allowedJobworkTypes.add(JobworkType.EMBROIDERY);
		}

		// conditions for adding stitching
		// Stitching can happen after:
		// 1. Cutting (if embroidery is skipped)
		// 2. Embroidery (if embroidery was done)
		// Calculate available quantity for stitching from both sources
		
		// From embroidery receipts (if embroidery was done)
		List<JobworkReceipt> embroideryJobworkReceipts = receiptRepository
				.findByJobworkBatchSerialCodeAndJobworkJobworkType(batchSerialCode, JobworkType.EMBROIDERY);
		LOGGER.debug("Fetched {} jobwork receipts for EMBROIDERY of batch {}", embroideryJobworkReceipts.size(),
				batchSerialCode);

		Long totalAcceptedQuantityFromEmbroidery = !embroideryJobworkReceipts.isEmpty() ? embroideryJobworkReceipts.stream()
				.flatMap(receipt -> receipt.getJobworkReceiptItems().stream())
				.map(JobworkReceiptItem::getAcceptedQuantity).filter(Objects::nonNull).mapToLong(Long::longValue).sum()
				: 0L;
		LOGGER.debug("Accepted quantities received for batch {} from EMBROIDERY jobs : {}", batchSerialCode,
				totalAcceptedQuantityFromEmbroidery);

		// If embroidery was done, use embroidery output; otherwise use cutting output
		Long inputQuantityForStitching = totalAcceptedQuantityFromEmbroidery > 0 
				? totalAcceptedQuantityFromEmbroidery 
				: totalAcceptedQuantityFromCutting;

		Long assignedStitchingQuantities = jobworkRepository.getAssignedQuantities(batchSerialCode,
				JobworkType.STITCHING.name());
		Long damagedRepairableStitchingQuantities = damageRepository.getDamagedQuantity(batchSerialCode,
				DamageType.REPAIRABLE.name(), JobworkType.STITCHING.name());
		long availableForStitching = inputQuantityForStitching - assignedStitchingQuantities
				+ damagedRepairableStitchingQuantities;

		if (availableForStitching > 0) {
			allowedJobworkTypes.add(JobworkType.STITCHING);
		}

		// conditions for adding stitching
		List<JobworkReceipt> stitchingJobworkReceipts = receiptRepository
				.findByJobworkBatchSerialCodeAndJobworkJobworkType(batchSerialCode, JobworkType.STITCHING);
		LOGGER.debug("Fetched {} jobwork receipts for STITCHING of batch {}", stitchingJobworkReceipts.size(),
				batchSerialCode);

		Long totalAcceptedQuantityForStitching = !stitchingJobworkReceipts.isEmpty() ? stitchingJobworkReceipts.stream()
				.flatMap(receipt -> receipt.getJobworkReceiptItems().stream())
				.map(JobworkReceiptItem::getAcceptedQuantity).filter(Objects::nonNull).mapToLong(Long::longValue).sum()
				: 0L;
		LOGGER.debug("Accepted quantities received for batch {} from STITCHING jobs : {}", batchSerialCode,
				totalAcceptedQuantityForStitching);

		if (totalAcceptedQuantityForStitching > 0) {
			allowedJobworkTypes.add(JobworkType.PACKAGING);
		}

		return allowedJobworkTypes;
	}

	// mark the batch as discarded in batch status and refill inventory
	@Override
	public void recycleBatch(Long batchId) {
		
		Batch batch = batchRepository.findById(batchId).orElseThrow(() -> {
			LOGGER.error("Batch not found with id {}", batchId);
			return new BatchNotFoundException("Batch not found with id " + batchId);
		});

		if (batch.getBatchStatus() == BatchStatus.DISCARDED) {
			LOGGER.info("Batch already recyled");
			return;
		}

		List<BatchSubCategory> batchSubCategories = batchSubCategoryRepository.findByBatchId(batchId);

		long categoryId = batch.getCategory().getId();

		for (BatchSubCategory batchSubCategory : batchSubCategories) {
//			batchSubCategory.setAvailableQuantity(0L);
//			boolean isInventoryValid = inventoryRepository.existsByCategoryIdAndSubCategoryId(categoryId,
//					batchSubCategory.getSubCategory().getId());
//			if (isInventoryValid) {
//				Inventory inventory = inventoryRepository
//						.findByCategoryIdAndSubCategoryId(categoryId, batchSubCategory.getSubCategory().getId())
//						.orElseThrow(() -> {
//							LOGGER.error("Inventory not found with category ID {}", categoryId);
//							return new InventoryNotFoundException("Inventory not found with category id " + categoryId);
//						});
//				long countAfterRecycle = inventory.getCount() + batchSubCategory.getQuantity();
//				inventory.setCount(countAfterRecycle);
//				validInventories.add(inventory);
//			}
//
			MaterialInventoryLedger inventory;
			inventory = new MaterialInventoryLedger();
			inventory.setDirection(LedgerDirection.IN);
			inventory.setMovementType(MovementType.BATCH_RECYLE);
			inventory.setReferenceType(ReferenceType.BATCH);
			inventory.setReference_id(batch.getId());
			inventory.setUnit("piece(s)");
			inventory.setQuantity(batchSubCategory.getQuantity());
			inventory.setSubCategory(batchSubCategory.getSubCategory());
			inventory.setCategory(batch.getCategory());

			ledgerRepository.save(inventory);
		}
		batch.setBatchStatus(BatchStatus.DISCARDED);
		batch.setAvailableQuantity(0L);
		batchRepository.save(batch);
	}

	@Override
	public List<BatchDetailDTO> getBatchDetails(Long batchId) {

		Batch batch = batchRepository.findById(batchId).orElseThrow(() -> {
			LOGGER.error("Batch not found with id {}", batchId);
			return new BatchNotFoundException("Batch not found with id " + batchId);
		});

		BatchDetailDTO batchDetailDTO = new BatchDetailDTO();
		batchDetailDTO.setBatchSerialCode(batch.getSerialCode());

		// FETCH all batch damages

		return null;
	}

	@Override
	public Long getAvailableQuantities(String serialCode, String jobworkType) {

		Batch batch = batchRepository.findBySerialCode(serialCode).orElseThrow(() -> {
			LOGGER.error("Batch not found with serial {}", serialCode);
			return new BatchNotFoundException("Batch not found with serial " + serialCode);
		});

		long totalQuantities = batchRepository.findQuantityBySerialCode(serialCode);

		// fetch jobwork receipts for the batch logic for CUTTING
		List<JobworkReceipt> receipts = receiptRepository.findByJobworkBatchSerialCodeAndJobworkJobworkType(serialCode,
				JobworkType.valueOf(jobworkType));
		List<Jobwork> jobworks = jobworkRepository.findByBatchSerialCodeAndJobworkStatusIn(serialCode,
				Arrays.asList(JobworkStatus.IN_PROGRESS, JobworkStatus.REASSIGNED));

		// subtract quantities from ongoing jobworks yet to be submitted
		for (Jobwork jobwork : jobworks) {
			List<JobworkItem> jobworkItems = jobwork.getJobworkItems();
			for (JobworkItem jobworkItem : jobworkItems) {
				totalQuantities -= jobworkItem.getQuantity();
			}

		}

		// subtract quantities from submitted jobworks
		for (JobworkReceipt jobworkReceipt : receipts) {
			List<JobworkReceiptItem> receiptItems = jobworkReceipt.getJobworkReceiptItems();
			for (JobworkReceiptItem receiptItem : receiptItems) {
				totalQuantities -= (receiptItem.getAcceptedQuantity() + receiptItem.getSalesQuantity());

			}

			long totalDamages = jobworkReceipt.getJobworkReceiptItems().stream()
					.flatMap(item -> item.getDamages().stream()) // flatten all damage lists
					.filter(damage -> damage.getDamageType() != DamageType.REPAIRABLE).mapToLong(Damage::getQuantity)
					.sum();

			totalQuantities -= totalDamages;

		}

		return totalQuantities;
	}

	@Override
	public Long getAvailableQuantitiesForCutting(String serialCode) {
		LOGGER.debug("Fetching available quantities for CUTTING for batch {}", serialCode);

		Batch batch = this.getBatchOrThrow(serialCode);

		Long assignedJobworksQuantity = jobworkRepository.getAssignedQuantities(serialCode, JobworkType.CUTTING.name());
		Long repairableDamages = damageRepository.getDamagedQuantity(serialCode, DamageType.REPAIRABLE.name(), JobworkType.CUTTING.name());
		LOGGER.debug("dog {} {}", assignedJobworksQuantity, repairableDamages);
		
		// For cutting, total quantity is sum of sub-categories
		Long batchQuantity = batchRepository.findQuantityBySerialCode(batch.getSerialCode());
		
		Long availableQuantitiesForCutting = (batchQuantity - assignedJobworksQuantity) + repairableDamages;
		
		LOGGER.debug("Available quantities for cutting work for batch {} is {}", serialCode,
				availableQuantitiesForCutting);
		if (availableQuantitiesForCutting < 0) {
			return 0L;
		}

		return availableQuantitiesForCutting;
	}

	@Override
	public Long getAvailableQuantitiesBySubCategory(String serialCode, String subCategoryName) {
		LOGGER.debug("Fetching available quantity for sub-category {} in batch {}", subCategoryName, serialCode);
		BatchSubCategory bsc = batchSubCategoryRepository.findByBatchSerialCodeAndSubCategoryName(serialCode, subCategoryName)
				.orElseThrow(() -> new SubCategoryNotFoundException("Sub-category " + subCategoryName + " not found for batch " + serialCode));
		
		Long assignedQuantity = jobworkRepository.getAssignedQuantitiesBySubCategory(serialCode, JobworkType.CUTTING.name(), subCategoryName);
		Long repairableDamages = damageRepository.getDamagedQuantity(serialCode, DamageType.REPAIRABLE.name(), JobworkType.CUTTING.name());
		
		return (bsc.getQuantity() - assignedQuantity) + repairableDamages;
	}

	@Override
	public List<String> getBatchSerialCodesForJobwork() {
		LOGGER.debug("Fetching batch serial codes that are available for jobworks");
		List<String> batchSerialCodes = batchRepository.findAllBatchSerialCodesForJobwork();
		return batchSerialCodes;
	}

	public void recalculateBatchStatus(Batch batch) {

		List<Jobwork> jobworks = jobworkRepository.findByBatch(batch);

		if (jobworks.isEmpty()) {
			batch.setBatchStatus(BatchStatus.CREATED);
			LOGGER.debug("Marked the status of batch {} as {}", batch.getSerialCode(), BatchStatus.CREATED);
		} else if (jobworks.stream().anyMatch(jw -> jw.getJobworkStatus() == JobworkStatus.IN_PROGRESS
				|| jw.getJobworkStatus() == JobworkStatus.AWAITING_CLOSE)) {
			batch.setBatchStatus(BatchStatus.ASSIGNED);
			LOGGER.debug("Marked the status of batch {} as {}", batch.getSerialCode(), BatchStatus.ASSIGNED);
		} else if (jobworks.stream().allMatch(jw -> jw.getJobworkStatus() == JobworkStatus.CLOSED
				|| jw.getJobworkStatus() == JobworkStatus.REASSIGNED)) {
			batch.setBatchStatus(BatchStatus.COMPLETED);
			LOGGER.debug("Marked the status of batch {} as {}", batch.getSerialCode(), BatchStatus.COMPLETED);
		}

		batchRepository.save(batch);
	}



	private Batch getBatchOrThrow(String serialCode) {
		return batchRepository.findBySerialCode(serialCode).orElseThrow(() -> {
			LOGGER.error("Batch not found: {}", serialCode);
			return new BatchNotFoundException("Batch not found: " + serialCode);
		});
	}

	@Override
	public List<String> getAllBatchSerialCode() {
		LOGGER.debug("Fetchging all the batch serial codes");
		return batchRepository.getAllBatchSerialCodes();
	}

	// get the subcategories for the given serial code
	@Override
	public List<String> getSubCategoriesBySerialCode(String serialCode) {
		LOGGER.debug("Fetching sub-categories for batch serial code: {}", serialCode);
		return batchSubCategoryRepository.findSubCategoriesBySerialCode(serialCode);
	}

	// ══════════════════════════════════════════════════════════
	//  HELPER METHODS FOR BATCH TIMELINE METRICS
	// ══════════════════════════════════════════════════════════

	/**
	 * Calculate average time between jobwork assignments.
	 */
	private String calculateAvgTimeBetweenJobworks(List<Jobwork> jobworks) {
		if (jobworks == null || jobworks.size() <= 1) return "N/A";
		
		long totalMillis = 0;
		List<Jobwork> sorted = jobworks.stream()
			.sorted(Comparator.comparing(Jobwork::getCreatedAt))
			.collect(Collectors.toList());
		for (int i = 1; i < sorted.size(); i++) {
			totalMillis += java.time.Duration.between(
				sorted.get(i-1).getCreatedAt(), 
				sorted.get(i).getCreatedAt()).toMillis();
		}
		return formatDuration(totalMillis / (jobworks.size() - 1));
	}

	/**
	 * Calculate average time between receipts.
	 */
	private String calculateAvgTimeBetweenReceipts(List<JobworkReceipt> receipts) {
		if (receipts == null || receipts.size() <= 1) return "N/A";
		
		long totalMillis = 0;
		List<JobworkReceipt> sorted = receipts.stream()
			.sorted(Comparator.comparing(JobworkReceipt::getCreatedAt))
			.collect(Collectors.toList());
		for (int i = 1; i < sorted.size(); i++) {
			totalMillis += java.time.Duration.between(
				sorted.get(i-1).getCreatedAt(), 
				sorted.get(i).getCreatedAt()).toMillis();
		}
		return formatDuration(totalMillis / (receipts.size() - 1));
	}

	/**
	 * Calculate rate as percentage.
	 */
	private Double calculateRate(long numerator, long denominator) {
		if (denominator <= 0) return 0.0;
		return Math.round((double) numerator / denominator * 10000.0) / 100.0;
	}

	/**
	 * Calculate production efficiency score (A-D rating).
	 */
	private String calculateProductionEfficiencyScore(long accepted, long damaged, long sales) {
		long total = accepted + damaged + sales;
		if (total <= 0) return "N/A";
		
		double acceptanceRate = (double) accepted / total * 100;
		double damageRate = (double) damaged / total * 100;
		
		// Weighted score: acceptance (60%), damage penalty (40%)
		double score = (acceptanceRate * 0.6) - (damageRate * 0.4);
		
		if (score >= 70) return "A";
		if (score >= 50) return "B";
		if (score >= 30) return "C";
		return "D";
	}

	/**
	 * Count items sent for rework.
	 */
	private Long calculateReworkCount(List<JobworkReceipt> receipts) {
		if (receipts == null) return 0L;
		return receipts.stream()
			.filter(r -> r.getJobworkReceiptItems() != null)
			.flatMap(r -> r.getJobworkReceiptItems().stream())
			.filter(ri -> ri.getDamages() != null)
			.flatMap(ri -> ri.getDamages().stream())
			.filter(d -> d.getReworkJobWork() != null)
			.mapToLong(d -> d.getQuantity() != null ? d.getQuantity() : 0L)
			.sum();
	}

	/**
	 * Format duration in milliseconds to human-readable format.
	 */
	private String formatDuration(long millis) {
		long days = java.util.concurrent.TimeUnit.MILLISECONDS.toDays(millis);
		long hours = java.util.concurrent.TimeUnit.MILLISECONDS.toHours(millis) % 24;
		long minutes = java.util.concurrent.TimeUnit.MILLISECONDS.toMinutes(millis) % 60;
		
		if (days > 0) {
			return String.format("%dd %dh %dm", days, hours, minutes);
		} else if (hours > 0) {
			return String.format("%dh %dm", hours, minutes);
		} else {
			return String.format("%dm", minutes);
		}
	}

}
