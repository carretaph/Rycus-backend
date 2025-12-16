package com.rycus.Rycus_backend.user;

import com.rycus.Rycus_backend.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // REGISTRAR NUEVO USUARIO
    public User registerUser(String fullName, String email, String password, String phone) {

        String cleanEmail = email == null ? null : email.trim().toLowerCase();
        String cleanName = fullName == null ? null : fullName.trim();
        String cleanPhone = phone == null ? null : phone.trim();

        if (cleanEmail == null || cleanEmail.isBlank()) {
            throw new RuntimeException("Email is required");
        }
        if (cleanName == null || cleanName.isBlank()) {
            throw new RuntimeException("Full name is required");
        }
        if (password == null || password.isBlank()) {
            throw new RuntimeException("Password is required");
        }

        // Si el email ya existe, lanzamos error
        if (userRepository.existsByEmail(cleanEmail)) {
            throw new RuntimeException("El email ya está registrado: " + cleanEmail);
        }

        User user = new User();
        user.setFullName(cleanName);
        user.setEmail(cleanEmail);

        // POR AHORA guardamos el password tal cual (luego lo encriptamos)
        user.setPassword(password);

        // ✅ NEW
        user.setPhone(cleanPhone);

        user.setRole("USER");

        return userRepository.save(user);
    }

    // LOGIN
    public User login(String email, String password) {

        String cleanEmail = email == null ? null : email.trim().toLowerCase();

        User user = userRepository.findByEmail(cleanEmail)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!user.getPassword().equals(password)) {
            throw new RuntimeException("Credenciales inválidas");
        }

        return user;
    }
}
