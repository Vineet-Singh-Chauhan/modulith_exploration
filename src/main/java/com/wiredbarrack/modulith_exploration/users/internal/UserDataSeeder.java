package com.wiredbarrack.modulith_exploration.users.internal;

import lombok.AllArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
class UserDataSeeder implements CommandLineRunner {
    private UserRepository userRepository;

    @Override
    public void run(String ...args){
        userRepository.save(User.builder().name("Vineet").email("vineet.wiredbarrack@gmail.com").build());
    }
}
