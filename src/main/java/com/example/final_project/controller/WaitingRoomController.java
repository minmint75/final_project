package com.example.final_project.controller;

import com.example.final_project.dto.WaitingRoomNotification;
import com.example.final_project.dto.WaitingRoomUserDto;
import com.example.final_project.entity.ExamOnline;
import com.example.final_project.entity.Student;
import com.example.final_project.service.CustomUserDetails;
import com.example.final_project.service.ExamOnlineService;
import com.example.final_project.service.WaitingRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.List;
import java.util.Objects;

@Controller
@RequiredArgsConstructor
public class WaitingRoomController {
    private final WaitingRoomService waitingRoomService;
    private final SimpMessagingTemplate messagingTemplate;
    private final ExamOnlineService examOnlineService;

    @MessageMapping("/waiting-room/{accessCode}/leave")
    public void leaveWaitingRoom(@DestinationVariable String accessCode, SimpMessageHeaderAccessor headerAccessor) {
        Authentication authentication = (Authentication) headerAccessor.getUser();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            Long userId = userDetails.getId();

            waitingRoomService.removeUser(accessCode, userId);
            broadcastUpdatedParticipants(accessCode);
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.wrap(event.getMessage());
        Authentication authentication = (Authentication) headerAccessor.getUser();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            Long userId = userDetails.getId();

            waitingRoomService.getAccessCodeByUserId(userId).ifPresent(accessCode -> {
                waitingRoomService.removeUser(accessCode, userId);
                broadcastUpdatedParticipants(accessCode);
            });
        }
    }

    private void broadcastUpdatedParticipants(String accessCode) {
        ExamOnline examOnline = examOnlineService.findByAccessCode(accessCode);
        List<WaitingRoomUserDto> participants = waitingRoomService.getParticipants(accessCode);
        int participantCount = participants != null ? participants.size() : 0;

        WaitingRoomNotification notification = new WaitingRoomNotification(
                examOnline.getName(),
                participantCount,
                participants,
                "A user has left the waiting room."
        );

        messagingTemplate.convertAndSend("/topic/waiting-room/" + accessCode, notification);
    }
}
