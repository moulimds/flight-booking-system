package com.flightbooking.auth.controller;

import com.flightbooking.auth.model.User;
import com.flightbooking.auth.service.AuthService;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final AuthService service;

    public UserController(AuthService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<User> getAllUsers() {
        return service.allUsers();
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER_SUPPORT')")
    public User getUserById(@PathVariable Long userId) {
        return service.currentUser(userId);
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
        service.deleteUser(userId);
        return ResponseEntity.ok(Map.of(
            "message", "User deleted successfully",
            "userId", userId
        ));
    }
}
