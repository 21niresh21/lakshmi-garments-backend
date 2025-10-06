package com.lakshmigarments.service.impl;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.lakshmigarments.dto.JobworkDetailDTO;
import com.lakshmigarments.dto.JobworkRequestDTO;
import com.lakshmigarments.dto.JobworkResponseDTO;
import com.lakshmigarments.model.Batch;
import com.lakshmigarments.model.Employee;
import com.lakshmigarments.model.Item;
import com.lakshmigarments.model.Jobwork;
import com.lakshmigarments.model.JobworkType;
import com.lakshmigarments.exception.BatchNotFoundException;
import com.lakshmigarments.exception.EmployeeNotFoundException;
import com.lakshmigarments.exception.ItemNotFoundException;
import com.lakshmigarments.exception.JobworkNotFoundException;
import com.lakshmigarments.exception.JobworkTypeNotFoundException;
import com.lakshmigarments.repository.BatchRepository;
import com.lakshmigarments.repository.EmployeeRepository;
import com.lakshmigarments.repository.ItemRepository;
import com.lakshmigarments.repository.JobworkRepository;
import com.lakshmigarments.repository.JobworkTypeRepository;
import com.lakshmigarments.service.JobworkService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JobworkServiceImpl implements JobworkService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobworkServiceImpl.class);
    private final JobworkRepository jobworkRepository;
    private final EmployeeRepository employeeRepository;
    private final ItemRepository itemRepository;
    private final JobworkTypeRepository jobworkTypeRepository;
    private final ModelMapper modelMapper;
    private final BatchRepository batchRepository;

    public Page<JobworkResponseDTO> getAllJobworks(Pageable pageable, String search) {

        Page<Jobwork> jobworks = jobworkRepository.findAll(pageable);

        List<JobworkResponseDTO> jobworkResponseDTOs = this.convertToJobworkResponseDTO(jobworks.getContent());

        LOGGER.info("Fetched {} jobworks", jobworkResponseDTOs.size());
        return new PageImpl<>(jobworkResponseDTOs, pageable, jobworks.getTotalElements());

    }

    @Override
    public void createJobwork(JobworkRequestDTO jobworkRequestDTO) {

        Employee employee = employeeRepository.findById(jobworkRequestDTO.getEmployeeId()).orElseThrow(() -> {
            LOGGER.error("Employee with ID {} not found", jobworkRequestDTO.getEmployeeId());
            return new EmployeeNotFoundException("Employee not found with ID " + jobworkRequestDTO.getEmployeeId());
        });

        Batch batch = batchRepository.findById(jobworkRequestDTO.getBatchId()).orElseThrow(() -> {
            LOGGER.error("Batch with ID {} not found", jobworkRequestDTO.getBatchId());
            return new BatchNotFoundException("Batch not found with ID " + jobworkRequestDTO.getBatchId());
        });

        Item item = null;
        if (jobworkRequestDTO.getItemId() != null) {
            item = itemRepository.findById(jobworkRequestDTO.getItemId()).orElseThrow(() -> {
                LOGGER.error("Item with ID {} not found", jobworkRequestDTO.getItemId());
                return new ItemNotFoundException("Item not found with ID " + jobworkRequestDTO.getItemId());
            });
        }

        JobworkType jobworkType = jobworkTypeRepository.findById(jobworkRequestDTO.getJobworkTypeId())
                .orElseThrow(() -> {
                    LOGGER.error("Jobwork type with ID {} not found", jobworkRequestDTO.getJobworkTypeId());
                    return new JobworkTypeNotFoundException(
                            "Jobwork type not found with ID " + jobworkRequestDTO.getJobworkTypeId());
                });

        Jobwork jobwork = new Jobwork();
        jobwork.setBatch(batch);
        jobwork.setQuantity(jobworkRequestDTO.getQuantity());
        jobwork.setItem(item);
        jobwork.setEmployee(employee);
        jobwork.setJobworkType(jobworkType);
        jobwork.setJobworkNumber(jobworkRequestDTO.getJobworkNumber());
        jobworkRepository.save(jobwork);
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
        List<Jobwork> jobworks = jobworkRepository.findAllByJobworkNumber(jobworkNumber);
        if (jobworks.isEmpty()) {
            LOGGER.error("Jobwork with number {} not found", jobworkNumber);
            throw new JobworkNotFoundException(
                    "Jobwork with number " + jobworkNumber + " not found");
        }
        LOGGER.debug("Found jobwork detail for jobwork number: {}", jobworkNumber);
        return convertToJobworkDetailDTO(jobworks);
    }

    @Override
    public String getNextJobworkNumber() {

        LocalDate today = LocalDate.now();
        String date = today.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        Jobwork jobwork = jobworkRepository.findTop1ByOrderByJobworkNumberDesc().orElse(null);

        String jobworkNumber = "";

        if (jobwork == null) {
            jobworkNumber = "JW-" + date + "001";
        } else if (!jobwork.getJobworkNumber().split("-")[1].equals(date)) {
            jobworkNumber = "JW-" + date + "-" + String.format("%03d", 1);
        } else {
            String lastJobworkNumber = jobwork.getJobworkNumber();
            String lastJobworkNumberWithoutDate = lastJobworkNumber.split("-")[2];
            int lastJobworkNumberInt = Integer.parseInt(lastJobworkNumberWithoutDate);
            System.out.println(date);
            jobworkNumber = "JW-" + date + "-" + String.format("%03d", lastJobworkNumberInt + 1);
        }
        return jobworkNumber;
    }

    private JobworkDetailDTO convertToJobworkDetailDTO(List<Jobwork> jobworks) {

        List<String> items = new ArrayList<>();
        List<Long> quantity = new ArrayList<>();

        for (Jobwork jobwork : jobworks) {
            if (jobwork.getItem() != null) {
                items.add(jobwork.getItem().getName());
            }
            if (jobwork.getQuantity() != null) {
                quantity.add(jobwork.getQuantity());
            }
        }

        JobworkDetailDTO jobworkDetailDTO = new JobworkDetailDTO();
        jobworkDetailDTO.setJobworkNumber(jobworks.get(0).getJobworkNumber());
        jobworkDetailDTO.setStartedAt(jobworks.get(0).getStartedAt());
        jobworkDetailDTO.setJobworkType(jobworks.get(0).getJobworkType().getName());
        jobworkDetailDTO.setBatchSerialCode(jobworks.get(0).getBatch().getSerialCode());
        jobworkDetailDTO.setAssignedTo(jobworks.get(0).getEmployee().getName());
        jobworkDetailDTO.setItems(items);
        jobworkDetailDTO.setQuantity(quantity);
        return jobworkDetailDTO;

    }

    private List<JobworkResponseDTO> convertToJobworkResponseDTO(List<Jobwork> jobworks) {
        // Group Jobworks by jobworkNumber
        Map<String, List<Jobwork>> grouped = jobworks.stream()
                .collect(Collectors.groupingBy(Jobwork::getJobworkNumber));

        List<JobworkResponseDTO> responseList = new ArrayList<>();

        for (Map.Entry<String, List<Jobwork>> entry : grouped.entrySet()) {
            String jobworkNumber = entry.getKey();
            List<Jobwork> jobworkGroup = entry.getValue();

            // Use the first jobwork as representative for shared fields
            Jobwork first = jobworkGroup.get(0);

            JobworkResponseDTO dto = new JobworkResponseDTO();
            dto.setJobworkNumber(jobworkNumber);
            dto.setStartedAt(first.getStartedAt());
            dto.setCompletedAt(first.getEndedAt());
            dto.setStatus(first.getEndedAt() != null ? "Completed" : "In Progress");

            // Safe null checks
            if (first.getJobworkType() != null) {
                dto.setJobworktype(first.getJobworkType().getName());
            }

            if (first.getBatch() != null) {
                dto.setBatchSerial(first.getBatch().getSerialCode());
            }

            if (first.getEmployee() != null) {
                dto.setEmployeeName(first.getEmployee().getName());
            }

            // Collect item names and quantities from the group
            List<String> itemNames = jobworkGroup.stream()
                    .map(Jobwork::getItem)
                    .filter(Objects::nonNull)
                    .map(Item::getName)
                    .collect(Collectors.toList());

            List<Long> quantities = jobworkGroup.stream()
                    .map(Jobwork::getQuantity)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            dto.setItemNames(itemNames);
            dto.setQuantities(quantities);

            responseList.add(dto);
        }

        return responseList;
    }

}
