package com.wiredbarrack.modulith_exploration.users.internal;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
interface UserRepository extends CrudRepository<UserEntity,Integer> {}
