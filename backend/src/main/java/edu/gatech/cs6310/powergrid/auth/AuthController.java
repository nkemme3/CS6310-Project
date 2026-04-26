package edu.gatech.cs6310.powergrid.auth;

import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.gatech.cs6310.powergrid.error.ErrorCode;
import edu.gatech.cs6310.powergrid.error.SystemError;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService users;

    public AuthController(UserService users) {
        this.users = users;
    }

    public record LoginRequest(String username, String password) {}
    public record LoginResponse(String token, String username, Set<Role> roles, Instant expiresAt) {}
    public record MeResponse(String username, Set<Role> roles) {}
    public record CreateUserRequest(String username, String password, Set<Role> roles) {}
    public record UserView(String username, Set<Role> roles, Instant createdAt) {
        public static UserView from(User u) {
            return new UserView(u.getUsername(), EnumSet.copyOf(u.getRoles()), u.getCreatedAt());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest req) {
        if (req == null || req.username() == null || req.password() == null) {
            throw new SystemError(ErrorCode.INVALID_COMMAND, "username and password are required",
                null, "Provide both fields.");
        }
        Session s = users.login(req.username(), req.password())
            .orElseThrow(() -> new SystemError(ErrorCode.UNAUTHORIZED,
                "Invalid username or password.", null, "Double-check credentials and try again."));
        return ResponseEntity.ok(new LoginResponse(s.token(), s.username(), s.roles(), s.expiresAt()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader(value = "Authorization", required = false) String auth) {
        if (auth != null && auth.startsWith("Bearer ")) {
            users.logout(auth.substring("Bearer ".length()).trim());
        }
        SecurityContextHolder.clearContext();
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<MeResponse> me() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new SystemError(ErrorCode.UNAUTHORIZED, "Not authenticated.", null, "Log in first.");
        }
        Object details = authentication.getDetails();
        if (details instanceof Session session) {
            return ResponseEntity.ok(new MeResponse(session.username(), session.roles()));
        }
        throw new SystemError(ErrorCode.UNAUTHORIZED, "Session could not be resolved.", null, "Log in again.");
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserView> listUsers() {
        return users.listUsers().stream().map(UserView::from).toList();
    }

    @PostMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserView> createUser(@RequestBody CreateUserRequest req) {
        User u = users.createUser(req.username(), req.password(), req.roles());
        return ResponseEntity.status(201).body(UserView.from(u));
    }
}
