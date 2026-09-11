package com.fraudService.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@Slf4j
public class FraudCaseSecurityEvaluator {

    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_ANALYST = "ROLE_ANALYST";
    public static final String ROLE_INVESTIGATOR = "ROLE_INVESTIGATOR";

    /**
     * Validates if actor has authorization to perform investigator case management actions.
     */
    public void validateAuthorization(String actor, String rolesHeader) {
        if (actor == null || actor.trim().isEmpty()) {
            log.warn("Access denied: missing actor/user-id header");
            throw new SecurityException("Authorization failed: User ID header (X-User-Id) is required.");
        }

        if (rolesHeader == null || rolesHeader.trim().isEmpty()) {
            log.warn("Access denied for actor {}: missing roles header", actor);
            throw new SecurityException("Authorization failed: User Roles header (X-User-Roles) is required.");
        }

        boolean authorized = Arrays.stream(rolesHeader.split(","))
                .map(String::trim)
                .anyMatch(role -> role.equalsIgnoreCase(ROLE_ADMIN)
                        || role.equalsIgnoreCase(ROLE_ANALYST)
                        || role.equalsIgnoreCase(ROLE_INVESTIGATOR)
                        || role.equalsIgnoreCase("ADMIN")
                        || role.equalsIgnoreCase("ANALYST")
                        || role.equalsIgnoreCase("INVESTIGATOR"));

        if (!authorized) {
            log.warn("Access denied for actor {}: insufficient roles '{}'", actor, rolesHeader);
            throw new SecurityException("Authorization failed: User " + actor + " does not possess investigator/analyst permissions.");
        }
    }
}
