package com.example.moviltpi.features.posts;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.moviltpi.R;
import com.example.moviltpi.core.models.Post;
import com.example.moviltpi.databinding.FragmentHomeBinding;
import com.example.moviltpi.features.auth.AuthViewModel;
import com.example.moviltpi.features.auth.MainActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragmento que muestra la lista de publicaciones (posts) en la pantalla principal.
 * Permite al usuario crear nuevas publicaciones y cerrar sesión.
 */
public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding; // Objeto de binding para acceder a las vistas
    private PostViewModel postViewModel; // ViewModel para gestionar las publicaciones
    private AuthViewModel authViewModel; // ViewModel para la autenticación
    private PostAdapter postAdapter; // Adaptador para el RecyclerView
    private boolean isFirstLoad = true; // Bandera para la primera carga

    /**
     * Constructor vacío requerido por el sistema de fragmentos.
     */
    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Crea una nueva instancia del fragmento HomeFragment.
     *
     * @return Una nueva instancia de HomeFragment
     */
    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflamos la vista usando View Binding
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicializar ViewModels compartidos con la actividad
        postViewModel = new ViewModelProvider(requireActivity()).get(PostViewModel.class);
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        // Configurar la Toolbar como ActionBar
        ((AppCompatActivity) requireActivity()).setSupportActionBar(binding.tools);

        // Configurar el RecyclerView
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        postAdapter = new PostAdapter(new ArrayList<>());
        binding.recyclerView.setAdapter(postAdapter);

        // Configurar el FloatingActionButton para crear nuevas publicaciones
        binding.fab.setOnClickListener(v -> {
            Log.d("HomeFragment", "FAB clicked");
            Intent intent = new Intent(getContext(), PostActivity.class);
            startActivity(intent);
        });

        // Resetear filtros solo en la primera carga
        if (isFirstLoad) {
            postViewModel.resetFilters();
            isFirstLoad = false;
        }

        // Cargar las publicaciones iniciales
        cargarPosts();

        // Configurar el menú de la Toolbar
        setupMenu();
    }

    @Override
    public void onResume() {
        super.onResume();

        // Limpiar el adaptador para evitar datos obsoletos
        if (postAdapter != null) {
            postAdapter.clearPosts();
        }

        // Resetear filtros solo si no hay fragmentos en la pila (venir de otra actividad)
        if (requireActivity().getSupportFragmentManager().getBackStackEntryCount() == 0) {
            postViewModel.resetFilters();
        }

        // Recargar publicaciones
        cargarPosts();

        // Asegurar que el FAB esté visible
        binding.fab.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Liberar el binding para evitar memory leaks
    }

    /**
     * Carga las publicaciones desde el ViewModel y observa los cambios.
     */
    private void cargarPosts() {
        // Mostrar el indicador de progreso si la actividad lo soporta
        if (getActivity() instanceof HomeActivity) {
            ((HomeActivity) requireActivity()).showProgressBar();
        }

        postViewModel.getPosts().observe(getViewLifecycleOwner(), this::updateUI);
    }

    /**
     * Actualiza la interfaz de usuario con la lista de publicaciones.
     *
     * @param posts Lista de publicaciones a mostrar
     */
    private void updateUI(List<Post> posts) {
        // Ocultar el indicador de progreso
        if (getActivity() instanceof HomeActivity) {
            ((HomeActivity) requireActivity()).hideProgressBar();
        }

        // Manejar caso de lista nula
        if (posts == null) {
            posts = new ArrayList<>();
        }

        Log.d("HomeFragment", "Número de posts: " + posts.size());

        // Actualizar el adaptador con las publicaciones
        postAdapter.setPosts(posts);

        // Mostrar u ocultar la vista vacía según corresponda
        if (posts.isEmpty() && postViewModel.isFiltered()) {
            binding.recyclerView.setVisibility(View.GONE);
            binding.emptyView.setVisibility(View.VISIBLE);
        } else {
            binding.recyclerView.setVisibility(View.VISIBLE);
            binding.emptyView.setVisibility(View.GONE);
        }

        // Asegurar que el FAB esté visible
        binding.fab.setVisibility(View.VISIBLE);
    }

    /**
     * Configura el menú de la Toolbar con las opciones disponibles.
     */
    private void setupMenu() {
        requireActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.main_menu, menu);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.itemLogout) {
                    onLogout();
                    return true;
                }
                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
    }

    /**
     * Realiza el cierre de sesión del usuario y redirige a la pantalla de inicio.
     */
    private void onLogout() {
        authViewModel.logout().observe(getViewLifecycleOwner(), logoutResult -> {
            if (logoutResult != null && logoutResult) {
                Intent intent = new Intent(getContext(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } else {
                Toast.makeText(getContext(), "Error al cerrar sesión. Intenta nuevamente.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}