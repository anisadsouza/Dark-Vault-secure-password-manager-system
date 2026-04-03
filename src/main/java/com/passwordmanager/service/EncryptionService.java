package com.passwordmanager.service;

public interface EncryptionService {
    String encrypt(String plainText);
    String decrypt(String cipherText);
}
