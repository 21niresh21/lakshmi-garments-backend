package com.lakshmigarments;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.lakshmigarments.utility.StreamlitRunner;

@SpringBootApplication
public class LakshmiGarmentsApplication {

	public static void main(String[] args) {
		SpringApplication.run(LakshmiGarmentsApplication.class, args);
	}

	public void run(String... args) throws Exception {
        StreamlitRunner.startStreamlit();
    }
	
}
