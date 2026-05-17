package com.wiredbarrack.modulith_exploration.users;

import org.springframework.stereotype.Service;

@Service
public class UserService {
    private UserRepository userRepository;
    public User getUser(Integer id){
        return userRepository.findById(id).orElseThrow(()-> new RuntimeException("No user found with given id"));
    }
}
