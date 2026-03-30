package com.ecommerce.auth_service.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class a {

	@GetMapping("/test")
    public String test() {
        return "Protected API Working";
    }
}
