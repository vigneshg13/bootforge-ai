package com.acme.saas.auth.service;

import com.acme.saas.auth.domain.*;
import com.acme.saas.auth.security.JwtService;
import com.acme.saas.common.exception.BusinessException;
import com.acme.saas.tenant.domain.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class AuthService {

    public record AuthTokens(String accessToken, String refreshToken, UUID defaultOrganizationId) {}

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final OrganizationRepository organizationRepository;
    private final MembershipRepository membershipRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final long refreshExpirationMs;

    public AuthService(UserRepository userRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       OrganizationRepository organizationRepository,
                       MembershipRepository membershipRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       @Value("${app.jwt.refresh-expiration-ms}") long refreshExpirationMs) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.organizationRepository = organizationRepository;
        this.membershipRepository = membershipRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshExpirationMs = refreshExpirationMs;
    }

    @Transactional
    public AuthTokens register(String email, String password, String fullName, String workspaceName) {
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new BusinessException(HttpStatus.CONFLICT, "EMAIL_IN_USE", "Email is already registered");
        }

        User user = new User();
        user.setEmail(email.toLowerCase().trim());
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setFullName(fullName);
        user = userRepository.save(user);

        Organization org = new Organization();
        org.setName(workspaceName);
        org.setSlug(toSlug(workspaceName) + "-" + UUID.randomUUID().toString().substring(0, 6));
        org = organizationRepository.save(org);

        Membership membership = new Membership();
        membership.setOrganizationId(org.getId());
        membership.setUserId(user.getId());
        membership.setRole(Role.OWNER);
        membershipRepository.save(membership);

        return issueTokenPair(user, org.getId());
    }

    @Transactional
    public AuthTokens login(String email, String password) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "Invalid email or password"));

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "Invalid email or password");
        }

        UUID defaultOrgId = membershipRepository.findAllByUserId(user.getId()).stream()
                .findFirst()
                .map(Membership::getOrganizationId)
                .orElse(null);

        return issueTokenPair(user, defaultOrgId);
    }

    @Transactional
    public AuthTokens rotateRefreshToken(String rawRefreshToken) {
        String hash = sha256(rawRefreshToken);
        RefreshToken existing = refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN", "Invalid refresh token"));

        if (existing.isRevoked() || existing.getExpiresAt().isBefore(Instant.now())) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "TOKEN_EXPIRED", "Refresh token expired or revoked");
        }

        existing.setRevoked(true);
        refreshTokenRepository.save(existing);

        User user = userRepository.findById(existing.getUserId())
                .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED, "USER_NOT_FOUND", "User not found"));

        UUID defaultOrgId = membershipRepository.findAllByUserId(user.getId()).stream()
                .findFirst()
                .map(Membership::getOrganizationId)
                .orElse(null);

        return issueTokenPair(user, defaultOrgId);
    }

    private AuthTokens issueTokenPair(User user, UUID defaultOrgId) {
        String accessToken = jwtService.generateAccessToken(user.getId(), user.getEmail());
        String rawRefresh = UUID.randomUUID() + "." + UUID.randomUUID();

        RefreshToken rt = new RefreshToken();
        rt.setUserId(user.getId());
        rt.setTokenHash(sha256(rawRefresh));
        rt.setExpiresAt(Instant.now().plusMillis(refreshExpirationMs));
        refreshTokenRepository.save(rt);

        return new AuthTokens(accessToken, rawRefresh, defaultOrgId);
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(input.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private String toSlug(String name) {
        return name.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
    }
}
