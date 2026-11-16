package com.example.cf_shop_management.service;

import com.example.cf_shop_management.dto.request.UserCreateRequest;
import com.example.cf_shop_management.dto.request.UserUpdateRequest;
import com.example.cf_shop_management.dto.response.UserResponse;
import com.example.cf_shop_management.entity.Account;
import com.example.cf_shop_management.entity.User;
import com.example.cf_shop_management.exception.AppException;
import com.example.cf_shop_management.exception.ErrorCode;
import com.example.cf_shop_management.mapper.UserMapper;
import com.example.cf_shop_management.repository.AccountRepository;
import com.example.cf_shop_management.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    private Account account;
    private User user;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        account = Account.builder()
                .id(5L)
                .email("customer@gmail.com")
                .build();

        user = User.builder()
                .id(1L)
                .fullName("Nguyễn Văn A")
                .account(account)
                .build();

        userResponse = UserResponse.builder()
                .id(1L)
                .fullName("Nguyễn Văn A")
                .build();
    }

    @Test
    void createUser_WithAccountAndFullName_Success() {
        UserCreateRequest request = UserCreateRequest.builder()
                .fullName("  Nguyễn Văn A  ")
                .accountId(5L)
                .build();

        User mappedUser = new User();

        when(userMapper.toUser(request)).thenReturn(mappedUser);
        when(accountRepository.findById(5L)).thenReturn(Optional.of(account));
        when(userRepository.existsByAccountId(5L)).thenReturn(false);
        when(userRepository.save(mappedUser)).thenReturn(user);
        when(userMapper.toUserResponse(user)).thenReturn(userResponse);

        UserResponse result = userService.createUser(request);

        assertNotNull(result);
        assertEquals("Nguyễn Văn A", mappedUser.getFullName());
        assertEquals(account, mappedUser.getAccount());
        verify(userRepository).save(mappedUser);
    }

    @Test
    void createUser_WithoutAccount_Success() {
        UserCreateRequest request = UserCreateRequest.builder()
                .fullName("Khách Vãng Lai")
                .accountId(null)
                .build();

        User mappedUser = new User();

        when(userMapper.toUser(request)).thenReturn(mappedUser);
        when(userRepository.save(mappedUser)).thenReturn(user);
        when(userMapper.toUserResponse(user)).thenReturn(userResponse);

        UserResponse result = userService.createUser(request);

        assertNotNull(result);
        assertNull(mappedUser.getAccount());
        verify(accountRepository, never()).findById(any());
        verify(userRepository).save(mappedUser);
    }

    @Test
    void createUser_AccountNotExisted_ThrowsException() {
        UserCreateRequest request = UserCreateRequest.builder()
                .fullName("Nguyễn Văn A")
                .accountId(99L)
                .build();

        when(userMapper.toUser(request)).thenReturn(new User());
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class,
                () -> userService.createUser(request));

        assertEquals(ErrorCode.ACCOUNT_NOT_EXISTED, exception.getErrorCode());
        verify(userRepository, never()).save(any());
    }

    @Test
    void createUser_AccountAlreadyLinked_ThrowsException() {
        UserCreateRequest request = UserCreateRequest.builder()
                .fullName("Nguyễn Văn A")
                .accountId(5L)
                .build();

        when(userMapper.toUser(request)).thenReturn(new User());
        when(accountRepository.findById(5L)).thenReturn(Optional.of(account));
        when(userRepository.existsByAccountId(5L)).thenReturn(true);

        AppException exception = assertThrows(AppException.class,
                () -> userService.createUser(request));

        assertEquals(ErrorCode.ACCOUNT_ALREADY_LINKED, exception.getErrorCode());
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_Success() {
        UserUpdateRequest request = UserUpdateRequest.builder()
                .fullName("  Trần Thị B  ")
                .build();

        UserResponse updatedResponse = UserResponse.builder()
                .id(1L)
                .fullName("Trần Thị B")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toUserResponse(user)).thenReturn(updatedResponse);

        UserResponse result = userService.updateUser(1L, request);

        assertEquals("Trần Thị B", request.getFullName());
        assertEquals("Trần Thị B", result.getFullName());
        verify(userMapper).updateUser(request, user);
        verify(userRepository).save(user);
    }

    @Test
    void updateUser_NotFound_ThrowsException() {
        UserUpdateRequest request = UserUpdateRequest.builder().fullName("Trần Thị B").build();
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class,
                () -> userService.updateUser(99L, request));

        assertEquals(ErrorCode.USER_NOT_EXISTED, exception.getErrorCode());
        verify(userRepository, never()).save(any());
    }

    @Test
    void getUserById_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toUserResponse(user)).thenReturn(userResponse);

        UserResponse result = userService.getUserById(1L);

        assertEquals(1L, result.getId());
        assertEquals("Nguyễn Văn A", result.getFullName());
    }

    @Test
    void getUserById_NotFound_ThrowsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class,
                () -> userService.getUserById(99L));

        assertEquals(ErrorCode.USER_NOT_EXISTED, exception.getErrorCode());
    }

    @Test
    void getAllUsers_Success() {
        when(userRepository.findAll()).thenReturn(List.of(user));
        when(userMapper.toUserResponse(user)).thenReturn(userResponse);

        List<UserResponse> result = userService.getAllUsers();

        assertEquals(1, result.size());
    }

    @Test
    void deleteUser_Success() {
        when(userRepository.existsById(1L)).thenReturn(true);
        doNothing().when(userRepository).deleteById(1L);

        userService.deleteUser(1L);

        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteUser_NotFound_ThrowsException() {
        when(userRepository.existsById(99L)).thenReturn(false);

        AppException exception = assertThrows(AppException.class,
                () -> userService.deleteUser(99L));

        assertEquals(ErrorCode.USER_NOT_EXISTED, exception.getErrorCode());
        verify(userRepository, never()).deleteById(any());
    }
}