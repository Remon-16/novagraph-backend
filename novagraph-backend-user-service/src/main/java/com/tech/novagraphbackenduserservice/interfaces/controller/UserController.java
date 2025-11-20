package com.tech.novagraphbackenduserservice.interfaces.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class UserController {

    @GetMapping("/hello")
    public String demo(){
        return "Hello World";
    }
}
