package com.lakshmigarments.init;

import com.lakshmigarments.model.Role;
import com.lakshmigarments.model.User;
import com.lakshmigarments.repository.RoleRepository;
import com.lakshmigarments.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptException;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataInitializer.class);
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
    	
    	Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM roles", Long.class);
        if (count != null && count == 0) {
            LOGGER.info("Inserting master data...");
            var resource = new ClassPathResource("data/db_script.sql");
            try {
				ScriptUtils.executeSqlScript(jdbcTemplate.getDataSource().getConnection(), resource);
			} catch (ScriptException e) {
				LOGGER.error("Initial Script Error");
				e.printStackTrace();
			} catch (SQLException e) {
				LOGGER.error("Invalid SQL lines in the script");
				e.printStackTrace();
			}
            LOGGER.info("Master data inserted successfully.");
        }
    	
        // assuming role is already inserted, otherwise fatal
    	Role adminRole = roleRepository.findByName("Super admin")
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setName("Super admin");
                    return roleRepository.save(newRole);
                });

        if (userRepository.count() == 0) {
            User admin = new User();
            admin.setFirstName("System");
            admin.setLastName("Administrator");
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin"));
            admin.setRole(adminRole);
            admin.setIsActive(true);

            userRepository.save(admin);
            LOGGER.info("Initial Admin User created: admin / admin");
        }
    }
}