package com.lakshmigarments.service.impl;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.lakshmigarments.dto.JobworkDetailDTO;
import com.lakshmigarments.model.Jobwork;
import com.lakshmigarments.exception.JobworkNotFoundException;
import com.lakshmigarments.repository.JobworkRepository;
import com.lakshmigarments.service.JobworkService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JobworkServiceImpl implements JobworkService {
    private static final Logger LOGGER = LoggerFactory.getLogger(JobworkServiceImpl.class);
    private final JobworkRepository jobworkRepository;

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

}
