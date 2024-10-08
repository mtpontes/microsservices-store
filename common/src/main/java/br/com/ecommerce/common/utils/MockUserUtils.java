package br.com.ecommerce.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import br.com.ecommerce.common.user.UserDetailsImpl;

/**
 * Utility class for mocking a user in the Spring Security context, extending the functionality
 * provided by the {@link WithMockUser} annotation.
 * <p>
 * This class is intended to be used in conjunction with the {@link WithMockUser} annotation. 
 * It reuses the mock user generated by {@link WithMockUser} but enhances it by assigning a 
 * dynamic user ID. The ID is used by various services, either as an object attribute or as 
 * a primary key.
 * </p>
 * 
 * <p>
 * The utility replaces the authenticated user's {@code UserDetails} with a new instance 
 * that includes the specified {@code userId} and retains the mock user's role.
 * </p>
 */
public class MockUserUtils {

    private static final Logger log = LoggerFactory.getLogger(MockUserUtils.class);

    /**
     * Mocks the user in the Spring Security context, adding a user ID while preserving 
     * the role provided by the {@link WithMockUser} annotation.
     * <p>
     * This method retrieves the user's authorities (roles) from the current security context, 
     * then creates a new {@link UserDetailsImpl} instance with the provided {@code userId} and 
     * the existing role. Finally, it updates the security context with the new mock user.
     * </p>
     * 
     * @param userId the ID of the user to mock, typically used as an attribute or primary key
     */
    public static void mockUser(String userId) {
        var authorities = SecurityContextHolder.getContext().getAuthentication().getAuthorities();

        String role = authorities.iterator().next().getAuthority().replace("ROLE_", "");
        var user = new UserDetailsImpl(userId, null, role);
        log.debug("MOCKED: {}", userId);

        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
    }
}