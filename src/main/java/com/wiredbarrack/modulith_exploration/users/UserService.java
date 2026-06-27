package com.wiredbarrack.modulith_exploration.users;

import com.wiredbarrack.modulith_exploration.users.dto.User;
import org.springframework.stereotype.Service;

@Service
public interface UserService {
    User getUser(Integer id);
}
