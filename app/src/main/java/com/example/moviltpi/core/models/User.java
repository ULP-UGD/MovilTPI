package com.example.moviltpi.core.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;

@ParseClassName("User")
public class User extends ParseObject {

    public static final String KEY_RED_SOCIAL = "redSocial";
    public static final String KEY_FOTO_PERFIL = "fotoperfil";
    public static final String KEY_USERNAME = "username";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_PASSWORD = "password";

    public User() {
        // Constructor vacío necesario para Parse
    }

    public String getRedSocial() {
        return getString(KEY_RED_SOCIAL);
    }

    public void setRedSocial(String redSocial) {
        if (redSocial != null) {
            put(KEY_RED_SOCIAL, redSocial);
        }
    }

    public String getFotoperfil() {
        return getString(KEY_FOTO_PERFIL);
    }

    public void setFotoperfil(String fotoperfil) {
        if (fotoperfil != null) {
            put(KEY_FOTO_PERFIL, fotoperfil);
        }
    }

    public String getUsername() {
        return getString(KEY_USERNAME);
    }

    public void setUsername(String username) {
        put(KEY_USERNAME, username);
    }

    public String getEmail() {
        return getString(KEY_EMAIL);
    }

    public void setEmail(String email) {
        if (email != null) {
            put(KEY_EMAIL, email);
        }
    }

    public String getPassword() {
        return getString(KEY_PASSWORD);
    }

    public void setPassword(String password) {
        put(KEY_PASSWORD, password);
    }

    public String getId() {
        return getObjectId();
    }
}