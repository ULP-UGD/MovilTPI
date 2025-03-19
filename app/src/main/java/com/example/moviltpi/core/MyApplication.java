package com.example.moviltpi.core;

import android.app.Application;

import com.example.moviltpi.R;
import com.example.moviltpi.core.models.Comentario;
import com.example.moviltpi.core.models.Mensaje;
import com.example.moviltpi.core.models.Post;
import com.example.moviltpi.core.models.User;
import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.livequery.ParseLiveQueryClient;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Clase de aplicación personalizada que inicializa Parse y configura ParseLiveQueryClient.
 */
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Habilita el almacenamiento local de Parse.
        Parse.enableLocalDatastore(this);

        // Registra las subclases de ParseObject para su uso con Parse.
        ParseObject.registerSubclass(Post.class);
        ParseObject.registerSubclass(User.class);
        ParseObject.registerSubclass(Comentario.class);
        ParseObject.registerSubclass(Mensaje.class);

        // Inicializa Parse con las credenciales de la aplicación.
        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId(getString(R.string.back4app_app_id))
                .clientKey(getString(R.string.back4app_client_key))
                .server(getString(R.string.back4app_server_url))
                .build());

        // Configura ParseLiveQueryClient para la comunicación en tiempo real.
        try {
            // Construye la URL del WebSocket a partir de la URL del servidor Parse.
            String serverUrl = getString(R.string.back4app_server_url);
            String wsUrl = serverUrl.replace("https://", "wss://")
                    .replace("http://", "ws://")
                    .replaceAll("/parse$", "/parse/liveQuery"); // Asegura el formato correcto.

            // Crea una instancia de ParseLiveQueryClient con la URL del WebSocket.
            ParseLiveQueryClient client = ParseLiveQueryClient.Factory.getClient(new URI(wsUrl));
            // Nota: Podría ser útil guardar esta instancia en una variable estática o singleton
            // para su reutilización en toda la aplicación.
        } catch (URISyntaxException e) {
            // Maneja la excepción si la URL del WebSocket no es válida.
            e.printStackTrace();
        }

        // Configura los Access Control Lists (ACLs) predeterminados para Parse.
        // Permite acceso público de lectura y escritura a los objetos Parse.
        ParseACL defaultACL = new ParseACL();
        defaultACL.setPublicReadAccess(true);
        defaultACL.setPublicWriteAccess(true);
        ParseACL.setDefaultACL(defaultACL, true);

        // Configura ParseInstallation para notificaciones push.
        // Utiliza el ID del remitente de FCM para las notificaciones push.
        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
        installation.put("GCMSenderId", getString(R.string.fcm_sender_id)); // Usar getString() para obtener el ID de los recursos.
        installation.saveInBackground(e -> {
            // Maneja el resultado de la operación de guardado.
            if (e != null) {
                e.printStackTrace();
            }
        });
    }
}