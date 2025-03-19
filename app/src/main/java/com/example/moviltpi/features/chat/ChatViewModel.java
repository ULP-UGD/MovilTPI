package com.example.moviltpi.features.chat;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.moviltpi.core.models.Mensaje;
import com.parse.ParseUser;

import java.util.List;

/**
 * ViewModel para el chat. Maneja la lógica de la interfaz de chat, incluyendo la carga y envío de mensajes.
 */
public class ChatViewModel extends ViewModel {
    private final ChatProvider chatProvider = new ChatProvider();

    /**
     * Obtiene los mensajes entre el usuario actual y otro usuario específico.
     *
     * @param otroUsuario El ParseUser con el que se está chateando.
     * @return Un LiveData que contiene la lista de mensajes.
     */
    public LiveData<List<Mensaje>> getMensajes(ParseUser otroUsuario) {
        return chatProvider.cargarMensajes(otroUsuario);
    }

    /**
     * Envía un mensaje de texto desde el remitente al destinatario.
     *
     * @param texto        El contenido del mensaje.
     * @param remitente    El ParseUser que envía el mensaje.
     * @param destinatario El ParseUser que recibe el mensaje.
     */
    public void enviarMensaje(String texto, ParseUser remitente, ParseUser destinatario) {
        if (texto == null || texto.trim().isEmpty()) {
            Log.w("ChatViewModel", "Intento de enviar un mensaje vacío");
            return;
        }

        // Usar el ChatProvider para enviar el mensaje
        chatProvider.enviarMensaje(texto, remitente, destinatario);
    }

    /**
     * Fuerza una actualización manual de los mensajes.
     */
    public void refreshMessages() {
        chatProvider.pollForNewMessages();
    }

    /**
     * Pausa el polling de nuevos mensajes.
     */
    public void pausePolling() {
        chatProvider.stopPolling();
    }

    /**
     * Reanuda el polling de nuevos mensajes.
     */
    public void resumePolling() {
        chatProvider.startPolling();
    }

    /**
     * Limpia los recursos cuando el ViewModel se destruye.
     */
    @Override
    protected void onCleared() {
        super.onCleared();
        chatProvider.cleanup();
    }
}