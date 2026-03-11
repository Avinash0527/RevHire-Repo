package com.example.revhirehiringplatform.config;



import com.example.revhirehiringplatform.model.User;
import com.example.revhirehiringplatform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (!userRepository.existsByEmail("admin@revhire.com")) {
            log.info("Creating default admin user...");
            User admin = new User();
            admin.setName("System Admin");
            admin.setEmail("admin@revhire.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(User.Role.ADMIN);
            admin.setStatus(true);
            userRepository.save(admin);
            log.info("Default admin user created: admin@revhire.com / admin123");
        } else {
            log.info("Admin user already exists.");
        }
    }
}