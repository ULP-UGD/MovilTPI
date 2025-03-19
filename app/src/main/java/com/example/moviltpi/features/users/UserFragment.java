package com.example.moviltpi.features.users;

import android.annotation.SuppressLint;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.example.moviltpi.R;
import com.example.moviltpi.features.auth.AuthProvider;
import com.example.moviltpi.features.chat.ChatFragment;
import com.parse.ParseUser;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Fragmento que muestra una lista de usuarios para selección.
 * Permite navegar a un chat con el usuario seleccionado.
 */
public class UserFragment extends Fragment {
    private RecyclerView recyclerView;      // RecyclerView para mostrar la lista de usuarios
    private UserAdapter usersAdapter;       // Adaptador para el RecyclerView
    private List<ParseUser> usersList;      // Lista de usuarios cargados
    private TextView tvNoUsers;             // TextView para mensajes cuando no hay usuarios
    private AuthProvider authProvider;      // Proveedor de autenticación

    /**
     * Crea la vista del fragmento y configura sus componentes.
     *
     * @param inflater           El inflador de layouts
     * @param container          El contenedor padre
     * @param savedInstanceState Estado guardado de la instancia
     * @return La vista inflada del fragmento
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user, container, false);

        authProvider = new AuthProvider();  // Inicializar el proveedor de autenticación

        // Configurar la barra de herramientas
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        if (toolbar != null) {
            ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
            Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).setTitle("Seleccionar Usuario");
        }

        // Inicializar el RecyclerView
        recyclerView = view.findViewById(R.id.recyclerUsers);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        tvNoUsers = view.findViewById(R.id.tvNoUsers);  // TextView para mensajes

        usersList = new ArrayList<>();  // Inicializar la lista de usuarios

        // Configurar el adaptador con un listener para clics en usuarios
        usersAdapter = new UserAdapter(usersList, user -> {
            Log.d("UsersFragment", "Usuario seleccionado: " + user.getUsername());

            // Verificar si ya existe un fragmento de chat
            ChatFragment existingFragment = (ChatFragment) getParentFragmentManager().findFragmentByTag("CHATS_FRAGMENT");

            if (existingFragment == null) {
                // Crear un nuevo fragmento de chat si no existe
                ChatFragment chatsFragment = new ChatFragment();
                Bundle bundle = new Bundle();
                bundle.putString("otroUsuarioId", user.getObjectId());
                chatsFragment.setArguments(bundle);

                getParentFragmentManager().beginTransaction()
                        .replace(R.id.container, chatsFragment, "CHATS_FRAGMENT")
                        .addToBackStack(null)
                        .commit();
            } else {
                // Actualizar el fragmento existente con el nuevo usuario
                existingFragment.updateUser(user);
            }
        });
        recyclerView.setAdapter(usersAdapter);

        // Mostrar mensaje de carga inicial
        tvNoUsers.setText("Cargando usuarios...");
        tvNoUsers.setVisibility(View.VISIBLE);

        loadUsers();  // Cargar la lista de usuarios

        return view;
    }

    /**
     * Carga la lista de usuarios desde el proveedor de autenticación.
     */
    @SuppressLint("NotifyDataSetChanged")
    private void loadUsers() {
        Log.d("UsersFragment", "Cargando usuarios...");

        String currentUserId = authProvider.getCurrentUserID();
        if (currentUserId == null) {
            // Manejar caso de usuario no autenticado
            Log.e("UsersFragment", "No hay usuario autenticado");
            tvNoUsers.setText("Error: No hay usuario autenticado");
            tvNoUsers.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            return;
        }

        // Observar los usuarios obtenidos del proveedor
        authProvider.getAllUsers().observe(getViewLifecycleOwner(), users -> {
            if (users != null) {
                usersList.clear();  // Limpiar la lista actual
                usersList.addAll(users);  // Añadir los nuevos usuarios
                usersAdapter.notifyDataSetChanged();  // Notificar al adaptador del cambio

                if (usersList.isEmpty()) {
                    // Mostrar mensaje si no hay usuarios
                    Log.d("UsersFragment", "No se encontraron usuarios");
                    recyclerView.setVisibility(View.GONE);
                    tvNoUsers.setText("No hay usuarios disponibles");
                    tvNoUsers.setVisibility(View.VISIBLE);
                } else {
                    // Mostrar la lista de usuarios
                    Log.d("UsersFragment", "Mostrando " + usersList.size() + " usuarios");
                    recyclerView.setVisibility(View.VISIBLE);
                    tvNoUsers.setVisibility(View.GONE);
                }
            } else {
                // Manejar error al cargar usuarios
                Log.e("UsersFragment", "Error al cargar usuarios");
                recyclerView.setVisibility(View.GONE);
                tvNoUsers.setText("Error al cargar usuarios");
                tvNoUsers.setVisibility(View.VISIBLE);
            }
        });
    }

    /**
     * Se ejecuta al reanudar el fragmento.
     * Recarga los usuarios solo si la lista está vacía.
     */
    @Override
    public void onResume() {
        super.onResume();
        if (usersList.isEmpty()) {
            loadUsers();  // Recargar solo si no hay usuarios cargados
        }
    }
}