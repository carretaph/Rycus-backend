package com.rycus.Rycus_backend.user;

import com.rycus.Rycus_backend.repository.UserConnectionRepository;
import com.rycus.Rycus_backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserConnectionService {

    private final UserRepository userRepository;
    private final UserConnectionRepository connectionRepository;
    private final EmailService emailService;

    public UserConnectionService(UserRepository userRepository,
                                 UserConnectionRepository connectionRepository,
                                 EmailService emailService) {
        this.userRepository = userRepository;
        this.connectionRepository = connectionRepository;
        this.emailService = emailService;
    }

    // =====================================
    // Helper: convertir entidad -> DTO
    // =====================================
    private UserConnectionDto toDto(UserConnection connection) {
        UserConnectionDto dto = new UserConnectionDto();
        dto.setId(connection.getId());

        User requester = connection.getRequester();
        User receiver = connection.getReceiver();

        dto.setRequesterId(requester.getId());
        dto.setRequesterName(requester.getFullName());
        dto.setRequesterEmail(requester.getEmail());

        dto.setReceiverId(receiver.getId());
        dto.setReceiverName(receiver.getFullName());
        dto.setReceiverEmail(receiver.getEmail());

        dto.setStatus(connection.getStatus().name());
        dto.setCreatedAt(connection.getCreatedAt());
        return dto;
    }

    // =====================================
    // Enviar invitación
    // =====================================
    @Transactional
    public UserConnectionDto sendRequest(String fromEmail, String toEmail) {
        if (!StringUtils.hasText(fromEmail) || !StringUtils.hasText(toEmail)) {
            throw new IllegalArgumentException("Emails cannot be empty");
        }

        if (fromEmail.equalsIgnoreCase(toEmail)) {
            throw new IllegalArgumentException("You cannot connect with yourself");
        }

        User fromUser = userRepository.findByEmail(fromEmail)
                .orElseThrow(() -> new IllegalArgumentException("From user not found"));

        User toUser = userRepository.findByEmail(toEmail)
                .orElseThrow(() -> new IllegalArgumentException("To user not found"));

        // ¿ya existe conexión en alguna dirección?
        Optional<UserConnection> forward =
                connectionRepository.findByRequesterAndReceiver(fromUser, toUser);
        Optional<UserConnection> backward =
                connectionRepository.findByRequesterAndReceiver(toUser, fromUser);

        if (forward.isPresent() || backward.isPresent()) {
            // Si ya existe, NO lanzamos error 500; devolvemos la conexión existente.
            UserConnection existing = forward.orElseGet(backward::get);

            // Si está PENDING o ACCEPTED, simplemente devolvemos el DTO
            if (existing.getStatus() == ConnectionStatus.PENDING
                    || existing.getStatus() == ConnectionStatus.ACCEPTED) {
                return toDto(existing);
            }

            // Si en el futuro hubiera otros estados, podríamos recrear,
            // pero por ahora solo devolvemos lo que hay.
            return toDto(existing);
        }

        // No existía ninguna conexión → creamos una nueva PENDING
        UserConnection connection = new UserConnection();
        connection.setRequester(fromUser);
        connection.setReceiver(toUser);
        connection.setStatus(ConnectionStatus.PENDING);
        connection.setCreatedAt(LocalDateTime.now());

        UserConnection saved = connectionRepository.save(connection);

        // ✅ EMAIL: mandar aviso al receiver (NO romper si falla)
        try {
            String requesterName = (fromUser.getFullName() != null && !fromUser.getFullName().isBlank())
                    ? fromUser.getFullName()
                    : fromUser.getEmail();

            emailService.sendConnectionInviteEmail(
                    toUser.getEmail(),
                    requesterName
            );
        } catch (Exception e) {
            // Importante: NO rompemos la invitación si el mail falla.
            System.out.println("⚠️ Email failed (connection invite): " + e.getMessage());
        }

        return toDto(saved);
    }

    // =====================================
    // Listar mis contactos (ACCEPTED)
    // =====================================
    @Transactional(readOnly = true)
    public List<UserConnectionDto> getConnectionsForUser(String userEmail) {
        if (!StringUtils.hasText(userEmail)) {
            return Collections.emptyList();
        }

        User user = userRepository.findByEmail(userEmail).orElse(null);
        if (user == null) {
            return Collections.emptyList();
        }

        List<UserConnection> allConnections =
                connectionRepository.findByRequesterOrReceiver(user, user);

        return allConnections.stream()
                .filter(c -> c.getStatus() == ConnectionStatus.ACCEPTED)
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // =====================================
    // Invitaciones pendientes PARA MÍ (lista)
    // =====================================
    @Transactional(readOnly = true)
    public List<UserConnectionDto> getPendingForUser(String userEmail) {
        if (!StringUtils.hasText(userEmail)) {
            return Collections.emptyList();
        }

        User user = userRepository.findByEmail(userEmail).orElse(null);
        if (user == null) {
            return Collections.emptyList();
        }

        List<UserConnection> pending =
                connectionRepository.findByReceiverAndStatus(user, ConnectionStatus.PENDING);

        return pending.stream().map(this::toDto).collect(Collectors.toList());
    }

    // =====================================
    // Invitaciones pendientes PARA MÍ (solo count)
    // =====================================
    @Transactional(readOnly = true)
    public long getPendingCountForUser(String userEmail) {
        if (!StringUtils.hasText(userEmail)) {
            return 0L;
        }

        User user = userRepository.findByEmail(userEmail).orElse(null);
        if (user == null) {
            return 0L;
        }

        List<UserConnection> pending =
                connectionRepository.findByReceiverAndStatus(user, ConnectionStatus.PENDING);

        return pending.size();
    }

    // =====================================
    // Aceptar invitación
    // =====================================
    @Transactional
    public UserConnectionDto acceptRequest(Long connectionId, String receiverEmail) {
        if (!StringUtils.hasText(receiverEmail)) {
            throw new IllegalArgumentException("Email cannot be null");
        }

        User receiver = userRepository.findByEmail(receiverEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        UserConnection connection = connectionRepository.findById(connectionId)
                .orElseThrow(() -> new IllegalArgumentException("Connection not found"));

        if (!connection.getReceiver().getId().equals(receiver.getId())) {
            throw new IllegalStateException("You are not the receiver of this connection request");
        }

        if (connection.getStatus() != ConnectionStatus.PENDING) {
            throw new IllegalStateException("Connection is not pending");
        }

        connection.setStatus(ConnectionStatus.ACCEPTED);
        UserConnection saved = connectionRepository.save(connection);
        return toDto(saved);
    }

    // =====================================
    // Rechazar invitación (la eliminamos)
    // =====================================
    @Transactional
    public void rejectRequest(Long connectionId, String receiverEmail) {
        if (!StringUtils.hasText(receiverEmail)) {
            throw new IllegalArgumentException("Email cannot be null");
        }

        User receiver = userRepository.findByEmail(receiverEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        UserConnection connection = connectionRepository.findById(connectionId)
                .orElseThrow(() -> new IllegalArgumentException("Connection not found"));

        if (!connection.getReceiver().getId().equals(receiver.getId())) {
            throw new IllegalStateException("You are not the receiver of this connection request");
        }

        if (connection.getStatus() != ConnectionStatus.PENDING) {
            throw new IllegalStateException("Connection is not pending");
        }

        connectionRepository.delete(connection);
    }
}
