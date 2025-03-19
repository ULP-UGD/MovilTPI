package com.example.moviltpi.core.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.Date;

@ParseClassName("Comentario")
public class Comentario extends ParseObject {

    public static final String KEY_TEXTO = "texto";
    public static final String KEY_POST = "post";
    public static final String KEY_USER = "user";

    public Comentario() {
    }

    public void setTexto(String texto) {
        put(KEY_TEXTO, texto);
    }

    public void setPost(ParseObject post) {
        put(KEY_POST, post);
    }

    public void setUser(ParseUser user) {
        put(KEY_USER, user);
    }

    public String getTexto() {
        return getString(KEY_TEXTO);
    }

    public String getId() {
        return getObjectId();
    }

    public ParseUser getUser() {
        return getParseUser(KEY_USER);
    }

    public Post getPost() {
        return (Post) getParseObject(KEY_POST);
    }

    public void setPost(Post post) {
        put(KEY_POST, post);
    }

    public Date getCreatedAt() {
        return super.getCreatedAt();
    }
}