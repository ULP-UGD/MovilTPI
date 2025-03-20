package com.example.moviltpi.features.users;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.moviltpi.R;
import com.example.moviltpi.databinding.FragmentUserBinding; // Clase generada por View Binding
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

    private FragmentUserBinding binding; // Objeto de binding para acceder a las vistas
    private UserAdapter usersAdapter; // Adaptador para el RecyclerView
    private List<ParseUser> usersList; // Lista de usuarios cargados
    private AuthProvider authProvider; // Proveedor de autenticación

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
        // Inflamos la vista usando View Binding
        binding = FragmentUserBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Inicializar el proveedor de autenticación
        authProvider = new AuthProvider();

        // Configurar la barra de herramientas (Toolbar)
        if (binding.toolbar != null) {
            ((AppCompatActivity) requireActivity()).setSupportActionBar(binding.toolbar);
            Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar())
                    .setTitle("Seleccionar Usuario");
        }

        // Inicializar el RecyclerView con un LinearLayoutManager
        binding.recyclerUsers.setLayoutManager(new LinearLayoutManager(getContext()));

        // Inicializar la lista de usuarios
        usersList = new ArrayList<>();

        // Configurar el adaptador con un listener para clics en usuarios
        usersAdapter = new UserAdapter(usersList, user -> {
            Log.d("UsersFragment", "Usuario seleccionado: " + user.getUsername());

            // Verificar si ya existe un fragmento de chat
            ChatFragment existingFragment = (ChatFragment) getParentFragmentManager()
                    .findFragmentByTag("CHATS_FRAGMENT");

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
        binding.recyclerUsers.setAdapter(usersAdapter);

        // Mostrar mensaje de carga inicial
        binding.tvNoUsers.setText("Cargando usuarios...");
        binding.tvNoUsers.setVisibility(View.VISIBLE);

        // Cargar la lista de usuarios
        loadUsers();

        return view;
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
     * Carga la lista de usuarios desde el proveedor de autenticación.
     */
    @SuppressLint("NotifyDataSetChanged")
    private void loadUsers() {
        Log.d("UsersFragment", "Cargando usuarios...");

        String currentUserId = authProvider.getCurrentUserID();
        if (currentUserId == null) {
            // Manejar caso de usuario no autenticado
            Log.e("UsersFragment", "No hay usuario autenticado");
            binding.tvNoUsers.setText("Error: No hay usuario autenticado");
            binding.tvNoUsers.setVisibility(View.VISIBLE);
            binding.recyclerUsers.setVisibility(View.GONE);
            return;
        }

        // Observar los usuarios obtenidos del proveedor
        authProvider.getAllUsers().observe(getViewLifecycleOwner(), users -> {
            if (users != null) {
                usersList.clear(); // Limpiar la lista actual
                usersList.addAll(users); // Añadir los nuevos usuarios
                usersAdapter.notifyDataSetChanged(); // Notificar al adaptador del cambio

                if (usersList.isEmpty()) {
                    // Mostrar mensaje si no hay usuarios
                    Log.d("UsersFragment", "No se encontraron usuarios");
                    binding.recyclerUsers.setVisibility(View.GONE);
                    binding.tvNoUsers.setText("No hay usuarios disponibles");
                    binding.tvNoUsers.setVisibility(View.VISIBLE);
                } else {
                    // Mostrar la lista de usuarios
                    Log.d("UsersFragment", "Mostrando " + usersList.size() + " usuarios");
                    binding.recyclerUsers.setVisibility(View.VISIBLE);
                    binding.tvNoUsers.setVisibility(View.GONE);
                }
            } else {
                // Manejar error al cargar usuarios
                Log.e("UsersFragment", "Error al cargar usuarios");
                binding.recyclerUsers.setVisibility(View.GONE);
                binding.tvNoUsers.setText("Error al cargar usuarios");
                binding.tvNoUsers.setVisibility(View.VISIBLE);
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
            loadUsers(); // Recargar solo si no hay usuarios cargados
        }
    }
}