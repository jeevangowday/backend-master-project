package com.ecommerce.auth_service.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Test {

	@GetMapping("/test")
    public String test() {
        return "Protected API Working";
    }
	
	@GetMapping("/admin")
	@PreAuthorize("hasRole('ADMIN')")
	public String adminApi() {
	    return "Admin Access";
	}
}
