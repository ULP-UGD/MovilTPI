package com.example.moviltpi.features.auth;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

/**
 * ViewModel para la gestión de la autenticación, específicamente para la operación de cierre de sesión.
 */
public class AuthViewModel extends ViewModel {
    private final AuthProvider authProvider;

    /**
     * Constructor para AuthViewModel. Inicializa el proveedor de autenticación.
     */
    public AuthViewModel() {
        this.authProvider = new AuthProvider();
    }

    /**
     * Solicita el cierre de sesión a través del proveedor de autenticación.
     *
     * @return Un LiveData que indica si el cierre de sesión fue exitoso o no.
     */
    public LiveData<Boolean> logout() {
        return authProvider.logout();
    }
}