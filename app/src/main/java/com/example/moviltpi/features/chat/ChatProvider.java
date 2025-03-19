package com.example.moviltpi.features.chat;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.moviltpi.core.models.Mensaje;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.livequery.ParseLiveQueryClient;
import com.parse.livequery.SubscriptionHandling;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Proveedor para la gestión de chats entre usuarios.
 * Esta clase implementa un sistema de comunicación en tiempo real utilizando Parse LiveQuery
 * y un mecanismo de polling como respaldo para asegurar la recepción de mensajes.
 * Proporciona funcionalidades para:
 * - Cargar mensajes históricos entre dos usuarios
 * - Enviar nuevos mensajes
 * - Mantener una conexión en tiempo real para recibir actualizaciones
 * - Manejar fallos en la conexión con un sistema de polling
 */
public class ChatProvider {
    /** Nombre de la clase en Parse para los mensajes */
    private static final String CLASS_NAME = "Mensaje";

    /** Tag para los logs */
    private static final String TAG = "ChatProvider";

    /** Intervalo en milisegundos para el polling de nuevos mensajes */
    private static final long POLLING_INTERVAL = 5000; // 5 segundos

    /** Cliente LiveQuery para suscripciones en tiempo real */
    private final ParseLiveQueryClient liveQueryClient;

    /** LiveData que contiene la lista de mensajes */
    private final MutableLiveData<List<Mensaje>> mensajesLiveData;

    /** Usuario con el que se está chateando actualmente */
    private ParseUser currentChatUser;

    /** Suscripción actual de LiveQuery */
    private SubscriptionHandling<Mensaje> currentSubscription;

    /** Consulta actual de LiveQuery */
    private ParseQuery<Mensaje> currentQuery;

    // Variables para el polling
    /** Handler para programar el polling en el hilo principal */
    private final Handler pollingHandler = new Handler(Looper.getMainLooper());

    /** Flag atómico para controlar si el polling está activo */
    private final AtomicBoolean isPolling = new AtomicBoolean(false);

    /** Timestamp del último mensaje recibido */
    private Date lastMessageTimestamp = null;

    /** Set para mantener registro de los IDs de mensajes procesados y evitar duplicados */
    private final Set<String> processedMessageIds = new HashSet<>();

    /**
     * Runnable que realiza el polling periódico de nuevos mensajes
     */
    private final Runnable pollingRunnable = new Runnable() {
        @Override
        public void run() {
            if (isPolling.get() && currentChatUser != null) {
                pollForNewMessages();
                // Programar la próxima ejecución
                pollingHandler.postDelayed(this, POLLING_INTERVAL);
            }
        }
    };

    /**
     * Constructor de ChatProvider.
     * Inicializa el cliente LiveQuery y la lista de mensajes vacía.
     */
    public ChatProvider() {
        liveQueryClient = ParseLiveQueryClient.Factory.getClient();
        mensajesLiveData = new MutableLiveData<>(new ArrayList<>());
    }

    /**
     * Envía un nuevo mensaje al destinatario.
     *
     * @param texto Contenido del mensaje a enviar
     * @param remitente Usuario que envía el mensaje
     * @param destinatario Usuario que recibe el mensaje
     */
    public void enviarMensaje(String texto, ParseUser remitente, ParseUser destinatario) {
        if (texto == null || texto.trim().isEmpty()) {
            Log.w(TAG, "Intento de enviar mensaje vacío");
            return;
        }

        Mensaje mensaje = new Mensaje();
        mensaje.setTexto(texto);
        mensaje.setRemitente(remitente);
        mensaje.setDestinatario(destinatario);

        // Guardar el mensaje en el servidor
        mensaje.saveInBackground(e -> {
            if (e == null) {
                Log.d(TAG, "Mensaje enviado correctamente");
                // Actualizar la marca de tiempo del último mensaje
                lastMessageTimestamp = new Date();
                // Forzar una actualización inmediata
                pollForNewMessages();
            } else {
                Log.e(TAG, "Error al enviar mensaje: " + e.getMessage());
            }
        });
    }

