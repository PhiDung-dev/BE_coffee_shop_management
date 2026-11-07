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
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {

    UserRepository userRepository;
    AccountRepository accountRepository;
    UserMapper userMapper;

    @Transactional
    public UserResponse createUser(UserCreateRequest request) {
        User user = userMapper.toUser(request);

        if (request.getAccountId() != null) {
            Account account = accountRepository.findById(request.getAccountId())
                    .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_EXISTED));

            if (userRepository.existsByAccountId(request.getAccountId())) {
                throw new AppException(ErrorCode.ACCOUNT_ALREADY_LINKED);
            }
            user.setAccount(account);
        }
        return userMapper.toUserResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        userMapper.updateUser(request, user);
        if (request.getAccountId() != null) {
            Account account = accountRepository.findById(request.getAccountId())
                    .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_EXISTED));
            Long currentAccountId = user.getAccount() != null ? user.getAccount().getId() : null;
            if (!request.getAccountId().equals(currentAccountId)
                    && userRepository.existsByAccountId(request.getAccountId())) {
                throw new AppException(ErrorCode.ACCOUNT_ALREADY_LINKED);
            }
            user.setAccount(account);
        }

        return userMapper.toUserResponse(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        return userMapper.toUserResponse(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toUserResponse)
                .toList();
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        }
        userRepository.deleteById(id);
    }
}