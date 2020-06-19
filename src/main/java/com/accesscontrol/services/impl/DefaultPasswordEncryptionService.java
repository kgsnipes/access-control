package com.accesscontrol.services.impl;

import com.accesscontrol.constants.AccessControlConfigConstants;
import com.accesscontrol.services.PasswordEncryptionService;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Properties;

public class DefaultPasswordEncryptionService implements PasswordEncryptionService {

    @Autowired
    @Qualifier(AccessControlConfigConstants.ACCESS_CONTROL_CONFIG)
    private Properties accessControlConfigProperties;

    @Override
    public String encryptPassword(String password) {
        String digest=accessControlConfigProperties.getProperty(AccessControlConfigConstants.PasswordEncryption.PASSWORD_DIGEST,"MD5");
        String salt=accessControlConfigProperties.getProperty(AccessControlConfigConstants.PasswordEncryption.PASSWORD_SALT,"");
        String pepper=accessControlConfigProperties.getProperty(AccessControlConfigConstants.PasswordEncryption.PASSWORD_PEPPER,"");
        String encryptedFlag=accessControlConfigProperties.getProperty(AccessControlConfigConstants.PasswordEncryption.PASSWORD_ENCRYTPION_FLAG,"__IS_ENCRYPTED__");
        return StringUtils.join(new String(DigestUtils.getDigest(digest).digest(StringUtils.join(salt,password,pepper).getBytes())),encryptedFlag);
    }

    @Override
    public Boolean isPasswordEncrypted(String password) {
        String encryptedFlag=accessControlConfigProperties.getProperty(AccessControlConfigConstants.PasswordEncryption.PASSWORD_ENCRYTPION_FLAG,"__IS_ENCRYPTED__");
        return password.endsWith(encryptedFlag);
    }


}
