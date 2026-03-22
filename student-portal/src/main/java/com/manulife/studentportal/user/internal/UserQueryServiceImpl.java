package com.manulife.studentportal.user.internal;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.manulife.studentportal.user.User;
import com.manulife.studentportal.user.UserInfo;
import com.manulife.studentportal.user.UserQueryService;
import com.manulife.studentportal.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserQueryServiceImpl implements UserQueryService {

    private final UserRepository userRepository;

    @Override
    public Optional<UserInfo> findById(Long id) {
        return userRepository.findById(id)
                .map(this::toUserInfo);
    }

    @Override
    public boolean existsById(Long id) {
        return userRepository.existsById(id);
    }

    private UserInfo toUserInfo(User user) {
        return new UserInfo(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.isActive()
        );
    }
}
