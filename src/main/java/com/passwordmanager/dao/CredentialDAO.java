package com.passwordmanager.dao;

import com.passwordmanager.model.Credential;
import java.util.List;
import java.util.Optional;

public interface CredentialDAO {
    boolean addCredential(Credential credential);
    List<Credential> findCredentialsByUserId(int userId);
    Optional<Credential> findCredentialById(int credentialId, int userId);
    boolean updateCredential(Credential credential);
    boolean deleteCredential(int credentialId, int userId);
    List<Credential> searchCredentials(int userId, String keyword);
}
