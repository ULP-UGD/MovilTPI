package com.example.moviltpi.features.chat;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.moviltpi.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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

    private ChatViewModel chatViewModel; // ViewModel para gestionar la lógica del chat
    private MensajeAdapter adapter; // Adaptador para el RecyclerView de mensajes
    private EditText etMensaje; // Campo de texto para escribir mensajes
    private ParseUser otroUsuario; // Usuario con el que se está chateando
    private RecyclerView recyclerView; // RecyclerView que muestra los mensajes
    private FloatingActionButton fabEnviar; // Botón flotante para enviar mensajes
    private TextView tvNoUserSelected; // Texto que indica que no hay usuario seleccionado
    private SwipeRefreshLayout swipeRefreshLayout; // Layout para refrescar mensajes
    private boolean isObservingMessages = false; // Bandera para verificar si se están observando mensajes

    /**
     * Método llamado cuando se crea la vista del fragmento.
     * Inicializa los componentes de la interfaz de usuario y configura su comportamiento.
     *
     * @param inflater           Inflater para inflar la vista
     * @param container          Contenedor de la vista
     * @param savedInstanceState Estado previo guardado
     * @return Vista inflada del fragmento
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        // Configuración de la barra de herramientas
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        if (toolbar != null) {
            ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        }

        // Inicialización de los elementos de la interfaz de usuario
        recyclerView = view.findViewById(R.id.recyclerMensajes2);
        etMensaje = view.findViewById(R.id.etMensaje);
        fabEnviar = view.findViewById(R.id.fabEnviar2);
        tvNoUserSelected = view.findViewById(R.id.tvNoUserSelected2);

        // Configurar SwipeRefreshLayout si existe en el layout
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout2);
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(() -> {
                if (chatViewModel != null) {
                    chatViewModel.refreshMessages();
                    swipeRefreshLayout.setRefreshing(false);
                }
            });
        }

        // Si el TextView para "Sin usuario seleccionado" no existe, lo creamos dinámicamente
        if (tvNoUserSelected == null) {
            tvNoUserSelected = new TextView(getContext());
            tvNoUserSelected.setText("Selecciona un usuario para chatear");
            tvNoUserSelected.setTextSize(16);
            tvNoUserSelected.setPadding(32, 64, 32, 64);
            tvNoUserSelected.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            ((ViewGroup) recyclerView.getParent()).addView(tvNoUserSelected);
        }

        // Configuración del RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true); // Hace que los mensajes se apilen desde abajo
        recyclerView.setLayoutManager(layoutManager);
        adapter = new MensajeAdapter(new ArrayList<>(), ParseUser.getCurrentUser());
        recyclerView.setAdapter(adapter);

        // Inicializar el ViewModel
        chatViewModel = new ViewModelProvider(requireActivity()).get(ChatViewModel.class);

        // Obtener el usuario seleccionado
        otroUsuario = obtenerOtroUsuario();
        if (otroUsuario == null) {
            Log.d("ChatsFragment", "No hay usuario seleccionado");
            mostrarInterfazSinUsuario();
            return view;
        }

        // Configuración de la barra de herramientas con el nombre del usuario
        if (toolbar != null && ((AppCompatActivity) requireActivity()).getSupportActionBar() != null) {
            Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar())
                    .setTitle("Chat con " + otroUsuario.getUsername());
        }

        // Mostrar la interfaz de chat
        mostrarInterfazConUsuario();

        // Observar los mensajes del usuario seleccionado
        observarMensajes();

        // Configurar el botón de enviar mensaje
        fabEnviar.setOnClickListener(v -> enviarMensaje());

        return view;
    }

    /**
     * Método llamado cuando el fragmento se reanuda.
     * Asegura que se estén observando los mensajes y reanuda el polling si es necesario.
     */
    @Override
    public void onResume() {
        super.onResume();
        if (otroUsuario != null && !isObservingMessages) {
            observarMensajes();
        }
        if (chatViewModel != null) {
            chatViewModel.resumePolling();
        }
    }

    /**
     * Método llamado cuando el fragmento se pausa.
     * Detiene la observación de mensajes y pausa el polling.
     */
    @Override
    public void onPause() {
        super.onPause();
        isObservingMessages = false;
        if (chatViewModel != null) {
            chatViewModel.pausePolling();
        }
    }

    /**
     * Observa los mensajes del usuario seleccionado y actualiza el RecyclerView.
     */
    private void observarMensajes() {
        if (otroUsuario != null) {
            isObservingMessages = true;
            chatViewModel.getMensajes(otroUsuario).observe(getViewLifecycleOwner(), mensajes -> {
                Log.d("ChatsFragment", "Mensajes cargados: " + mensajes.size());
                adapter.setMensajes(mensajes);
                if (!mensajes.isEmpty()) {
                    recyclerView.scrollToPosition(mensajes.size() - 1);
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
                user.fetchIfNeeded(); // Asegura que tenga los datos cargados
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

        if (getActivity() != null && ((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Chat con " + otroUsuario.getUsername());
        }

        if (otroUsuario == null) {
            mostrarInterfazSinUsuario();
            return;
        }

        mostrarInterfazConUsuario();
        isObservingMessages = false;
        observarMensajes();
    }

    /**
     * Envía un mensaje al usuario seleccionado y actualiza la interfaz.
     */
    private void enviarMensaje() {
        String texto = etMensaje.getText().toString().trim();
        if (!texto.isEmpty()) {
            chatViewModel.enviarMensaje(texto, ParseUser.getCurrentUser(), otroUsuario);
            etMensaje.setText("");

            // Desplazar el RecyclerView hacia abajo
            recyclerView.post(() -> recyclerView.scrollToPosition(adapter.getItemCount() - 1));
        }
    }

    /**
     * Muestra la interfaz cuando no hay usuario seleccionado.
     */
    private void mostrarInterfazSinUsuario() {
        if (getView() != null) {
            getView().post(() -> { // Usar post para asegurar que se ejecute en el hilo principal
                recyclerView.setVisibility(View.GONE);
                etMensaje.setVisibility(View.GONE);
                fabEnviar.setVisibility(View.GONE);
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setVisibility(View.GONE);
                }
                tvNoUserSelected.setVisibility(View.VISIBLE);

                if (isAdded()) { // Verificar si el fragmento está agregado antes de intentar popBackStack
                    requireActivity().getSupportFragmentManager().popBackStack();
                }
            });
        }
    }

    /**
     * Muestra la interfaz cuando hay un usuario seleccionado.
     */
    private void mostrarInterfazConUsuario() {
        tvNoUserSelected.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        etMensaje.setVisibility(View.VISIBLE);
        fabEnviar.setVisibility(View.VISIBLE);
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setVisibility(View.VISIBLE);
        }
    }
}