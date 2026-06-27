package com.wiredbarrack.modulith_exploration.users.internal;

import com.wiredbarrack.modulith_exploration.users.dto.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
interface UserMapper {
    User toRecord(UserEntity entity);
    UserEntity toEntity(User record);
}
