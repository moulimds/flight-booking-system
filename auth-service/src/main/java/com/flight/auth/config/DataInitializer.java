package com.flight.auth.config;

import com.flight.auth.entity.Role;
import com.flight.auth.entity.User;
import com.flight.auth.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${app.seed.admin.enabled:true}")
    private boolean seedAdminEnabled;

    @Value("${app.seed.admin.email:admin@example.com}")
    private String adminEmail;

    @Value("${app.seed.admin.password:Password123}")
    private String adminPassword;

    @Value("${app.seed.admin.fullName:System Administrator}")
    private String adminFullName;

    @Override
    public void run(String... args) {
        if (seedAdminEnabled) {
            userRepository.findByEmail(adminEmail).ifPresentOrElse(
                admin -> {
                    if (admin.getRole() != Role.ADMIN) {
                        admin.setRole(Role.ADMIN);
                        userRepository.save(admin);
                        logger.info("Restored ADMIN role for: {}", adminEmail);
                    }
                },
                () -> {
                    User admin = new User();
                    admin.setFullName(adminFullName);
                    admin.setEmail(adminEmail);
                    admin.setPassword(passwordEncoder.encode(adminPassword));
                    admin.setRole(Role.ADMIN);
                    admin.setEnabled(true);
                    userRepository.save(admin);
                    logger.info("Default ADMIN user initialized successfully: {}", adminEmail);
                }
            );

          
            if (!userRepository.existsByEmail("customer@example.com")) {
                User cust = new User();
                cust.setFullName("John Customer");
                cust.setEmail("customer@example.com");
                cust.setPassword(passwordEncoder.encode("Password123"));
                cust.setRole(Role.CUSTOMER);
                cust.setEnabled(true);
                userRepository.save(cust);
                logger.info("Default CUSTOMER user initialized successfully: customer@example.com");
            }

         
            if (!userRepository.existsByEmail("delete_user@example.com")) {
                User delUser = new User();
                delUser.setFullName("Delete Test User");
                delUser.setEmail("delete_user@example.com");
                delUser.setPassword(passwordEncoder.encode("Password123"));
                delUser.setRole(Role.CUSTOMER);
                delUser.setEnabled(true);
                userRepository.save(delUser);
                logger.info("Dedicated DELETE user initialized successfully: delete_user@example.com");
            }
        }
    }
}
