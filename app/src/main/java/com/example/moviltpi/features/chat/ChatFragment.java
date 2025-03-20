package com.example.moviltpi.features.chat;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.moviltpi.databinding.FragmentChatBinding;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Fragmento que maneja la interfaz de chat entre usuarios.
 * Proporciona funcionalidades para enviar mensajes, observar mensajes recibidos,
 * y actualizar la interfaz según el usuario seleccionado.
 */
public class ChatFragment extends Fragment {

    private FragmentChatBinding binding; // Objeto de binding para acceder a las vistas del layout
    private ChatViewModel chatViewModel; // ViewModel que gestiona la lógica del chat
    private MensajeAdapter adapter; // Adaptador para el RecyclerView que muestra los mensajes
    private ParseUser otroUsuario; // Usuario con el que se está chateando
    private boolean isObservingMessages = false; // Bandera para verificar si se están observando mensajes

    /**
     * Método llamado cuando se crea la vista del fragmento.
     * Inicializa los componentes de la interfaz de usuario y configura su comportamiento.
     *
     * @param inflater           Inflater para inflar la vista del layout
     * @param container          Contenedor padre donde se insertará la vista
     * @param savedInstanceState Estado previo guardado, si existe
     * @return Vista inflada del fragmento
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflamos la vista usando View Binding
        binding = FragmentChatBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Configuración de la barra de herramientas (Toolbar)
        if (binding.toolbar != null) {
            ((AppCompatActivity) requireActivity()).setSupportActionBar(binding.toolbar);
        }

        // Configurar el SwipeRefreshLayout para actualizar los mensajes manualmente
        if (binding.swipeRefreshLayout2 != null) {
            binding.swipeRefreshLayout2.setOnRefreshListener(() -> {
                if (chatViewModel != null) {
                    chatViewModel.refreshMessages(); // Refresca los mensajes desde el ViewModel
                    binding.swipeRefreshLayout2.setRefreshing(false); // Detiene el indicador de refresco
                }
            });
        }

        // Configuración del RecyclerView para mostrar los mensajes
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true); // Los mensajes se apilan desde el final (abajo)
        binding.recyclerMensajes2.setLayoutManager(layoutManager);
        adapter = new MensajeAdapter(new ArrayList<>(), ParseUser.getCurrentUser());
        binding.recyclerMensajes2.setAdapter(adapter);

        // Inicialización del ViewModel compartido con la actividad
        chatViewModel = new ViewModelProvider(requireActivity()).get(ChatViewModel.class);

        // Obtener el usuario con el que se chateará
        otroUsuario = obtenerOtroUsuario();
        if (otroUsuario == null) {
            Log.d("ChatsFragment", "No hay usuario seleccionado");
            mostrarInterfazSinUsuario(); // Mostrar interfaz sin usuario
            return view;
        }

        // Configurar el título de la barra de herramientas con el nombre del usuario
        if (binding.toolbar != null && ((AppCompatActivity) requireActivity()).getSupportActionBar() != null) {
            Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar())
                    .setTitle("Chat con " + otroUsuario.getUsername());
        }

        // Mostrar la interfaz completa del chat
        mostrarInterfazConUsuario();

        // Iniciar la observación de mensajes en tiempo real
        observarMensajes();

        // Configurar el botón flotante para enviar mensajes
        binding.fabEnviar2.setOnClickListener(v -> enviarMensaje());

        return view;
    }

    /**
     * Método llamado cuando el fragmento se reanuda.
     * Reanuda la observación de mensajes y el polling si es necesario.
     */
    @Override
    public void onResume() {
        super.onResume();
        if (otroUsuario != null && !isObservingMessages) {
            observarMensajes(); // Reanudar la observación de mensajes si estaba pausada
        }
        if (chatViewModel != null) {
            chatViewModel.resumePolling(); // Reanudar el polling del ViewModel
        }
    }

    /**
     * Método llamado cuando el fragmento se pausa.
     * Detiene la observación de mensajes y pausa el polling.
     */
    @Override
    public void onPause() {
        super.onPause();
        isObservingMessages = false; // Marcar que no se están observando mensajes
        if (chatViewModel != null) {
            chatViewModel.pausePolling(); // Pausar el polling del ViewModel
        }
    }

