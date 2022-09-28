package com.pmrodrigues.commons.stringutils;

import com.pmrodrigues.commons.dtos.Password;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;


@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PasswordGenerator {

    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public static Password generatePassword() {
        val password = RandomStringUtils.randomAlphabetic(16);
        return Password.builder().cleanPassword(password)
                .password(encoder(password))
                .build();
    }

    public static String encoder(final String value) {
        return encoder.encode(value);
    }

    public static PasswordEncoder getPasswordEncoder() {
        return encoder;
    }
}
