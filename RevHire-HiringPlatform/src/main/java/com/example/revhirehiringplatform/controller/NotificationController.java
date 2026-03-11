package com.example.revhirehiringplatform.controller;



import com.example.revhirehiringplatform.model.Notification;
import com.example.revhirehiringplatform.model.User;
import com.example.revhirehiringplatform.security.UserDetailsImpl;
import com.example.revhirehiringplatform.repository.UserRepository;
import com.example.revhirehiringplatform.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    private User getUserFromContext(UserDetailsImpl userDetails) {
        if (userDetails == null)
            return null;
        Optional<User> userOpt = userRepository.findById(userDetails.getId());
        return userOpt.orElse(null);
    }

    @GetMapping
    public ResponseEntity<?> getMyNotifications(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = getUserFromContext(userDetails);
        if (user == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        List<Notification> notifications = notificationService.getMyNotifications(user);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/unread")
    public ResponseEntity<?> getUnreadNotifications(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = getUserFromContext(userDetails);
        if (user == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        List<Notification> notifications = notificationService.getUnreadNotifications(user);
        return ResponseEntity.ok(notifications);
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable("id") Long id,
                                        @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = getUserFromContext(userDetails);
        if (user == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        try {
            notificationService.markAsRead(id, user);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/mark-all-read")
    public ResponseEntity<?> markAllAsRead(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = getUserFromContext(userDetails);
        if (user == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        notificationService.markAllAsRead(user);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNotification(@PathVariable("id") Long id,
                                                @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = getUserFromContext(userDetails);
        if (user == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        try {
            notificationService.deleteNotification(id, user);
            return ResponseEntity.ok("Notification deleted");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/all")
    public ResponseEntity<?> deleteAllNotifications(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = getUserFromContext(userDetails);
        if (user == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        notificationService.deleteAllNotifications(user);
        return ResponseEntity.ok("All notifications deleted");
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getNotification(@PathVariable("id") Long id,
                                             @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = getUserFromContext(userDetails);
        if (user == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        return ResponseEntity.ok("Notification details");
    }

    @PostMapping("/broadcast")
    public ResponseEntity<?> broadcastNotification(@RequestBody Notification notification) {

        return ResponseEntity.ok("Broadcast sent");
    }
}