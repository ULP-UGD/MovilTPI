package com.example.moviltpi.core.utils;

import android.util.Log;
import java.util.regex.Pattern;

public class Validaciones {
    private static final String TAG = "Validaciones";
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$");
    private static final int MIN_TEXT_LENGTH = 3;
    private static final int MIN_PASSWORD_LENGTH = 6;

    /**
     * Valida que un texto no sea nulo, no esté vacío y tenga al menos 3 caracteres.
     * @param texto Texto a validar
     * @return true si el texto es válido, false en caso contrario
     */
    public static boolean validarTexto(String texto) {
        return texto != null && !texto.trim().isEmpty() && texto.length() >= MIN_TEXT_LENGTH;
        // para reactivar la validación  solo letras y acentos:
        // && texto.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$");
    }

    /**
     * Convierte un String a un número entero y valida que sea mayor o igual a cero.
     * @param numero String a convertir y validar
     * @return El número entero si es válido, -1 en caso contrario
     */
    public static int validarNumero(String numero) {
        if (numero == null || numero.trim().isEmpty()) {
            return -1;
        }

        try {
            int valor = Integer.parseInt(numero);
            return valor >= 0 ? valor : -1;
        } catch (NumberFormatException e) {
            Log.w(TAG, "Error al convertir número: " + e.getMessage());
            return -1;
        }
    }

    /**
     * Valida que un email tenga un formato correcto.
     * @param email Email a validar
     * @return true si el email es válido, false en caso contrario
     */
    public static boolean validarMail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Valida que dos contraseñas coincidan y cumplan con los requisitos mínimos.
     * @param pass Primera contraseña
     * @param pass1 Confirmación de contraseña
     * @return null si la contraseña es válida, mensaje de error en caso contrario
     */
    public static String validarPass(String pass, String pass1) {
        Log.d(TAG, "Validando contraseñas");

        if (pass == null || pass.isEmpty()) {
            return "La contraseña no puede estar vacía";
        }

        if (pass1 == null || pass1.isEmpty()) {
            return "La confirmación de contraseña no puede estar vacía";
        }

        if (pass.length() < MIN_PASSWORD_LENGTH) {
            return "La contraseña debe tener al menos " + MIN_PASSWORD_LENGTH + " caracteres";
        }

        if (!pass.equals(pass1)) {
            return "Las contraseñas no coinciden";
        }

        // Se podría agregar validación adicional de seguridad aquí
        // Por ejemplo: if (!pass.matches(".*[A-Z].*")) return "Debe incluir al menos una mayúscula";

        return null; // Contraseña válida
    }

    /**
     * Verifica que una contraseña cumpla con los requisitos mínimos.
     * @param pass Contraseña a validar
     * @return true si la contraseña cumple los requisitos, false en caso contrario
     */
    public static boolean controlarPasword(String pass) {
        return (pass != null && pass.length() >= MIN_PASSWORD_LENGTH);
    }
}