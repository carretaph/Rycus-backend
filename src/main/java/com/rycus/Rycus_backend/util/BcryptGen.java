package com.rycus.Rycus_backend.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class BcryptGen {

    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        String raw = "rycus123";

        // 👇 pega aquí EL HASH que tienes en la DB
        String hashFromDb = "$2a$10$Gz8FHT/zrkad.dOPSHE4mOFbiNMFLi0Jfj1IVjVakJguku.OTlDsi";

        System.out.println("RAW: " + raw);
        System.out.println("HASH(DB): " + hashFromDb);

        boolean ok = encoder.matches(raw, hashFromDb);
        System.out.println("✅ matches? " + ok);

        // si quieres generar uno nuevo
        String newHash = encoder.encode(raw);
        System.out.println("NEW HASH: " + newHash);
        System.out.println("matches(newHash)? " + encoder.matches(raw, newHash));
    }
}
