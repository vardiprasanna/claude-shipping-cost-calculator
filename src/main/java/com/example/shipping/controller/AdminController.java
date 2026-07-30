package com.example.shipping.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdminController {

    @GetMapping("/api/admin/rates")
    public ResponseEntity<Void> rates() {
        return ResponseEntity.ok().build();
    }
}
