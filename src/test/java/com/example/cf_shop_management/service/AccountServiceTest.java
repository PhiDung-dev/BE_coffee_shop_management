package com.example.cf_shop_management.service;

import com.example.cf_shop_management.dto.request.AccountCreateRequest;
import com.example.cf_shop_management.dto.request.AccountUpdateRequest;
import com.example.cf_shop_management.dto.response.AccountResponse;
import com.example.cf_shop_management.entity.Account;
import com.example.cf_shop_management.entity.Role;
import com.example.cf_shop_management.exception.AppException;
import com.example.cf_shop_management.exception.ErrorCode;
import com.example.cf_shop_management.mapper.AccountMapper;
import com.example.cf_shop_management.repository.AccountRepository;
import com.example.cf_shop_management.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private AccountMapper accountMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AccountService accountService;

    private Role staffRole;
    private Account account;
    private AccountResponse accountResponse;

    @BeforeEach
    void setUp() {
        staffRole = Role.builder()
                .id(2L)
                .name("STAFF")
                .build();

        account = Account.builder()
                .id(1L)
                .email("staff@cfshop.vn")
                .passwordHash("encoded_password")
                .phone("0905123456")
                .status("ACTIVE")
                .role(staffRole)
                .build();

        accountResponse = AccountResponse.builder()
                .id(1L)
                .email("staff@cfshop.vn")
                .phone("0905123456")
                .status("ACTIVE")
                .roleName("STAFF")
                .build();
    }

    @Test
    void createAccount_DefaultStatusActive_Success() {
        AccountCreateRequest request = AccountCreateRequest.builder()
                .email("  staff@cfshop.vn  ")
                .password("123456")
                .phone("0905123456")
                .roleId(2L)
                .build();

        Account mappedAccount = new Account();

        when(accountRepository.existsByEmail("staff@cfshop.vn")).thenReturn(false);
        when(accountMapper.toAccount(request)).thenReturn(mappedAccount);
        when(roleRepository.findById(2L)).thenReturn(Optional.of(staffRole));
        when(passwordEncoder.encode("123456")).thenReturn("encoded_password");
        when(accountRepository.save(mappedAccount)).thenReturn(account);
        when(accountMapper.toAccountResponse(account)).thenReturn(accountResponse);

        AccountResponse result = accountService.createAccount(request);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("staff@cfshop.vn", mappedAccount.getEmail());
        assertEquals("encoded_password", mappedAccount.getPasswordHash());
        assertEquals("ACTIVE", mappedAccount.getStatus());
        assertEquals(staffRole, mappedAccount.getRole());
        verify(accountRepository).save(mappedAccount);
    }

    @Test
    void createAccount_CustomStatusProvided_KeepsStatus() {
        AccountCreateRequest request = AccountCreateRequest.builder()
                .email("staff@cfshop.vn")
                .password("123456")
                .status("INACTIVE")
                .roleId(2L)
                .build();

        Account mappedAccount = Account.builder()
                .status("INACTIVE")
                .build();

        when(accountRepository.existsByEmail("staff@cfshop.vn")).thenReturn(false);
        when(accountMapper.toAccount(request)).thenReturn(mappedAccount);
        when(roleRepository.findById(2L)).thenReturn(Optional.of(staffRole));
        when(passwordEncoder.encode("123456")).thenReturn("encoded_password");
        when(accountRepository.save(mappedAccount)).thenReturn(account);
        when(accountMapper.toAccountResponse(account)).thenReturn(accountResponse);

        accountService.createAccount(request);

        assertEquals("INACTIVE", mappedAccount.getStatus());
    }

    @Test
    void createAccount_EmailExisted_ThrowsException() {
        AccountCreateRequest request = AccountCreateRequest.builder()
                .email("  staff@cfshop.vn  ")
                .password("123456")
                .roleId(2L)
                .build();

        when(accountRepository.existsByEmail("staff@cfshop.vn")).thenReturn(true);

        AppException exception = assertThrows(AppException.class,
                () -> accountService.createAccount(request));

        assertEquals(ErrorCode.EMAIL_EXISTED, exception.getErrorCode());
        verify(accountRepository, never()).save(any());
    }

    @Test
    void createAccount_RoleNotExisted_ThrowsException() {
        AccountCreateRequest request = AccountCreateRequest.builder()
                .email("staff@cfshop.vn")
                .password("123456")
                .roleId(99L)
                .build();

        when(accountRepository.existsByEmail("staff@cfshop.vn")).thenReturn(false);
        when(accountMapper.toAccount(request)).thenReturn(new Account());
        when(roleRepository.findById(99L)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class,
                () -> accountService.createAccount(request));

        assertEquals(ErrorCode.ROLE_NOT_EXISTED, exception.getErrorCode());
        verify(accountRepository, never()).save(any());
    }

    @Test
    void updateAccount_Success() {
        Role adminRole = Role.builder().id(1L).name("ADMIN").build();
        AccountUpdateRequest request = AccountUpdateRequest.builder()
                .email("  newstaff@cfshop.vn  ")
                .password("newpass123")
                .roleId(1L)
                .status("INACTIVE")
                .build();

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(accountRepository.existsByEmailIgnoreCaseAndIdNot("newstaff@cfshop.vn", 1L)).thenReturn(false);
        when(roleRepository.findById(1L)).thenReturn(Optional.of(adminRole));
        when(passwordEncoder.encode("newpass123")).thenReturn("new_encoded_hash");
        when(accountRepository.save(account)).thenReturn(account);
        when(accountMapper.toAccountResponse(account)).thenReturn(accountResponse);

        AccountResponse result = accountService.updateAccount(1L, request);

        assertNotNull(result);
        assertEquals("newstaff@cfshop.vn", request.getEmail());
        assertEquals(adminRole, account.getRole());
        assertEquals("new_encoded_hash", account.getPasswordHash());
        verify(accountMapper).updateAccount(request, account);
        verify(accountRepository).save(account);
    }

    @Test
    void updateAccount_NotFound_ThrowsException() {
        AccountUpdateRequest request = AccountUpdateRequest.builder().status("INACTIVE").build();
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class,
                () -> accountService.updateAccount(99L, request));

        assertEquals(ErrorCode.ACCOUNT_NOT_EXISTED, exception.getErrorCode());
    }

    @Test
    void updateAccount_EmailExisted_ThrowsException() {
        AccountUpdateRequest request = AccountUpdateRequest.builder()
                .email("duplicate@cfshop.vn")
                .build();

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(accountRepository.existsByEmailIgnoreCaseAndIdNot("duplicate@cfshop.vn", 1L)).thenReturn(true);

        AppException exception = assertThrows(AppException.class,
                () -> accountService.updateAccount(1L, request));

        assertEquals(ErrorCode.EMAIL_EXISTED, exception.getErrorCode());
        verify(accountRepository, never()).save(any());
    }

    @Test
    void updateAccount_RoleNotExisted_ThrowsException() {
        AccountUpdateRequest request = AccountUpdateRequest.builder()
                .roleId(99L)
                .build();

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(roleRepository.findById(99L)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class,
                () -> accountService.updateAccount(1L, request));

        assertEquals(ErrorCode.ROLE_NOT_EXISTED, exception.getErrorCode());
        verify(accountRepository, never()).save(any());
    }

    @Test
    void getAccountById_Success() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(accountMapper.toAccountResponse(account)).thenReturn(accountResponse);

        AccountResponse result = accountService.getAccountById(1L);

        assertEquals(1L, result.getId());
        assertEquals("staff@cfshop.vn", result.getEmail());
    }

    @Test
    void getAccountById_NotFound_ThrowsException() {
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class,
                () -> accountService.getAccountById(99L));

        assertEquals(ErrorCode.ACCOUNT_NOT_EXISTED, exception.getErrorCode());
    }

    @Test
    void getAllAccounts_Success() {
        when(accountRepository.findAll()).thenReturn(List.of(account));
        when(accountMapper.toAccountResponse(account)).thenReturn(accountResponse);

        List<AccountResponse> result = accountService.getAllAccounts();

        assertEquals(1, result.size());
        assertEquals("staff@cfshop.vn", result.get(0).getEmail());
    }

    @Test
    void deleteAccount_Success() {
        when(accountRepository.existsById(1L)).thenReturn(true);
        doNothing().when(accountRepository).deleteById(1L);

        accountService.deleteAccount(1L);

        verify(accountRepository).deleteById(1L);
    }

    @Test
    void deleteAccount_NotFound_ThrowsException() {
        when(accountRepository.existsById(99L)).thenReturn(false);

        AppException exception = assertThrows(AppException.class,
                () -> accountService.deleteAccount(99L));

        assertEquals(ErrorCode.ACCOUNT_NOT_EXISTED, exception.getErrorCode());
        verify(accountRepository, never()).deleteById(any());
    }
}