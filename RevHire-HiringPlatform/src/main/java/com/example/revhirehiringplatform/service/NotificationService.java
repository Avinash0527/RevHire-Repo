package com.example.revhirehiringplatform.service;

import com.example.revhirehiringplatform.model.Notification;
import com.example.revhirehiringplatform.model.User;
import com.example.revhirehiringplatform.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;

    @Transactional
    public void createNotification(User user, String message) {
        createNotification(user, message, false, null, null);
    }

    @Transactional
    public void createNotification(User user, String message, boolean sendEmail, String subject, String emailBody) {
        log.info("Creating notification for user {}: {}", user.getEmail(), message);
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle("Update");
        notification.setMessage(message);
        notification.setType("INFO");
        notification.setRead(false);
        notificationRepository.save(notification);

        if (sendEmail && emailService != null) {
            try {
                if (subject != null && emailBody != null) {
                    emailService.sendEmail(user.getEmail(), subject, emailBody);
                } else {
                    emailService.sendEmail(user.getEmail(), "RevHire Notification", message);
                }
            } catch (Exception e) {
                log.error("Failed to send email notification to {}", user.getEmail(), e);
            }
        }
    }

    public List<Notification> getMyNotifications(User user) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
    }

    public List<Notification> getUnreadNotifications(User user) {
        return notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(user.getId());
    }

    @Transactional
    public void markAsRead(Long notificationId, User user) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (!notification.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead(User user) {
        List<Notification> unread = notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(user.getId());
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }

    @Transactional
    public void deleteNotification(Long notificationId, User user) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        if (!notification.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }
        notificationRepository.delete(notification);
    }

    @Transactional
    public void deleteAllNotifications(User user) {
        List<Notification> all = notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        notificationRepository.deleteAll(all);
    }
}