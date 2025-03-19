package com.example.moviltpi.features.posts;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.example.moviltpi.R;

/**
 * Fragmento que permite al usuario aplicar filtros a la lista de publicaciones.
 * Los filtros incluyen la categoría de la publicación y el orden de visualización.
 */
public class FiltrosFragment extends Fragment {
    private Spinner spinnerCategoria;
    private Spinner spinnerOrden;
    private PostViewModel postViewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_filtros, container, false);

        spinnerCategoria = view.findViewById(R.id.spinnerCategoria);
        spinnerOrden = view.findViewById(R.id.spinnerOrden);
        Button btnAplicar = view.findViewById(R.id.btnAplicar);

        // Inicializa el ViewModel para la gestión de publicaciones.
        postViewModel = new ViewModelProvider(requireActivity()).get(PostViewModel.class);

        // Configura los Spinners con las opciones de filtro.
        configurarSpinners();

        // Configura el listener del botón "Aplicar" para aplicar los filtros seleccionados.
        btnAplicar.setOnClickListener(v -> aplicarFiltros());

        return view;
    }

    /**
     * Configura los Spinners con las opciones de categoría y orden de visualización.
     */
    private void configurarSpinners() {
        // Configura el Spinner de categorías con las opciones del array de recursos.
        ArrayAdapter<CharSequence> categoriaAdapter = ArrayAdapter.createFromResource(requireContext(), R.array.categorias_array, android.R.layout.simple_spinner_item);
        categoriaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategoria.setAdapter(categoriaAdapter);

        // Configura el Spinner de orden con las opciones del array de recursos.
        ArrayAdapter<CharSequence> ordenAdapter = ArrayAdapter.createFromResource(requireContext(), R.array.orden_array, android.R.layout.simple_spinner_item);
        ordenAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerOrden.setAdapter(ordenAdapter);
    }

    /**
     * Aplica los filtros seleccionados por el usuario y actualiza la lista de publicaciones.
     */
    private void aplicarFiltros() {
        String categoria = spinnerCategoria.getSelectedItem().toString();
        String orden = spinnerOrden.getSelectedItem().toString();

        Log.d("FiltrosFragment", "Aplicando filtros: Categoría=" + categoria + ", Orden=" + orden);

        // Llama al método del ViewModel para aplicar los filtros y cargar las publicaciones filtradas.
        postViewModel.aplicarFiltros(categoria, orden);

        // Cierra el fragmento de filtros y vuelve a la lista de publicaciones.
        requireActivity().getSupportFragmentManager().popBackStack();
    }
}