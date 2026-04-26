package edu.gatech.cs6310.powergrid.auth;

import java.util.EnumSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1000)
public class AdminSeeder {

    private static final Logger log = LoggerFactory.getLogger(AdminSeeder.class);

    private final UserService users;
    private final String adminUsername;
    private final String adminPassword;

    public AdminSeeder(
        UserService users,
        @Value("${powergrid.auth.admin-username:admin}") String adminUsername,
        @Value("${powergrid.auth.admin-password:admin123}") String adminPassword
    ) {
        this.users = users;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void seed() {
        if (users.findUser(adminUsername).isPresent()) {
            log.info("Admin user '{}' already present; skipping seed.", adminUsername);
            return;
        }
        users.seedIfAbsent(adminUsername, adminPassword, EnumSet.of(Role.ADMIN));
        log.warn("Seeded default admin '{}'. CHANGE THE PASSWORD before exposing this service.", adminUsername);
    }
}
