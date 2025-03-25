package com.example.moviltpi.features.posts;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.moviltpi.R;
import com.example.moviltpi.core.models.Post;
import com.example.moviltpi.core.utils.ImageAdapter;
import com.example.moviltpi.core.utils.ImageUtils;
import com.example.moviltpi.core.utils.Validaciones;
import com.example.moviltpi.databinding.ActivityPostBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Actividad para crear y publicar un nuevo post.
 * Permite al usuario seleccionar imágenes, ingresar detalles del post y publicarlo.
 * Después de publicar exitosamente, navega a HomeActivity.
 */
public class PostActivity extends AppCompatActivity {
    private static final int MAX_IMAGES = 3;
    private static final int REQUEST_IMAGE = 1;
    private static final String TAG = "PostActivity";
    private ActivityPostBinding binding;

    private PostViewModel postViewModel;
    private final List<String> imagenesUrls = new ArrayList<>();
    private ImageAdapter adapter;
    private String categoria;

    private ActivityResultLauncher<Intent> galleryLauncher;
    private final AtomicBoolean isNavigating = new AtomicBoolean(false);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPostBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setupRecyclerView();
        setupViewModels();
        setupCategorySpinner();
        setupGalleryLauncher();
        binding.btnPublicar.setOnClickListener(v -> publicarPost());
    }

    /**
     * Configura el RecyclerView para mostrar las imágenes seleccionadas.
     */
    private void setupRecyclerView() {
        adapter = new ImageAdapter(imagenesUrls, this);
        binding.recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        binding.recyclerView.setAdapter(adapter);
        updateRecyclerViewVisibility();
    }

    /**
     * Configura el ViewModel y observa los resultados de la publicación.
     */
    private void setupViewModels() {
        postViewModel = new ViewModelProvider(this).get(PostViewModel.class);

        // Observar mensajes de éxito del ViewModel
        postViewModel.getPostSuccess().observe(this, mensaje -> {
            Log.d(TAG, "Mensaje de éxito recibido: " + mensaje);
            if (mensaje != null && mensaje.equals("Post publicado")) {
                Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
                navigateToHome();
            }
        });
    }

    /**
     * Configura el Spinner para seleccionar la categoría del post.
     */
    private void setupCategorySpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, getResources().getStringArray(R.array.categorias_array));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerCategoria.setAdapter(adapter);
        binding.spinnerCategoria.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                categoria = (String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                categoria = null;
            }
        });
    }

    /**
     * Configura el launcher para abrir la galería y seleccionar imágenes.
     */
    @SuppressLint("NotifyDataSetChanged")
    private void setupGalleryLauncher() {
        galleryLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                Uri imageUri = result.getData().getData();
                if (imageUri != null && imagenesUrls.size() < MAX_IMAGES) {
                    ImageUtils.subirImagenAParse(PostActivity.this, imageUri, new ImageUtils.ImageUploadCallback() {
                        @Override
                        public void onSuccess(String imageUrl) {
                            Log.d(TAG, "Imagen subida con éxito: " + imageUrl);
                            imagenesUrls.add(imageUrl);
                            adapter.notifyDataSetChanged();
                            updateRecyclerViewVisibility();
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Log.e(TAG, "Error al subir la imagen", e);
                            Toast.makeText(PostActivity.this, "Error al subir la imagen", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else if (imagenesUrls.size() >= MAX_IMAGES) {
                    Toast.makeText(PostActivity.this, "Máximo de imágenes alcanzado", Toast.LENGTH_SHORT).show();
                }
            }
        });
        binding.uploadImage.setOnClickListener(v -> ImageUtils.pedirPermisos(PostActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_IMAGE));
    }

    /**
     * Valida y publica el post con los datos ingresados por el usuario.
     * Si la publicación es exitosa, navega a HomeActivity.
     */
    private void publicarPost() {
        String titulo = binding.itTitulo.getText().toString().trim();
        String descripcion = binding.etDescripcion.getText().toString().trim();
        String duracionStr = binding.etDuracion.getText().toString().trim();
        String presupuestoStr = binding.etPresupuesto.getText().toString().trim();

        // Validaciones
        if (titulo.isEmpty()) {
            binding.itTitulo.setError("El título es obligatorio");
            return;
        }
        if (!Validaciones.validarTexto(titulo)) {
            binding.itTitulo.setError("El título no es válido");
            return;
        }
        if (descripcion.isEmpty()) {
            binding.etDescripcion.setError("La descripción es obligatoria");
            return;
        }
        if (!Validaciones.validarTexto(descripcion)) {
            binding.etDescripcion.setError("La descripción no es válida");
            return;
        }
        int duracion = Validaciones.validarNumero(duracionStr);
        if (duracionStr.isEmpty() || duracion == -1) {
            binding.etDuracion.setError("Duración no válida");
            return;
        }
        double presupuesto;
        try {
            if (presupuestoStr.isEmpty()) {
                binding.etPresupuesto.setError("El presupuesto es obligatorio");
                return;
            }
            presupuesto = Double.parseDouble(presupuestoStr);
        } catch (NumberFormatException e) {
            binding.etPresupuesto.setError("Presupuesto no válido");
            return;
        }
        if (categoria == null) {
            Toast.makeText(this, "Por favor, selecciona una categoría", Toast.LENGTH_SHORT).show();
            return;
        }

        // Crear el objeto Post
        Post post = new Post();
        post.setTitulo(titulo);
        post.setDescripcion(descripcion);
        post.setDuracion(duracion);
        post.setCategoria(categoria);
        post.setPresupuesto(presupuesto);
        post.setImagenes(new ArrayList<>(imagenesUrls));

        // Mostrar un mensaje de carga
        binding.btnPublicar.setEnabled(false);
        binding.btnPublicar.setText("Publicando...");

        // Publicar el post
        postViewModel.publicar(post);

        // Establecer un tiempo máximo de espera para la navegación
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (!isNavigating.get() && !isFinishing()) {
                Log.d(TAG, "Tiempo de espera agotado, navegando a HomeActivity");
                binding.btnPublicar.setEnabled(true);
                binding.btnPublicar.setText("Publicar");
                Toast.makeText(PostActivity.this, "Post publicado con éxito", Toast.LENGTH_SHORT).show();
                navigateToHome();
            }
        }, 5000); // 5 segundos de tiempo máximo de espera
    }

    /**
     * Navega a la HomeActivity después de publicar un post exitosamente.
     */
    private void navigateToHome() {
        if (isNavigating.compareAndSet(false, true)) {
            Log.d(TAG, "Navegando a HomeActivity");
            Intent intent = new Intent(PostActivity.this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish(); // Cierra la actividad actual
        }
    }

    /**
     * Actualiza la visibilidad del RecyclerView y el botón de carga de imágenes.
     */
    private void updateRecyclerViewVisibility() {
        boolean hasImages = !imagenesUrls.isEmpty();
        binding.recyclerView.setVisibility(hasImages ? View.VISIBLE : View.GONE);
        binding.uploadImage.setVisibility(imagenesUrls.size() < MAX_IMAGES ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_IMAGE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Permiso concedido, abriendo galería");
            ImageUtils.openGallery(PostActivity.this, galleryLauncher);
        } else {
            Log.d(TAG, "Permiso denegado");
            Toast.makeText(this, "Permiso denegado", Toast.LENGTH_SHORT).show();
        }
    }
}