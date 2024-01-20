package ru.berdnikov.telegrambot.util.errors;

import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class AuthUsernameNotFoundException extends UsernameNotFoundException {

    public AuthUsernameNotFoundException(String msg) {
        super(msg);
    }
}
