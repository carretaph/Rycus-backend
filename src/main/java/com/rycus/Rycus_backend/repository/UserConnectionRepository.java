package com.rycus.Rycus_backend.repository;

import com.rycus.Rycus_backend.user.ConnectionStatus;
import com.rycus.Rycus_backend.user.User;
import com.rycus.Rycus_backend.user.UserConnection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserConnectionRepository extends JpaRepository<UserConnection, Long> {

    // Para evitar duplicados (una invitación de A a B)
    Optional<UserConnection> findByRequesterAndReceiver(User requester, User receiver);

    // ✅ Lista todas las conexiones donde el usuario participa (como requester o receiver)
    List<UserConnection> findByRequesterOrReceiver(User requester, User receiver);

    // ✅ Invitaciones pendientes recibidas por un usuario
    List<UserConnection> findByReceiverAndStatus(User receiver, ConnectionStatus status);
}
