package com.demo_system.service;

import com.demo_system.entity.User;

public interface UserService {
    User login(String username, String password);
}