    /**
     * Método llamado cuando la vista del fragmento se destruye.
     * Libera el objeto de binding para evitar memory leaks.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Liberar el binding para evitar referencias a vistas destruidas
    }

    /**
     * Observa los mensajes del usuario seleccionado y actualiza el RecyclerView en tiempo real.
     */
    private void observarMensajes() {
        if (otroUsuario != null) {
            isObservingMessages = true; // Indicar que se están observando mensajes
            chatViewModel.getMensajes(otroUsuario).observe(getViewLifecycleOwner(), mensajes -> {
                Log.d("ChatsFragment", "Mensajes cargados: " + mensajes.size());
                adapter.setMensajes(mensajes); // Actualizar el adaptador con los nuevos mensajes
                if (!mensajes.isEmpty()) {
                    // Desplazar el RecyclerView al último mensaje
                    binding.recyclerMensajes2.scrollToPosition(mensajes.size() - 1);
                }
            });
        }
    }

    /**
     * Obtiene el usuario seleccionado a partir de los argumentos del fragmento.
     *
     * @return El usuario seleccionado o null si no se encuentra
     */
    private ParseUser obtenerOtroUsuario() {
        Bundle bundle = getArguments();
        if (bundle != null && bundle.containsKey("otroUsuarioId")) {
            String userId = bundle.getString("otroUsuarioId");
            ParseUser user = ParseUser.createWithoutData(ParseUser.class, userId);
            try {
                user.fetchIfNeeded(); // Cargar los datos del usuario desde el servidor
                Log.d("ChatsFragment", "Usuario cargado: " + user.getUsername());
                return user;
            } catch (ParseException e) {
                Log.e("ChatsFragment", "Error al cargar usuario: ", e);
            }
        }
        return null;
    }

    /**
     * Actualiza el usuario con el que se está chateando y reinicia la observación de mensajes.
     *
     * @param newUser Nuevo usuario seleccionado
     */
    public void updateUser(ParseUser newUser) {
        this.otroUsuario = newUser;

        // Actualizar el título de la barra de herramientas
        if (getActivity() != null && ((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar()
                    .setTitle("Chat con " + otroUsuario.getUsername());
        }

        if (otroUsuario == null) {
            mostrarInterfazSinUsuario(); // Mostrar interfaz sin usuario si no hay selección
            return;
        }

        mostrarInterfazConUsuario(); // Mostrar interfaz de chat completa
        isObservingMessages = false; // Reiniciar la bandera
        observarMensajes(); // Reiniciar la observación de mensajes
    }

    /**
     * Envía un mensaje al usuario seleccionado y actualiza la interfaz.
     */
    private void enviarMensaje() {
        String texto = binding.etMensaje.getText().toString().trim();
        if (!texto.isEmpty()) {
            // Enviar el mensaje a través del ViewModel
            chatViewModel.enviarMensaje(texto, ParseUser.getCurrentUser(), otroUsuario);
            binding.etMensaje.setText(""); // Limpiar el campo de texto
            // Desplazar el RecyclerView al último mensaje después de enviarlo
            binding.recyclerMensajes2.post(() ->
                    binding.recyclerMensajes2.scrollToPosition(adapter.getItemCount() - 1));
        }
    }

    /**
     * Muestra la interfaz cuando no hay usuario seleccionado.
     * Oculta los elementos de chat y muestra un mensaje informativo.
     */
    private void mostrarInterfazSinUsuario() {
        if (getView() != null) {
            getView().post(() -> { // Ejecutar en el hilo principal
                binding.recyclerMensajes2.setVisibility(View.GONE);
                binding.etMensaje.setVisibility(View.GONE);
                binding.fabEnviar2.setVisibility(View.GONE);
                if (binding.swipeRefreshLayout2 != null) {
                    binding.swipeRefreshLayout2.setVisibility(View.GONE);
                }
                binding.tvNoUserSelected2.setVisibility(View.VISIBLE);

                // Volver atrás en la pila de fragmentos si está agregado
                if (isAdded()) {
                    requireActivity().getSupportFragmentManager().popBackStack();
                }
            });
        }
    }

    /**
     * Muestra la interfaz cuando hay un usuario seleccionado.
     * Muestra los elementos de chat y oculta el mensaje de "sin usuario".
     */
    private void mostrarInterfazConUsuario() {
        binding.tvNoUserSelected2.setVisibility(View.GONE);
        binding.recyclerMensajes2.setVisibility(View.VISIBLE);
        binding.etMensaje.setVisibility(View.VISIBLE);
        binding.fabEnviar2.setVisibility(View.VISIBLE);
        if (binding.swipeRefreshLayout2 != null) {
            binding.swipeRefreshLayout2.setVisibility(View.VISIBLE);
        }
    }
}