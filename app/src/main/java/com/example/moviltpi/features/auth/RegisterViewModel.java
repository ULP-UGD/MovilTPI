package com.example.moviltpi.features.auth;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

import com.example.moviltpi.core.models.User;

/**
 * ViewModel para la actividad de registro (RegisterActivity).
 * Maneja la lógica de registro de usuarios y proporciona el resultado del registro.
 */
public class RegisterViewModel extends ViewModel {
    private final MutableLiveData<String> registerResult = new MutableLiveData<>();
    private final AuthProvider authProvider;

    /**
     * Constructor para RegisterViewModel. Inicializa el proveedor de autenticación.
     */
    public RegisterViewModel() {
        this.authProvider = new AuthProvider();
    }

    /**
     * Obtiene el resultado del registro como un LiveData.
     *
     * @return Un LiveData que contiene el ID del usuario registrado si el registro es exitoso, o null si falla.
     */
    public LiveData<String> getRegisterResult() {
        return registerResult;
    }

    /**
     * Realiza el registro de un nuevo usuario.
     *
     * @param user El objeto User que contiene la información del nuevo usuario.
     */
    public void register(User user) {
        // Llama al método signUp del proveedor de autenticación.
        LiveData<String> result = authProvider.signUp(user);

        // Observa el resultado del registro.
        result.observeForever(new Observer<>() {
            @Override
            public void onChanged(String objectId) {
                if (objectId != null) {
                    // Registro exitoso, establece el resultado en el LiveData.
                    registerResult.setValue(objectId);
                    Log.d("RegisterViewModel", "Usuario registrado con ID: " + objectId);
                } else {
                    // Registro fallido, establece el resultado en null.
                    registerResult.setValue(null);
                    Log.e("RegisterViewModel", "Error durante el registro.");
                }

                // Remueve el observador para evitar múltiples llamadas.
                result.removeObserver(this);
            }
        });
    }
}