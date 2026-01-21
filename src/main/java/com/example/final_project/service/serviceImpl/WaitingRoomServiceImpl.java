package com.example.final_project.service.serviceImpl;

import com.example.final_project.dto.WaitingRoomUserDto;
import com.example.final_project.service.WaitingRoomService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class WaitingRoomServiceImpl implements WaitingRoomService {
    private final Map<String, List<WaitingRoomUserDto>> waitingRooms = new ConcurrentHashMap<>();
    private final Map<Long, String> userAccessCodes = new ConcurrentHashMap<>();

    @Override
    public synchronized void addUser(String accessCode, WaitingRoomUserDto user) {
        // Remove user from any other waiting room they might be in
        removeUserFromAllRooms(user.getUserId());

        waitingRooms.computeIfAbsent(accessCode, k -> new CopyOnWriteArrayList<>()).add(user);
        userAccessCodes.put(user.getUserId(), accessCode);
    }

    @Override
    public synchronized void removeUser(String accessCode, Long userId) {
        if (accessCode != null && waitingRooms.containsKey(accessCode)) {
            waitingRooms.get(accessCode).removeIf(u -> u.getUserId().equals(userId));

            // Remove from userAccessCodes if the code matches
            String registeredCode = userAccessCodes.get(userId);
            if (accessCode.equals(registeredCode)) {
                userAccessCodes.remove(userId);
            }

            if (waitingRooms.get(accessCode).isEmpty()) {
                waitingRooms.remove(accessCode);
            }
        }
    }

    @Override
    public List<WaitingRoomUserDto> getParticipants(String accessCode) {
        return waitingRooms.getOrDefault(accessCode, new CopyOnWriteArrayList<>());
    }

    @Override
    public Optional<String> getAccessCodeByUserId(Long userId) {
        return Optional.ofNullable(userAccessCodes.get(userId));
    }

    @Override
    public synchronized void removeUserFromAllRooms(Long userId) {
        String accessCode = userAccessCodes.get(userId);
        if (accessCode != null) {
            removeUser(accessCode, userId);
        }
    }
}
