package com.example.moviltpi.core.models;

import android.os.Bundle;

import com.parse.ParseClassName;
import com.parse.ParseObject;

import java.util.ArrayList;
import java.util.List;

@ParseClassName("Post")
public class Post extends ParseObject {

    public static final String KEY_TITULO = "titulo";
    public static final String KEY_DESCRIPCION = "descripcion";
    public static final String KEY_DURACION = "duracion";
    public static final String KEY_CATEGORIA = "categoria";
    public static final String KEY_PRESUPUESTO = "presupuesto";
    public static final String KEY_IMAGENES = "imagenes";
    public static final String KEY_USER = "user";

    public String getId() {
        return getObjectId();
    }

    public String getTitulo() {
        return getString(KEY_TITULO);
    }

    public void setTitulo(String titulo) {
        put(KEY_TITULO, titulo);
    }

    public String getDescripcion() {
        return getString(KEY_DESCRIPCION);
    }

    public void setDescripcion(String descripcion) {
        put(KEY_DESCRIPCION, descripcion);
    }

    public int getDuracion() {
        return getInt(KEY_DURACION);
    }

    public void setDuracion(int duracion) {
        put(KEY_DURACION, duracion);
    }

    public String getCategoria() {
        return getString(KEY_CATEGORIA);
    }

    public void setCategoria(String categoria) {
        put(KEY_CATEGORIA, categoria);
    }

    public double getPresupuesto() {
        return getDouble(KEY_PRESUPUESTO);
    }

    public void setPresupuesto(double presupuesto) {
        put(KEY_PRESUPUESTO, presupuesto);
    }

    public List<String> getImagenes() {
        return getList(KEY_IMAGENES);
    }

    public void setImagenes(List<String> imagenes) {
        put(KEY_IMAGENES, imagenes);
    }

    public User getUser() {
        return (User) getParseObject(KEY_USER);
    }

    public void setUser(User user) {
        put(KEY_USER, user);
    }

    public Bundle toBundle() {
        Bundle bundle = new Bundle();
        bundle.putString(KEY_TITULO, getTitulo());
        bundle.putString(KEY_DESCRIPCION, getDescripcion());
        bundle.putString(KEY_CATEGORIA, getCategoria());
        bundle.putInt(KEY_DURACION, getDuracion());
        bundle.putDouble(KEY_PRESUPUESTO, getPresupuesto());

        // Datos del Usuario
        User user = getUser();
        if (user != null) {
            bundle.putString("username", user.getUsername());
            bundle.putString("email", user.getEmail());
            bundle.putString("fotoperfil", user.getString("foto_perfil"));
        }

        // Lista de imágenes
        bundle.putStringArrayList(KEY_IMAGENES, new ArrayList<>(getImagenes()));
        return bundle;
    }
}