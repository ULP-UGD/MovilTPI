package com.example.moviltpi.features.auth;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * ViewModel para la actividad principal (MainActivity) que maneja la lógica de inicio de sesión y verificación de sesión activa.
 */
public class MainViewModel extends ViewModel {
    public final AuthProvider authProvider;

    /**
     * Constructor para MainViewModel. Inicializa el proveedor de autenticación.
     */
    public MainViewModel() {
        authProvider = new AuthProvider();
    }

    /**
     * Inicia sesión de un usuario con el email y la contraseña proporcionados.
     *
     * @param email    El email del usuario.
     * @param password La contraseña del usuario.
     * @return Un LiveData que contiene el ID del usuario si el inicio de sesión es exitoso, o null si falla.
     */
    public LiveData<String> login(String email, String password) {
        MutableLiveData<String> loginResult = new MutableLiveData<>();
        authProvider.signIn(email, password).observeForever(loginResult::setValue);
        return loginResult;
    }

    /**
     * Verifica si hay una sesión activa.
     *
     * @return Un LiveData que contiene true si hay una sesión activa, o false si no la hay.
     */
    public LiveData<Boolean> verificarSesionActiva() {
        MutableLiveData<Boolean> si = new MutableLiveData<>();

        if (authProvider.getCurrentUserID() != null) {
            si.setValue(true);
        } else {
            si.setValue(false);
        }
        return si;
    }
}