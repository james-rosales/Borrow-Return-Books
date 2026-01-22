
package com.example.demo.util;

import org.mindrot.jbcrypt.BCrypt; 

public class PasswordHasher {

    
    public static String hashPassword(String plaintextPassword) {
        return BCrypt.hashpw(plaintextPassword, BCrypt.gensalt());
    }

    
    public static boolean checkPassword(String plaintextPassword, String hashedPassword) {
        if (hashedPassword == null || (!hashedPassword.startsWith("$2a$") && !hashedPassword.startsWith("$2y$") && !hashedPassword.startsWith("$2b$"))) {
            
            
            return false;
        }
        return BCrypt.checkpw(plaintextPassword, hashedPassword);
    }
}