package com.example.revhirehiringplatform.service;



import com.example.revhirehiringplatform.model.Notification;
import com.example.revhirehiringplatform.model.User;
import com.example.revhirehiringplatform.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    private User user;
    private User otherUser;
    private Notification notification;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);

        otherUser = new User();
        otherUser.setId(99L);

        notification = new Notification();
        notification.setId(10L);
        notification.setUser(user);
        notification.setMessage("Test Message");
        notification.setRead(false);
    }

    @Test
    void testCreateNotification() {
        notificationService.createNotification(user, "Hello");
        verify(notificationRepository).save(any(Notification.class));
    }



    @Test
    void testGetUnreadNotifications() {
        when(notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(notification));

        List<Notification> results = notificationService.getUnreadNotifications(user);

        assertNotNull(results);
        assertEquals(1, results.size());
    }

    @Test
    void testGetMyNotifications() {
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(notification));

        List<Notification> results = notificationService.getMyNotifications(user);

        assertNotNull(results);
        assertEquals(1, results.size());
        verify(notificationRepository).findByUserIdOrderByCreatedAtDesc(1L);
    }

    @Test
    void testMarkAsRead_Success() {
        when(notificationRepository.findById(10L)).thenReturn(Optional.of(notification));

        notificationService.markAsRead(10L, user);

        assertTrue(notification.isRead());
        verify(notificationRepository).save(notification);
    }

    @Test
    void testMarkAsRead_NotFound_ThrowsException() {
        when(notificationRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> notificationService.markAsRead(10L, user));
        verify(notificationRepository, never()).save(any());
    }

    @Test
    void testMarkAsRead_Unauthorized_ThrowsException() {
        when(notificationRepository.findById(10L)).thenReturn(Optional.of(notification));

        assertThrows(RuntimeException.class, () -> notificationService.markAsRead(10L, otherUser));
        verify(notificationRepository, never()).save(any());
    }

    @Test
    void testMarkAllAsRead() {
        Notification n1 = new Notification();
        n1.setId(1L);
        n1.setUser(user);
        n1.setRead(false);

        Notification n2 = new Notification();
        n2.setId(2L);
        n2.setUser(user);
        n2.setRead(false);

        when(notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(1L))
                .thenReturn(Arrays.asList(n1, n2));

        notificationService.markAllAsRead(user);

        assertTrue(n1.isRead());
        assertTrue(n2.isRead());
        verify(notificationRepository).saveAll(Arrays.asList(n1, n2));
    }

    @Test
    void testDeleteNotification_Success() {
        when(notificationRepository.findById(10L)).thenReturn(Optional.of(notification));

        notificationService.deleteNotification(10L, user);

        verify(notificationRepository).delete(notification);
    }

    @Test
    void testDeleteNotification_NotFound_ThrowsException() {
        when(notificationRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> notificationService.deleteNotification(10L, user));
        verify(notificationRepository, never()).delete(any());
    }

    @Test
    void testDeleteNotification_Unauthorized_ThrowsException() {
        when(notificationRepository.findById(10L)).thenReturn(Optional.of(notification));

        assertThrows(RuntimeException.class, () -> notificationService.deleteNotification(10L, otherUser));
        verify(notificationRepository, never()).delete(any());
    }


    @Test
    void testDeleteAllNotifications() {
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(notification));

        notificationService.deleteAllNotifications(user);

        verify(notificationRepository).deleteAll(List.of(notification));
    }
}