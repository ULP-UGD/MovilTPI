package com.example.moviltpi.features.posts;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.moviltpi.R;
import com.example.moviltpi.databinding.FragmentFiltrosBinding; // Clase generada por View Binding

/**
 * Fragmento que permite al usuario aplicar filtros a la lista de publicaciones.
 * Los filtros incluyen la categoría de la publicación y el orden de visualización.
 */
public class FiltrosFragment extends Fragment {

    private FragmentFiltrosBinding binding; // Objeto de binding para acceder a las vistas
    private PostViewModel postViewModel; // ViewModel para gestionar las publicaciones

    /**
     * Método llamado cuando se crea la vista del fragmento.
     * Inicializa los componentes de la interfaz y configura su comportamiento.
     *
     * @param inflater           Inflater para inflar la vista del layout
     * @param container          Contenedor padre donde se insertará la vista
     * @param savedInstanceState Estado previo guardado, si existe
     * @return Vista inflada del fragmento
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflamos la vista usando View Binding
        binding = FragmentFiltrosBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Inicializa el ViewModel compartido con la actividad
        postViewModel = new ViewModelProvider(requireActivity()).get(PostViewModel.class);

        // Configura los Spinners con las opciones de filtro
        configurarSpinners();

        // Configura el listener del botón "Aplicar" para procesar los filtros
        binding.btnAplicar.setOnClickListener(v -> aplicarFiltros());

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
     * Configura los Spinners con las opciones de categoría y orden de visualización.
     */
    private void configurarSpinners() {
        // Configura el Spinner de categorías con las opciones del array de recursos
        ArrayAdapter<CharSequence> categoriaAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.categorias_filtros_array,
                android.R.layout.simple_spinner_item
        );
        categoriaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerCategoria.setAdapter(categoriaAdapter);

        // Configura el Spinner de orden con las opciones del array de recursos
        ArrayAdapter<CharSequence> ordenAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.orden_array,
                android.R.layout.simple_spinner_item
        );
        ordenAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerOrden.setAdapter(ordenAdapter);
    }

    /**
     * Aplica los filtros seleccionados por el usuario y actualiza la lista de publicaciones.
     */
    private void aplicarFiltros() {
        String categoria = binding.spinnerCategoria.getSelectedItem().toString();
        String orden = binding.spinnerOrden.getSelectedItem().toString();

        // Registrar los filtros aplicados en el log para depuración
        Log.d("FiltrosFragment", "Aplicando filtros: Categoría=" + categoria + ", Orden=" + orden);

        // Llama al ViewModel para aplicar los filtros y actualizar las publicaciones
        postViewModel.aplicarFiltros(categoria, orden);

        // Navegar directamente a HomeFragment
        if (getActivity() instanceof HomeActivity) {
            ((HomeActivity) getActivity()).openHomeFragment();
        }

        // Cierra el fragmento y regresa a la pantalla anterior
        requireActivity().getSupportFragmentManager().popBackStack();
    }
}