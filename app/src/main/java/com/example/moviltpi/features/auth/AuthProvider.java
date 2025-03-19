package com.example.moviltpi.features.auth;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.moviltpi.core.models.User;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Proveedor de servicios de autenticación que gestiona el inicio de sesión, registro y cierre de sesión de usuarios,
 * así como la recuperación de información de usuarios desde Parse.
 */
public class AuthProvider {

    /**
     * Constructor vacío para AuthProvider.
     */
    public AuthProvider() {}

    /**
     * Inicia sesión de un usuario con el email y contraseña proporcionados.
     *
     * @param email    El email del usuario.
     * @param password La contraseña del usuario.
     * @return Un LiveData que contiene el ID del usuario si la autenticación es exitosa, o null si falla.
     */
    public LiveData<String> signIn(String email, String password) {
        MutableLiveData<String> authResult = new MutableLiveData<>();
        ParseUser.logInInBackground(email, password, (user, e) -> {
            if (e == null) {
                authResult.setValue(user.getObjectId());
                Log.d("AuthProvider", "Usuario autenticado exitosamente: " + user.getObjectId());
            } else {
                Log.e("AuthProvider", "Error en inicio de sesión: ", e);
                authResult.setValue(null);
            }
        });
        return authResult;
    }

    /**
     * Registra un nuevo usuario con la información proporcionada.
     *
     * @param user El objeto User que contiene la información del nuevo usuario.
     * @return Un LiveData que contiene el ID del usuario si el registro es exitoso, o null si falla.
     */
    public LiveData<String> signUp(User user) {
        MutableLiveData<String> authResult = new MutableLiveData<>();

        if (user.getUsername() == null || user.getEmail() == null) {
            Log.e("AuthProvider", "Username o Email son nulos. No se puede registrar el usuario.");
            authResult.setValue(null);
            return authResult;
        }

        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            Log.e("AuthProvider", "La contraseña no puede estar vacía.");
            authResult.setValue(null);
            return authResult;
        }

        ParseUser parseUser = new ParseUser();
        parseUser.setUsername(user.getUsername());
        parseUser.setPassword(user.getPassword());  // Establecer la contraseña en ParseUser
        parseUser.setEmail(user.getEmail());

        parseUser.signUpInBackground(e -> {
            if (e == null) {
                authResult.setValue(parseUser.getObjectId());
                Log.d("AuthProvider", "Usuario registrado exitosamente: " + parseUser.getObjectId());
            } else {
                Log.e("AuthProvider", "Error en registro: ", e);
                authResult.setValue(null);
            }
        });

        return authResult;
    }

    /**
     * Cierra la sesión del usuario actual.
     *
     * @return Un LiveData que contiene true si el cierre de sesión es exitoso, o false si falla.
     */
    public LiveData<Boolean> logout() {
        MutableLiveData<Boolean> logoutResult = new MutableLiveData<>();
        ParseUser.logOutInBackground(e -> {
            if (e == null) {
                logoutResult.setValue(true);
                Log.d("AuthProvider", "Caché eliminada y usuario desconectado.");
            } else {
                logoutResult.setValue(false);
                Log.e("AuthProvider", "Error al desconectar al usuario: ", e);
            }
        });
        return logoutResult;
    }

    /**
     * Obtiene el ID del usuario actualmente autenticado.
     *
     * @return El ID del usuario actual, o null si no hay usuario autenticado.
     */
    public String getCurrentUserID() {
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null) {
            Log.d("AuthProvider", "ID del usuario actual: " + currentUser.getObjectId());
            return currentUser.getObjectId();
        } else {
            Log.e("AuthProvider", "No hay usuario autenticado.");
            return null;
        }
    }

    /**
     * Obtiene una lista de todos los usuarios registrados, excluyendo al usuario actual.
     *
     * @return Un LiveData que contiene una lista de ParseUser, o null si ocurre un error.
     */
    public LiveData<List<ParseUser>> getAllUsers() {
        MutableLiveData<List<ParseUser>> usersResult = new MutableLiveData<>();
        ParseQuery<ParseUser> query = ParseUser.getQuery();

        // Incluir explícitamente el username
        query.include("username");
        query.orderByAscending("username");

        query.findInBackground((users, e) -> {
            if (e == null) {
                if (users == null || users.isEmpty()) {
                    Log.d("AuthProvider", "No se encontraron usuarios en Parse.");
                    usersResult.setValue(new ArrayList<>());
                } else {
                    Log.d("AuthProvider", "Usuarios obtenidos: " + users.size());

                    for (ParseUser user : users) {
                        Log.d("AuthProvider", "Usuario encontrado: " + user.getUsername() + " - ID: " + user.getObjectId());
                    }

                    // Filtrar manualmente al usuario actual
                    ParseUser currentUser = ParseUser.getCurrentUser();
                    String currentUserId = currentUser != null ? currentUser.getObjectId() : "";

                    List<ParseUser> filteredUsers = new ArrayList<>();
                    for (ParseUser user : users) {
                        if (!user.getObjectId().equals(currentUserId)) {
                            filteredUsers.add(user);
                        }
                    }

                    Log.d("AuthProvider", "Usuarios después de filtrar: " + filteredUsers.size());
                    usersResult.setValue(filteredUsers);
                }
            } else {
                Log.e("AuthProvider", "Error al obtener usuarios: ", e);
                usersResult.setValue(null);
            }
        });

        return usersResult;
    }
}