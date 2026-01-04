package com.rycus.Rycus_backend.user;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/connections")
@CrossOrigin // por si acaso, para localhost:5173
public class UserConnectionController {

    private final UserConnectionService connectionService;

    public UserConnectionController(UserConnectionService connectionService) {
        this.connectionService = connectionService;
    }

    // POST /connections/request
    @PostMapping("/request")
    public UserConnectionDto requestConnection(@RequestBody ConnectionRequestDto request) {
        return connectionService.sendRequest(request.getFromEmail(), request.getToEmail());
    }

    // GET /connections/my?email=algo@test.com
    @GetMapping("/my")
    public List<UserConnectionDto> myConnections(@RequestParam("email") String email) {
        return connectionService.getConnectionsForUser(email);
    }

    // 👉 NUEVO: invitaciones pendientes para mí (yo soy el receiver)
    // GET /connections/pending?email=yo@test.com
    @GetMapping("/pending")
    public List<UserConnectionDto> pendingConnections(@RequestParam("email") String email) {
        return connectionService.getPendingForUser(email);
    }

    // 👉 NUEVO: aceptar invitación
    // POST /connections/{id}/accept?email=yo@test.com
    @PostMapping("/{id}/accept")
    public UserConnectionDto accept(@PathVariable Long id,
                                    @RequestParam("email") String email) {
        return connectionService.acceptRequest(id, email);
    }

    // 👉 NUEVO: rechazar invitación
    // POST /connections/{id}/reject?email=yo@test.com
    @PostMapping("/{id}/reject")
    public void reject(@PathVariable Long id,
                       @RequestParam("email") String email) {
        connectionService.rejectRequest(id, email);
    }
}
