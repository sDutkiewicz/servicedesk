package com.servicedesk.service;

import com.servicedesk.entity.Account;
import com.servicedesk.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AccountRepository accountRepository;

    public UserAccount authenticate(String login, String password) {
        Optional<Account> account = accountRepository.findByLoginAndPassword(login, password);

        if (account.isPresent()) {
            Account acc = account.get();
            Long userId = acc.getUser() != null ? acc.getUser().getId() : null;
            Long technicianId = acc.getTechnician() != null ? acc.getTechnician().getId() : null;
            return new UserAccount(acc.getLogin(), acc.getPassword(), acc.getRole(), userId, technicianId);
        }

        return null;
    }

    public static class UserAccount {
        public String login;
        public String password;
        public String role;
        public Long userId;
        public Long technicianId;

        public UserAccount(String login, String password, String role, Long userId, Long technicianId) {
            this.login = login;
            this.password = password;
            this.role = role;
            this.userId = userId;
            this.technicianId = technicianId;
        }
    }
}

