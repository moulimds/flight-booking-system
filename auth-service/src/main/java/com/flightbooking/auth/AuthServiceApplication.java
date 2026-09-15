package com.flightbooking.auth;

import com.flightbooking.auth.model.Role;
import com.flightbooking.auth.model.User;
import com.flightbooking.auth.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class AuthServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }

    @Bean
    CommandLineRunner initAdminUser(UserRepository repo, PasswordEncoder encoder) {
        return args -> {
            repo.findByEmailIgnoreCase("admin@example.com").ifPresentOrElse(
                admin -> {
                    if (!admin.isActive()) {
                        admin.setActive(true);
                        repo.save(admin);
                        System.out.println(">>> Existing Admin account ensured ACTIVE: admin@example.com");
                    }
                },
                () -> {
                    User admin = new User();
                    admin.setFullName("System Admin");
                    admin.setEmail("admin@example.com");
                    admin.setUsername("admin@example.com");
                    admin.setPasswordHash(encoder.encode("AdminPass123"));
                    admin.setRole(Role.ADMIN);
                    admin.setActive(true);
                    admin.setEmailVerified(true);
                    repo.save(admin);
                    System.out.println(">>> Default Admin user initialized: admin@example.com / AdminPass123");
                }
            );
        };
    }
}