    /**
     * Carga los mensajes de chat con otro usuario y configura las suscripciones
     * para recibir actualizaciones en tiempo real.
     *
     * @param otroUsuario Usuario con el que se está chateando
     * @return LiveData con la lista de mensajes que se actualizará automáticamente
     */
    public LiveData<List<Mensaje>> cargarMensajes(@NonNull ParseUser otroUsuario) {
        // Cancelar cualquier suscripción anterior
        unsubscribeFromLiveQuery();

        // Detener el polling anterior si existe
        stopPolling();

        // Limpiar el conjunto de IDs de mensajes procesados
        processedMessageIds.clear();

        // Almacenar el usuario actual del chat para referencia
        this.currentChatUser = otroUsuario;

        // Cargar mensajes iniciales
        cargarMensajesIniciales(otroUsuario);

        // Intentar configurar LiveQuery
        try {
            setupLiveQuery(otroUsuario);
        } catch (Exception e) {
            Log.e(TAG, "Error al configurar LiveQuery: " + e.getMessage(), e);
            // Si falla LiveQuery, asegurarse de que el polling esté activo
            startPolling();
        }

        // Iniciar polling como respaldo
        startPolling();

        return mensajesLiveData;
    }

    /**
     * Carga los mensajes históricos entre el usuario actual y otro usuario.
     *
     * @param otroUsuario Usuario con el que se está chateando
     */
    private void cargarMensajesIniciales(@NonNull ParseUser otroUsuario) {
        // Crear consultas para mensajes enviados y recibidos
        ParseQuery<Mensaje> query = ParseQuery.getQuery(CLASS_NAME);
        query.whereEqualTo("remitente", ParseUser.getCurrentUser());
        query.whereEqualTo("destinatario", otroUsuario);

        ParseQuery<Mensaje> queryReceived = ParseQuery.getQuery(CLASS_NAME);
        queryReceived.whereEqualTo("remitente", otroUsuario);
        queryReceived.whereEqualTo("destinatario", ParseUser.getCurrentUser());

        List<ParseQuery<Mensaje>> queries = new ArrayList<>();
        queries.add(query);
        queries.add(queryReceived);

        ParseQuery<Mensaje> mainQuery = ParseQuery.or(queries);
        mainQuery.addAscendingOrder("createdAt");
        mainQuery.include("remitente");
        mainQuery.include("destinatario");

        // Establecer un límite alto para asegurar que obtenemos todos los mensajes
        mainQuery.setLimit(1000);

        // Cargar mensajes existentes
        mainQuery.findInBackground((mensajes, e) -> {
            if (e == null) {
                // Ordenar los mensajes por createdAt en memoria
                mensajes.sort(Comparator.comparing(ParseObject::getCreatedAt));

                // Almacenar los IDs para evitar duplicados posteriormente
                for (Mensaje mensaje : mensajes) {
                    processedMessageIds.add(mensaje.getObjectId());
                }

                mensajesLiveData.postValue(mensajes);

                // Actualizar la marca de tiempo del último mensaje
                if (!mensajes.isEmpty()) {
                    lastMessageTimestamp = mensajes.get(mensajes.size() - 1).getCreatedAt();
                    Log.d(TAG, "Último mensaje recibido: " + lastMessageTimestamp);
                }
            } else {
                Log.e(TAG, "Error al cargar mensajes iniciales: ", e);
                mensajesLiveData.postValue(new ArrayList<>());
            }
        });
    }

