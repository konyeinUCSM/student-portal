package com.manulife.studentportal.security;

import com.manulife.studentportal.entity.User;
import com.manulife.studentportal.enums.Role;
import com.manulife.studentportal.repository.TeacherRepository;
import com.manulife.studentportal.repository.StudentRepository;
import com.manulife.studentportal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        Long profileId = resolveProfileId(user);
        return new CustomUserDetails(user, profileId);
    }

    private Long resolveProfileId(User user) {
        if (user.getRole() == Role.TEACHER) {
            return teacherRepository.findByUserId(user.getId())
                    .map(t -> t.getId())
                    .orElse(null);
        }
        if (user.getRole() == Role.STUDENT) {
            return studentRepository.findByUserId(user.getId())
                    .map(s -> s.getId())
                    .orElse(null);
        }
        return null; // ADMIN has no profile
    }
}