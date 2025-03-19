package com.example.moviltpi.features.posts;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.moviltpi.R;
import com.example.moviltpi.core.models.Post;
import com.example.moviltpi.databinding.FragmentHomeBinding;
import com.example.moviltpi.features.auth.AuthViewModel;
import com.example.moviltpi.features.auth.MainActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragmento que muestra la lista de publicaciones (posts) en la pantalla principal.
 * Permite al usuario crear nuevas publicaciones y cerrar sesión.
 */
public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;
    private PostViewModel postViewModel;
    private AuthViewModel authViewModel;
    private PostAdapter postAdapter;
    private boolean isFirstLoad = true;
    private LinearLayout emptyView;
    private FloatingActionButton fab;

    /**
     * Constructor vacío requerido.
     */
    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Crea una nueva instancia del fragmento HomeFragment.
     *
     * @return Una nueva instancia de HomeFragment.
     */
    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicializar ViewModels
        postViewModel = new ViewModelProvider(requireActivity()).get(PostViewModel.class);
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        // Configurar Toolbar
        ((AppCompatActivity) requireActivity()).setSupportActionBar(binding.tools);

        // Configurar RecyclerView
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        postAdapter = new PostAdapter(new ArrayList<>()); // Inicializar con lista vacía
        binding.recyclerView.setAdapter(postAdapter);

        // Obtener referencia a la vista vacía
        emptyView = binding.emptyView;

        // Configurar FAB
        fab = binding.fab;
        if (fab != null) {
            Log.d("HomeFragment", "FAB encontrado y configurado");
            fab.setVisibility(View.VISIBLE);
            fab.setOnClickListener(v -> {
                Log.d("HomeFragment", "FAB clicked");
                Intent intent = new Intent(getContext(), PostActivity.class);
                startActivity(intent);
            });
        } else {
            Log.e("HomeFragment", "FAB es null");
            // Intenta encontrar el FAB directamente desde la vista
            fab = view.findViewById(R.id.fab);
            if (fab != null) {
                Log.d("HomeFragment", "FAB encontrado directamente desde la vista");
                fab.setVisibility(View.VISIBLE);
                fab.setOnClickListener(v -> {
                    Log.d("HomeFragment", "FAB clicked");
                    Intent intent = new Intent(getContext(), PostActivity.class);
                    startActivity(intent);
                });
            } else {
                Log.e("HomeFragment", "FAB no encontrado en la vista");
            }
        }

        // Solo resetear filtros en la primera carga
        if (isFirstLoad) {
            postViewModel.resetFilters();
            isFirstLoad = false;
        }

        // Cargar posts
        cargarPosts();

        // Configurar menú
        setupMenu();
    }

    @Override
    public void onResume() {
        super.onResume();

        // Limpiar el adaptador para evitar que se muestren imágenes antiguas
        if (postAdapter != null) {
            postAdapter.clearPosts();
        }

        // Solo resetear filtros si venimos de otra actividad, no de un fragmento
        if (requireActivity().getSupportFragmentManager().getBackStackEntryCount() == 0) {
            postViewModel.resetFilters();
        }

        // Recargar posts
        cargarPosts();

        // Asegurar que el FAB esté visible
        if (fab != null) {
            fab.setVisibility(View.VISIBLE);
        } else {
            Log.e("HomeFragment", "FAB es null en onResume");
            // Intenta encontrar el FAB nuevamente
            View view = getView();
            if (view != null) {
                fab = view.findViewById(R.id.fab);
                if (fab != null) {
                    fab.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    /**
     * Carga las publicaciones desde el ViewModel y observa los cambios.
     */
    private void cargarPosts() {
        // Mostrar el indicador de progreso
        if (getActivity() instanceof HomeActivity) {
            View progressBarLayout = getActivity().findViewById(R.id.progress_layout);
            if (progressBarLayout != null) {
                progressBarLayout.setVisibility(View.VISIBLE);
            }
        }

        postViewModel.getPosts().observe(getViewLifecycleOwner(), this::updateUI);
    }

    /**
     * Actualiza la interfaz de usuario con la lista de publicaciones.
     *
     * @param posts La lista de publicaciones a mostrar.
     */
    private void updateUI(List<Post> posts) {
        // Ocultar el indicador de progreso
        if (getActivity() instanceof HomeActivity) {
            ((HomeActivity) requireActivity()).hideProgressBar();
        }

        if (posts == null) {
            posts = new ArrayList<>();
        }

        Log.d("HomeFragment", "Número de posts: " + posts.size());

        // Actualizar el adaptador
        postAdapter.setPosts(posts);

        // Mostrar vista vacía si no hay posts y hay filtros activos
        if (posts.isEmpty() && postViewModel.isFiltered()) {
            binding.recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            binding.recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }

        // Asegurar que el FAB esté visible
        if (fab != null) {
            fab.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Configura el menú de la barra de herramientas.
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
     * Realiza el cierre de sesión del usuario.
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Prevenir fugas de memoria
    }
}