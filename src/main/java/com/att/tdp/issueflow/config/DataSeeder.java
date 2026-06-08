package com.att.tdp.issueflow.config;

import com.att.tdp.issueflow.entity.Project;
import com.att.tdp.issueflow.entity.User;
import com.att.tdp.issueflow.entity.enums.Role;
import com.att.tdp.issueflow.repository.ProjectRepository;
import com.att.tdp.issueflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        log.info("Checking database for seed users...");

        // 1. Seed admin user
        User admin = userRepository.findByUsername("admin").orElseGet(() -> {
            log.info("Seeding admin user...");
            User user = new User();
            user.setUsername("admin");
            user.setEmail("admin@issueflow.com");
            user.setFullName("System Admin");
            user.setPassword(passwordEncoder.encode("secret"));
            user.setRole(Role.ADMIN);
            return userRepository.save(user);
        });

        // 2. Seed developer user
        User jdoe = userRepository.findByUsername("jdoe").orElseGet(() -> {
            log.info("Seeding developer user (jdoe)...");
            User user = new User();
            user.setUsername("jdoe");
            user.setEmail("jdoe@issueflow.com");
            user.setFullName("John Doe");
            user.setPassword(passwordEncoder.encode("secret"));
            user.setRole(Role.DEVELOPER);
            return userRepository.save(user);
        });

        // 3. Seed a default project so GET /tickets/project/1 works in the stress test
        if (projectRepository.count() == 0) {
            log.info("Seeding default project...");
            Project project = new Project();
            project.setName("Default Project");
            project.setDescription("Seed project for local development and testing.");
            project.setOwner(jdoe);
            projectRepository.save(project);
        }

        log.info("Seeding checked successfully.");
    }
}
