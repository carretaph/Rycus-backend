package com.rycus.Rycus_backend.user;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/connections")
@CrossOrigin
public class UserConnectionController {

    private final UserConnectionService connectionService;

    public UserConnectionController(UserConnectionService connectionService) {
        this.connectionService = connectionService;
    }

    @PostMapping("/request")
    public UserConnectionDto requestConnection(@RequestBody ConnectionRequestDto request) {
        return connectionService.sendRequest(request.getFromEmail(), request.getToEmail());
    }

    @GetMapping("/my")
    public List<UserConnectionDto> myConnections(@RequestParam("email") String email) {
        return connectionService.getConnectionsForUser(email);
    }

    @GetMapping("/pending")
    public List<UserConnectionDto> pendingConnections(@RequestParam("email") String email) {
        return connectionService.getPendingForUser(email);
    }

    @GetMapping("/pending/count")
    public PendingCountDto pendingCount(@RequestParam("email") String email) {
        return new PendingCountDto(connectionService.getPendingCountForUser(email));
    }

    @PostMapping("/{id}/accept")
    public UserConnectionDto accept(@PathVariable Long id,
                                    @RequestParam("email") String email) {
        return connectionService.acceptRequest(id, email);
    }

    @PostMapping("/{id}/reject")
    public void reject(@PathVariable Long id,
                       @RequestParam("email") String email) {
        connectionService.rejectRequest(id, email);
    }
}