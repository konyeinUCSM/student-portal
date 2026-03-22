package com.manulife.studentportal.user.internal;

import java.util.List;

import org.mapstruct.Mapper;

import com.manulife.studentportal.user.User;
import com.manulife.studentportal.user.web.UserResponse;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponse toResponse(User user);

    List<UserResponse> toResponseList(List<User> users);
}