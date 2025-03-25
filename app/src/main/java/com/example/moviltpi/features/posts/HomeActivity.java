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

    private ActivityHomeBinding binding; // Objeto de binding para el layout principal
    private PostViewModel postViewModel; // ViewModel para gestionar las publicaciones
    private View progressBarLayout; // Referencia al progress_layout inflado

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inflamos el layout principal usando View Binding y asignamos a la variable de instancia
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Inicializar el ViewModel
        postViewModel = new ViewModelProvider(this).get(PostViewModel.class);

        // Inflar y añadir el progress_layout al contenedor principal
        LayoutInflater inflater = LayoutInflater.from(this);
        progressBarLayout = inflater.inflate(R.layout.progress_layout, binding.mainCont, false);
        binding.mainCont.addView(progressBarLayout);
        progressBarLayout.setVisibility(View.GONE); // Ocultar por defecto

        // Configurar la navegación inferior
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.itemHome) {
                postViewModel.resetFilters();
                postViewModel.loadPosts();
                openFragment(HomeFragment.newInstance(), true, "HOME_FRAGMENT");
            } else if (item.getItemId() == R.id.itemChats) {
                openFragment(new UserFragment(), false, "USERS_FRAGMENT");
            } else if (item.getItemId() == R.id.itemPerfil) {
                openFragment(new PerfilFragment(), false, "PERFIL_FRAGMENT");
            } else if (item.getItemId() == R.id.itemFiltros) {
                openFragment(new FiltrosFragment(), false, "FILTROS_FRAGMENT");
            }
            return true;
        });

        // Cargar el HomeFragment por defecto si no hay estado guardado
        if (savedInstanceState == null) {
            openFragment(HomeFragment.newInstance(), true, "HOME_FRAGMENT");
            //binding.bottomNavigation.setSelectedItemId(R.id.itemHome); // Seleccionar el itemHome inicialmente
        }
    }

    /**
     * Abre un fragmento en el contenedor principal y gestiona el back stack.
     *
     * @param fragment El fragmento a abrir
     * @param isHome   Indica si el fragmento es el HomeFragment (limpia el back stack)
     * @param tag      Etiqueta única para identificar el fragmento
     */
    private void openFragment(Fragment fragment, boolean isHome, String tag) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment existingFragment = fragmentManager.findFragmentByTag(tag);

        // Si el fragmento ya existe y está visible, no hacer nada
        if (existingFragment != null && existingFragment.isVisible()) {
            return;
        }

        FragmentTransaction transaction = fragmentManager.beginTransaction();

        // Limpiar el back stack si es el HomeFragment
        if (isHome) {
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }

        // Reemplazar el fragmento actual
        transaction.replace(binding.container.getId(), fragment, tag);

        // Añadir al back stack solo si no es el HomeFragment
        if (!isHome) {
            transaction.addToBackStack(tag);
        }

        transaction.commit();
    }

    /**
     * Muestra la barra de progreso en la interfaz.
     */
    public void showProgressBar() {
        if (progressBarLayout != null) {
            progressBarLayout.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Oculta la barra de progreso en la interfaz.
     */
    public void hideProgressBar() {
        if (progressBarLayout != null) {
            progressBarLayout.setVisibility(View.GONE);
        }
    }

    /**
     * Maneja el botón de retroceso para navegar al HomeFragment o salir de la aplicación.
     */
    @Override
    public void onBackPressed() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment currentFragment = fragmentManager.findFragmentById(binding.container.getId());

        if (fragmentManager.getBackStackEntryCount() > 0) {
            // Retroceder en el back stack si hay fragmentos apilados
            fragmentManager.popBackStack();
        } else if (!(currentFragment instanceof HomeFragment)) {
            // Si no estamos en Home, resetear filtros y cargar HomeFragment
            postViewModel.resetFilters();
            postViewModel.loadPosts();
            openFragment(HomeFragment.newInstance(), true, "HOME_FRAGMENT");
        } else {
            // Si estamos en Home, salir de la aplicación
            super.onBackPressed();
        }
    }

    public void openHomeFragment() {
        postViewModel.loadPosts(); // Cargar los posts con los filtros aplicados
        openFragment(HomeFragment.newInstance(), true, "HOME_FRAGMENT");
    }
}