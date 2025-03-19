package com.example.moviltpi.features.posts;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.example.moviltpi.R;
import com.example.moviltpi.databinding.ActivityHomeBinding;
import com.example.moviltpi.features.users.PerfilFragment;
import com.example.moviltpi.features.users.UserFragment;

/**
 * Actividad principal que gestiona la navegación entre los diferentes fragmentos de la aplicación.
 * Utiliza un NavigationBarView para la navegación inferior y un ViewModel para la gestión de datos.
 */
public class HomeActivity extends AppCompatActivity {
    private PostViewModel postViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        com.example.moviltpi.databinding.ActivityHomeBinding binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Inicializar ViewModel
        postViewModel = new ViewModelProvider(this).get(PostViewModel.class);

        // Añadir barra de progreso
        LayoutInflater inflater = LayoutInflater.from(this);
        View progressBarLayout = inflater.inflate(R.layout.progress_layout, binding.mainCont, false);
        binding.mainCont.addView(progressBarLayout);

        // Configurar la navegación inferior
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.itemHome) {
                // Resetear filtros y cargar posts solo cuando se selecciona explícitamente el Home
                postViewModel.resetFilters();
                postViewModel.loadPosts();
                openFragment(HomeFragment.newInstance(), true, "HOME_FRAGMENT");
            } else if (item.getItemId() == R.id.itemChats) {
                // Abrir UserFragment en lugar de ChatsFragment
                openFragment(new UserFragment(), false, "USERS_FRAGMENT");
            } else if (item.getItemId() == R.id.itemPerfil) {
                openFragment(new PerfilFragment(), false, "PERFIL_FRAGMENT");
            } else if (item.getItemId() == R.id.itemFiltros) {
                openFragment(new FiltrosFragment(), false, "FILTROS_FRAGMENT");
            }
            return true;
        });

        // Cargar el fragmento Home por defecto si no hay estado guardado
        if (savedInstanceState == null) {
            openFragment(HomeFragment.newInstance(), true, "HOME_FRAGMENT");
        }
    }

    /**
     * Abre un fragmento en el contenedor principal.
     *
     * @param fragment El fragmento a abrir.
     * @param isHome   Indica si el fragmento es el HomeFragment.
     * @param tag      El tag del fragmento.
     */
    private void openFragment(Fragment fragment, boolean isHome, String tag) {
        FragmentManager fragmentManager = getSupportFragmentManager();

        // Limpiar el back stack si estamos abriendo el HomeFragment
        if (isHome) {
            int backStackCount = fragmentManager.getBackStackEntryCount();
            for (int i = 0; i < backStackCount; i++) {
                fragmentManager.popBackStack();
            }
        }

        // Verificar si el fragmento ya existe
        Fragment existingFragment = fragmentManager.findFragmentByTag(tag);
        if (existingFragment == null) {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.container, fragment, tag);

            // Solo añadir al back stack si no es HomeFragment
            if (!isHome) {
                fragmentTransaction.addToBackStack(null);
            }

            fragmentTransaction.commit();
        } else {
            // Si el fragmento ya existe, simplemente mostrarlo
            fragmentManager.beginTransaction().show(existingFragment).commit();
        }
    }

    /**
     * Oculta la barra de progreso.
     */
    public void hideProgressBar() {
        View progressBarLayout = findViewById(R.id.progress_layout);
        if (progressBarLayout != null) {
            progressBarLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void onBackPressed() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.container);
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else if (!(currentFragment instanceof HomeFragment)) {
            // Resetear filtros y cargar posts solo cuando volvemos explícitamente al Home
            postViewModel.resetFilters();
            postViewModel.loadPosts();
            openFragment(HomeFragment.newInstance(), true, "HOME_FRAGMENT");
        } else {
            super.onBackPressed();
        }
    }
}