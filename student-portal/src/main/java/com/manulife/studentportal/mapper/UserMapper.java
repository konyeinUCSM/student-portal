package com.manulife.studentportal.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.manulife.studentportal.dto.response.UserResponse;
import com.manulife.studentportal.entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponse toResponse(User user);

    List<UserResponse> toResponseList(List<User> users);
}