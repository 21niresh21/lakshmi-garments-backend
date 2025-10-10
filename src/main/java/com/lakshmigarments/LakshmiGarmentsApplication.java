package com.lakshmigarments;

import java.awt.Desktop;
import java.net.URI;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.lakshmigarments.utility.StreamlitRunner;

@SpringBootApplication
public class LakshmiGarmentsApplication {

	public static void main(String[] args) {
		SpringApplication.run(LakshmiGarmentsApplication.class, args);
		openHomePage();
	}

	public void run(String... args) throws Exception {
        StreamlitRunner.startStreamlit();
    }
	
	private static void openHomePage() {
	    try {
	      URI uri = new URI("http://localhost:8080");
	      if (Desktop.isDesktopSupported()) {
	        Desktop.getDesktop().browse(uri);
	      }
	    } catch (Exception e) {
	      System.err.println("Failed to open browser: " + e.getMessage());
	    }
	  }
	
}
