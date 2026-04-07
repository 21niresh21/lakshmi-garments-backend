package com.lakshmigarments.service.policy;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.lakshmigarments.exception.AccessDeniedException;

@Component
public class EditWindowPolicy {

    // Time window per role (minutes)
    private static final Map<String, Integer> ROLE_EDIT_WINDOW = Map.of(
        "Accounts Admin", 3
        // add more roles if needed
    );

    /**
     * Boolean check (NO exception)
     */
    public boolean canEdit(LocalDateTime createdAt, String role) {
        // Roles not in map → unlimited edit
        if (!ROLE_EDIT_WINDOW.containsKey(role)) {
        	System.out.println("not present " + role);
            return true;
        }

        int allowedMinutes = ROLE_EDIT_WINDOW.get(role);
        LocalDateTime expiryTime = createdAt.plusMinutes(allowedMinutes);

        return LocalDateTime.now().isBefore(expiryTime);
    }

    /**
     * Validation method (throws exception)
     */
    public void validateEditPermission(LocalDateTime createdAt, String role) {
        if (!canEdit(createdAt, role)) {
            throw new AccessDeniedException(
                "Edit window expired. Contact Admin"
            );
        }
    }
}
