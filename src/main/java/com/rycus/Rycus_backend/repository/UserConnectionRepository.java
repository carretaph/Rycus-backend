package com.rycus.Rycus_backend.repository;

import com.rycus.Rycus_backend.user.ConnectionStatus;
import com.rycus.Rycus_backend.user.User;
import com.rycus.Rycus_backend.user.UserConnection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserConnectionRepository extends JpaRepository<UserConnection, Long> {

    // Para evitar duplicados (una invitación de A a B)
    Optional<UserConnection> findByRequesterAndReceiver(User requester, User receiver);

    // ✅ Lista todas las conexiones donde el usuario participa (como requester o receiver)
    // (OJO: este método es fácil de usar mal en el service. Mejor usa los @Query de abajo.)
    List<UserConnection> findByRequesterOrReceiver(User requester, User receiver);

    // ✅ Invitaciones pendientes recibidas por un usuario
    List<UserConnection> findByReceiverAndStatus(User receiver, ConnectionStatus status);

    /* =========================================================
       ✅ FIX DEFINITIVO (RECOMENDADO)
       ========================================================= */

    // ✅ Mis conexiones ACEPTADAS (yo soy requester o receiver)
    @Query("""
      select uc from UserConnection uc
      where uc.status = :status
        and (uc.requester = :me or uc.receiver = :me)
      order by uc.createdAt desc
    """)
    List<UserConnection> findMyConnectionsByStatus(@Param("me") User me,
                                                   @Param("status") ConnectionStatus status);

    // ✅ Mis invitaciones ENTRANTES por status (ej: PENDING)
    @Query("""
      select uc from UserConnection uc
      where uc.status = :status
        and uc.receiver = :me
      order by uc.createdAt desc
    """)
    List<UserConnection> findMyIncomingByStatus(@Param("me") User me,
                                                @Param("status") ConnectionStatus status);

    // ✅ (Opcional pero útil) Buscar por ID si lo necesitas en accept/reject
    Optional<UserConnection> findById(Long id);
}