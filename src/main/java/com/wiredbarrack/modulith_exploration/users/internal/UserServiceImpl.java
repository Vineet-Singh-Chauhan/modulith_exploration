package com.wiredbarrack.modulith_exploration.users.internal;

import com.wiredbarrack.modulith_exploration.users.dto.User;
import com.wiredbarrack.modulith_exploration.users.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper mapper;

    public User getUser(Integer id){
        UserEntity userEntity = userRepository.findById(id).orElseThrow(()-> new RuntimeException("No user found with given id"));
        return mapper.toRecord(userEntity);
    }
}