    /**
     * Realiza una consulta para buscar nuevos mensajes desde la última actualización.
     * Este método se ejecuta periódicamente cuando el polling está activo.
     */
    void pollForNewMessages() {
        if (currentChatUser == null) {
            Log.d(TAG, "No hay usuario de chat activo para realizar polling");
            return;
        }

        Log.d(TAG, "Ejecutando polling para nuevos mensajes");

        // Crear consultas para mensajes enviados y recibidos
        ParseQuery<Mensaje> query = ParseQuery.getQuery(CLASS_NAME);
        query.whereEqualTo("remitente", ParseUser.getCurrentUser());
        query.whereEqualTo("destinatario", currentChatUser);

        ParseQuery<Mensaje> queryReceived = ParseQuery.getQuery(CLASS_NAME);
        queryReceived.whereEqualTo("remitente", currentChatUser);
        queryReceived.whereEqualTo("destinatario", ParseUser.getCurrentUser());

        List<ParseQuery<Mensaje>> queries = new ArrayList<>();
        queries.add(query);
        queries.add(queryReceived);

        ParseQuery<Mensaje> mainQuery = ParseQuery.or(queries);

        // Si tenemos una marca de tiempo, solo buscar mensajes más recientes
        if (lastMessageTimestamp != null) {
            mainQuery.whereGreaterThan("createdAt", lastMessageTimestamp);
        }

        mainQuery.addAscendingOrder("createdAt");
        mainQuery.include("remitente");
        mainQuery.include("destinatario");

        // Buscar nuevos mensajes
        mainQuery.findInBackground((nuevosMensajes, e) -> {
            if (e == null && !nuevosMensajes.isEmpty()) {
                Log.d(TAG, "Polling encontró " + nuevosMensajes.size() + " nuevos mensajes");

                // Obtener la lista actual
                List<Mensaje> currentList = mensajesLiveData.getValue() != null ?
                        new ArrayList<>(mensajesLiveData.getValue()) : new ArrayList<>();

                // Agregar solo mensajes que no existan ya
                boolean hayNuevosMensajes = false;
                for (Mensaje nuevoMensaje : nuevosMensajes) {
                    String messageId = nuevoMensaje.getObjectId();

                    // Verificar si ya hemos procesado este mensaje
                    if (!processedMessageIds.contains(messageId)) {
                        currentList.add(nuevoMensaje);
                        processedMessageIds.add(messageId);
                        hayNuevosMensajes = true;
                        Log.d(TAG, "Nuevo mensaje agregado por polling: " + messageId);
                    }
                }

                // Actualizar la lista solo si hay cambios
                if (hayNuevosMensajes) {
                    // Ordenar por fecha
                    currentList.sort(Comparator.comparing(ParseObject::getCreatedAt));
                    mensajesLiveData.postValue(currentList);

                    // Actualizar la marca de tiempo del último mensaje
                    lastMessageTimestamp = currentList.get(currentList.size() - 1).getCreatedAt();
                    Log.d(TAG, "Timestamp actualizado a: " + lastMessageTimestamp);
                }
            } else if (e != null) {
                Log.e(TAG, "Error en polling: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Inicia el mecanismo de polling para buscar nuevos mensajes periódicamente.
     * Utiliza un flag atómico para evitar iniciar múltiples hilos de polling.
     */
    public void startPolling() {
        if (isPolling.compareAndSet(false, true)) {
            Log.d(TAG, "Iniciando polling de mensajes");
            pollingHandler.post(pollingRunnable);
        } else {
            Log.d(TAG, "El polling ya está activo");
        }
    }

    /**
     * Detiene el mecanismo de polling.
     */
    public void stopPolling() {
        if (isPolling.compareAndSet(true, false)) {
            Log.d(TAG, "Deteniendo polling de mensajes");
            pollingHandler.removeCallbacks(pollingRunnable);
        }
    }

    /**
     * Configura las suscripciones LiveQuery para recibir actualizaciones en tiempo real.
     *
     * @param otroUsuario Usuario con el que se está chateando
     * @throws RuntimeException Si hay un problema al configurar la suscripción
     */
    private void setupLiveQuery(@NonNull ParseUser otroUsuario) {
        // Crear consultas para mensajes enviados y recibidos
        ParseQuery<Mensaje> querySent = ParseQuery.getQuery(CLASS_NAME);
        querySent.whereEqualTo("remitente", ParseUser.getCurrentUser());
        querySent.whereEqualTo("destinatario", otroUsuario);

        ParseQuery<Mensaje> queryReceived = ParseQuery.getQuery(CLASS_NAME);
        queryReceived.whereEqualTo("remitente", otroUsuario);
        queryReceived.whereEqualTo("destinatario", ParseUser.getCurrentUser());

        List<ParseQuery<Mensaje>> queries = new ArrayList<>();
        queries.add(querySent);
        queries.add(queryReceived);

        ParseQuery<Mensaje> mainQuery = ParseQuery.or(queries);

        // Guardar la consulta actual para poder cancelar la suscripción más tarde
        currentQuery = mainQuery;

        // Incluir los objetos de usuario para evitar tener que cargarlos después
        mainQuery.include("remitente");
        mainQuery.include("destinatario");

        // Suscribirse a la consulta para actualizaciones en tiempo real
        currentSubscription = liveQueryClient.subscribe(mainQuery);

        if (currentSubscription == null) {
            throw new RuntimeException("No se pudo crear la suscripción LiveQuery");
        }

        // Configurar el manejador para nuevos mensajes
        currentSubscription.handleEvent(SubscriptionHandling.Event.CREATE, (query, mensaje) -> {
            Log.d(TAG, "LiveQuery: Nuevo mensaje recibido: " + mensaje.getTexto());

            // Actualizar la marca de tiempo del último mensaje si es más reciente
            if (mensaje.getCreatedAt() != null &&
                    (lastMessageTimestamp == null || mensaje.getCreatedAt().after(lastMessageTimestamp))) {
                lastMessageTimestamp = mensaje.getCreatedAt();
                Log.d(TAG, "Timestamp actualizado por LiveQuery a: " + lastMessageTimestamp);
            }

            // Verificar si ya hemos procesado este mensaje
            String messageId = mensaje.getObjectId();
            if (!processedMessageIds.contains(messageId)) {
                // Obtener la lista actual de mensajes
                List<Mensaje> currentList = mensajesLiveData.getValue() != null ?
                        new ArrayList<>(mensajesLiveData.getValue()) : new ArrayList<>();

                // Agregar el nuevo mensaje
                currentList.add(mensaje);
                processedMessageIds.add(messageId);

                // Ordenar y actualizar
                currentList.sort(Comparator.comparing(ParseObject::getCreatedAt));
                mensajesLiveData.postValue(currentList);

                Log.d(TAG, "Mensaje agregado por LiveQuery: " + messageId);
            } else {
                Log.d(TAG, "Mensaje ya procesado, ignorando: " + messageId);
            }
        });

        // Manejar eventos de actualización de mensajes
        currentSubscription.handleEvent(SubscriptionHandling.Event.UPDATE, (query, mensaje) -> {
            Log.d(TAG, "LiveQuery: Mensaje actualizado: " + mensaje.getTexto());

            List<Mensaje> currentList = mensajesLiveData.getValue() != null ?
                    new ArrayList<>(mensajesLiveData.getValue()) : new ArrayList<>();

            // Buscar y reemplazar el mensaje actualizado
            boolean encontrado = false;
            for (int i = 0; i < currentList.size(); i++) {
                if (currentList.get(i).getObjectId().equals(mensaje.getObjectId())) {
                    currentList.set(i, mensaje);
                    encontrado = true;
                    Log.d(TAG, "Mensaje actualizado en la posición " + i);
                    break;
                }
            }

            // Si no se encontró, podría ser un mensaje nuevo
            if (!encontrado && !processedMessageIds.contains(mensaje.getObjectId())) {
                currentList.add(mensaje);
                processedMessageIds.add(mensaje.getObjectId());
                currentList.sort(Comparator.comparing(ParseObject::getCreatedAt));
                Log.d(TAG, "Mensaje no encontrado para actualizar, agregado como nuevo");
            }

            // Actualizar la lista si hubo cambios
            if (encontrado || !processedMessageIds.contains(mensaje.getObjectId())) {
                mensajesLiveData.postValue(currentList);
            }
        });

        // Manejar eventos de eliminación de mensajes
        currentSubscription.handleEvent(SubscriptionHandling.Event.DELETE, (query, mensaje) -> {
            Log.d(TAG, "LiveQuery: Mensaje eliminado: " + mensaje.getObjectId());

            // Verificar que tengamos un ID válido
            String messageId = mensaje.getObjectId();
            if (messageId == null || messageId.isEmpty()) {
                Log.w(TAG, "ID de mensaje inválido para eliminación");
                return;
            }

            List<Mensaje> currentList = mensajesLiveData.getValue() != null ?
                    new ArrayList<>(mensajesLiveData.getValue()) : new ArrayList<>();

            // Eliminar el mensaje de la lista
            boolean removido = currentList.removeIf(m -> m.getObjectId().equals(messageId));

            // Actualizar solo si se eliminó algún mensaje
            if (removido) {
                processedMessageIds.remove(messageId);
                mensajesLiveData.postValue(currentList);
                Log.d(TAG, "Mensaje eliminado correctamente: " + messageId);
            }
        });

        Log.d(TAG, "LiveQuery configurado correctamente para el usuario: " + otroUsuario.getUsername());
    }

    /**
     * Cancela la suscripción actual a LiveQuery si existe.
     * Este método debe llamarse al cambiar de chat o al cerrar la aplicación.
     */
    public void unsubscribeFromLiveQuery() {
        if (currentSubscription != null && currentQuery != null) {
            try {
                // La forma correcta de cancelar una suscripción en Parse LiveQuery
                liveQueryClient.unsubscribe(currentQuery);
                currentSubscription = null;
                currentQuery = null;
                Log.d(TAG, "Suscripción a LiveQuery cancelada");
            } catch (Exception e) {
                Log.e(TAG, "Error al cancelar suscripción a LiveQuery: " + e.getMessage(), e);
            }
        }
    }

    /**
     * Limpia todos los recursos utilizados por este proveedor.
     * Debe llamarse en el método onDestroy de la actividad o fragmento.
     */
    public void cleanup() {
        Log.d(TAG, "Limpiando recursos de ChatProvider");
        unsubscribeFromLiveQuery();
        stopPolling();
        currentChatUser = null;
        lastMessageTimestamp = null;
        processedMessageIds.clear();
    }

    /**
     * Obtiene el usuario con el que se está chateando actualmente.
     *
     * @return El usuario actual de chat o null si no hay ninguno
     */
    @Nullable
    public ParseUser getCurrentChatUser() {
        return currentChatUser;
    }

    /**
     * Verifica si el polling está activo.
     *
     * @return true si el polling está ejecutándose, false en caso contrario
     */
    public boolean isPollingActive() {
        return isPolling.get();
    }
}