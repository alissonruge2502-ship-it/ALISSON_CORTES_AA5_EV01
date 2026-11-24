package com.blogpersonal.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Genera y verifica hashes de contraseñas con PBKDF2.
 * Nunca se almacena la contraseña original en la base de datos.
 */
public final class PasswordUtil {
    private static final int ITERACIONES = 120_000;
    private static final int BITS = 256;
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordUtil() {}

    public static String generarHash(String password) throws Exception {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        byte[] hash = derivar(password.toCharArray(), salt);
        return Base64.getEncoder().encodeToString(salt) + ":" +
               Base64.getEncoder().encodeToString(hash);
    }

    public static boolean verificar(String password, String valorGuardado) throws Exception {
        String[] partes = valorGuardado.split(":", 2);
        if (partes.length != 2) return false;

        byte[] salt = Base64.getDecoder().decode(partes[0]);
        byte[] hashEsperado = Base64.getDecoder().decode(partes[1]);
        byte[] hashRecibido = derivar(password.toCharArray(), salt);

        // Comparación de tiempo constante para reducir filtraciones por temporización.
        if (hashEsperado.length != hashRecibido.length) return false;
        int diferencia = 0;
        for (int i = 0; i < hashEsperado.length; i++) {
            diferencia |= hashEsperado[i] ^ hashRecibido[i];
        }
        return diferencia == 0;
    }

    private static byte[] derivar(char[] password, byte[] salt) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(password, salt, ITERACIONES, BITS);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(spec).getEncoded();
        } finally {
            spec.clearPassword();
        }
    }
}
