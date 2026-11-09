package jakarta.ecofarm_backend.infrastructure.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.security.Principal;
import java.util.Set;

@ToString
@Builder
@Getter
@AllArgsConstructor
public class UserSecurityDetailsPrincipal implements Principal {

    private Long id;
    private String email;
    private Set<String> roles;


    @Override
    public String getName() {
        return this.email;
    }

    public boolean hasRole(String role) {
        return this.roles.stream().anyMatch(r -> r.equals(role));
    }
}
