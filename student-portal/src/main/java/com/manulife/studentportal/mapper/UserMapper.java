package com.manulife.studentportal.mapper;

import com.manulife.studentportal.dto.response.UserResponse;
import com.manulife.studentportal.entity.User;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponse toResponse(User user);

    List<UserResponse> toResponseList(List<User> users);
}