package jakarta.ecofarm_backend.infrastructure.service.impl;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.security.enterprise.credential.Credential;
import jakarta.security.enterprise.credential.UsernamePasswordCredential;
import jakarta.security.enterprise.identitystore.CredentialValidationResult;
import jakarta.security.enterprise.identitystore.IdentityStore;

@ApplicationScoped
public class IdentityStoreServiceImpl implements IdentityStore {

    @Override
    public CredentialValidationResult validate(Credential credential) {
        if (credential instanceof UsernamePasswordCredential upc) {
//            User u = userRepository.findByEmail(upc.getCaller()).orElse(null);
//            if (u != null && passwordHash.verify(upc.getPassword().getValue(), u.getPasswordHash())) {
//                return new CredentialValidationResult(u.getEmail(), u.getRoleNames());
//            }
            return CredentialValidationResult.INVALID_RESULT;
        }
        return CredentialValidationResult.NOT_VALIDATED_RESULT;
    }
}
