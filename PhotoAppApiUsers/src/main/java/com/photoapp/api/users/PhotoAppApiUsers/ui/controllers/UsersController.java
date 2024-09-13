package com.photoapp.api.users.PhotoAppApiUsers.ui.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UsersController {

    @Autowired
    private Environment env;

    //@Autowired
    //UsersService usersService;

    @GetMapping("/status/check")
    public String status()
    {
        return "Working on port " + env.getProperty("local.server.port");
    }

}
