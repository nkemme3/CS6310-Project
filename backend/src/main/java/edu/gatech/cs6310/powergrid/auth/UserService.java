package edu.gatech.cs6310.powergrid.auth;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import edu.gatech.cs6310.powergrid.domain.PowerGridSystem;
import edu.gatech.cs6310.powergrid.error.ErrorCode;
import edu.gatech.cs6310.powergrid.error.SystemError;
import edu.gatech.cs6310.powergrid.robustness.JournalCommand;
import edu.gatech.cs6310.powergrid.robustness.TransactionJournal;

@Service
public class UserService {

    private final PowerGridSystem pgs;
    private final TransactionJournal journal;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final ConcurrentHashMap<String, Session> sessions = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();
    private final Duration sessionTtl;

    public UserService(
        PowerGridSystem pgs,
        TransactionJournal journal,
        @Value("${powergrid.auth.session-ttl-hours:12}") int sessionTtlHours
    ) {
        this.pgs = pgs;
        this.journal = journal;
        this.sessionTtl = Duration.ofHours(Math.max(1, sessionTtlHours));
    }

    public Optional<User> findUser(String username) {
        return Optional.ofNullable(pgs.users().get(username));
    }

    public Collection<User> listUsers() {
        return pgs.users().values();
    }

    public User createUser(String username, String plaintextPassword, Set<Role> roles) {
        if (username == null || username.isBlank()) {
            throw new SystemError(ErrorCode.INVALID_COMMAND, "username is required", "username", null);
        }
        if (plaintextPassword == null || plaintextPassword.length() < 6) {
            throw new SystemError(ErrorCode.INVALID_COMMAND, "password must be at least 6 characters", "password", null);
        }
        Set<Role> effectiveRoles = (roles == null || roles.isEmpty())
            ? EnumSet.of(Role.VIEWER) : EnumSet.copyOf(roles);
        synchronized (pgs.lock()) {
            if (pgs.users().containsKey(username)) {
                throw SystemError.duplicate("User", username);
            }
            String hash = encoder.encode(plaintextPassword);
            JournalCommand.CreateUserCmd cmd = new JournalCommand.CreateUserCmd(
                username, hash, effectiveRoles, Instant.now());
            journal.append(cmd);
            cmd.apply(pgs);
            return pgs.users().get(username);
        }
    }

    public void seedIfAbsent(String username, String plaintextPassword, Set<Role> roles) {
        if (pgs.users().containsKey(username)) return;
        createUser(username, plaintextPassword, roles);
    }

    public Optional<Session> login(String username, String plaintextPassword) {
        User u = pgs.users().get(username);
        if (u == null) return Optional.empty();
        if (!encoder.matches(plaintextPassword, u.getPasswordHash())) return Optional.empty();
        byte[] raw = new byte[32];
        random.nextBytes(raw);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(raw);
        Instant now = Instant.now();
        Session s = new Session(token, u.getUsername(), EnumSet.copyOf(u.getRoles()), now, now.plus(sessionTtl));
        sessions.put(token, s);
        return Optional.of(s);
    }

    public void logout(String token) {
        if (token != null) sessions.remove(token);
    }

    public Optional<Session> validate(String token) {
        if (token == null) return Optional.empty();
        Session s = sessions.get(token);
        if (s == null) return Optional.empty();
        if (s.isExpired(Instant.now())) {
            sessions.remove(token);
            return Optional.empty();
        }
        return Optional.of(s);
    }

    public int activeSessionCount() { return sessions.size(); }
}
