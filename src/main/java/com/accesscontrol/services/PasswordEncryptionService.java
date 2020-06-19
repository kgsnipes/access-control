package com.accesscontrol.services;

public interface PasswordEncryptionService {

    String encryptPassword(String password);

    Boolean isPasswordEncrypted(String password);
}
