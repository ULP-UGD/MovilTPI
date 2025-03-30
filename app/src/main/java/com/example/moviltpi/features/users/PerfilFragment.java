package com.example.moviltpi.features.users;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.example.moviltpi.R;
import com.example.moviltpi.core.utils.ImageUtils;
import com.example.moviltpi.databinding.FragmentPerfilBinding;
import com.example.moviltpi.features.posts.HomeActivity;
import com.example.moviltpi.features.posts.PostAdapter;
import com.example.moviltpi.features.posts.PostViewModel;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;
import java.io.IOException;

/**
 * Fragmento que muestra el perfil del usuario y sus posts.
 * Permite actualizar la foto de perfil y cerrar sesión.
 */
public class PerfilFragment extends Fragment {
    private FragmentPerfilBinding binding;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private PostViewModel postViewModel;

    /** Constructor vacío requerido por Fragment */
    public PerfilFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPerfilBinding.inflate(inflater, container, false);
        postViewModel = new ViewModelProvider(this).get(PostViewModel.class);
        setupMenu();           // Configurar el menú
        setupToolbar();        // Configurar la barra de herramientas
        displayUserInfo();     // Mostrar información del usuario
        setupGalleryLauncher(); // Configurar el lanzador de galería
        setupProfileImageClick(); // Configurar el clic en la imagen de perfil
        setupViewModel();      // Configurar el ViewModel y RecyclerView
        return binding.getRoot();
    }

    /**
     * Configura el ViewModel y el RecyclerView para mostrar los posts del usuario.
     */
    @SuppressLint("NotifyDataSetChanged")
    private void setupViewModel() {
        // Configurar el layout manager del RecyclerView
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Observar los posts del usuario actual
        postViewModel.getPostsByCurrentUser().observe(getViewLifecycleOwner(), posts -> {
            if (posts != null && !posts.isEmpty()) {
                Log.d("PerfilFragment", "Número de posts: " + posts.size());
                PostAdapter adapter = new PostAdapter(posts);
                binding.recyclerView.setAdapter(adapter);

                // Actualizar contador de publicaciones
                if (binding.cantPost != null) {
                    binding.cantPost.setText(String.valueOf(posts.size()));
                }

                if (getActivity() instanceof HomeActivity) {
                    ((HomeActivity) requireActivity()).hideProgressBar();
                }
            } else {
                Log.d("PerfilFragment", "No hay posts disponibles.");

                // Actualizar contador de publicaciones a cero
                if (binding.cantPost != null) {
                    binding.cantPost.setText("0");
                }

                if (getActivity() instanceof HomeActivity) {
                    ((HomeActivity) requireActivity()).hideProgressBar();
                }
            }
        });
    }


    /**
     * Configura el menú con la opción de cerrar sesión.
     */
    private void setupMenu() {
        requireActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.close_menu, menu);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.itemClose) {
                    Toast.makeText(requireContext(), "Cerrar sesión", Toast.LENGTH_SHORT).show();
                    return true;
                }
                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
    }

    /**
     * Configura la barra de herramientas del fragmento.
     */
    private void setupToolbar() {
        ((AppCompatActivity) requireActivity()).setSupportActionBar(binding.getRoot().findViewById(R.id.tools_filtro));
    }

    /**
     * Muestra la información del usuario actual en la UI.
     */
    private void displayUserInfo() {
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null) {
            binding.nameUser.setText(currentUser.getUsername());
            binding.emailUser.setText(currentUser.getEmail());
            binding.insta.setText(currentUser.getString("instagram"));

            // Cargar la foto de perfil si existe
            String fotoUrl = currentUser.getString("foto_perfil");
            if (fotoUrl != null) {
                Picasso.get().load(fotoUrl)
                        .placeholder(R.drawable.ic_person)
                        .error(R.drawable.ic_person)
                        .into(binding.circleImageView);
            } else {
                binding.circleImageView.setImageResource(R.drawable.ic_person);
            }
        } else {
            Toast.makeText(getContext(), "Usuario no logueado", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Configura el lanzador para seleccionar imágenes de la galería.
     */
    private void setupGalleryLauncher() {
        galleryLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                Uri imageUri = result.getData().getData();
                if (imageUri != null) {
                    handleImageSelection(imageUri);  // Procesar la imagen seleccionada
                }
            }
        });
    }

    /**
     * Configura el evento de clic en la imagen de perfil para abrir la galería.
     */
    private void setupProfileImageClick() {
        binding.circleImageView.setOnClickListener(v -> {
            // Solicitar permisos si es necesario (Android 6.0+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ImageUtils.pedirPermisos(requireActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }
            ImageUtils.openGallery(requireContext(), galleryLauncher);
        });
    }

    /**
     * Maneja la selección de una imagen de la galería y la sube a Parse.
     *
     * @param imageUri URI de la imagen seleccionada
     */
    private void handleImageSelection(Uri imageUri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), imageUri);
            binding.circleImageView.setImageBitmap(bitmap);

            // Subir la imagen a Parse
            ImageUtils.subirImagenAParse(requireContext(), imageUri, new ImageUtils.ImageUploadCallback() {
                @Override
                public void onSuccess(String imageUrl) {
                    ParseUser currentUser = ParseUser.getCurrentUser();
                    if (currentUser != null) {
                        currentUser.put("foto_perfil", imageUrl);
                        currentUser.saveInBackground(e -> {
                            if (e == null) {
                                Toast.makeText(requireContext(), "Foto subida correctamente", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(requireContext(), "Error al guardar la URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(requireContext(), "Error al subir la foto: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (IOException e) {
            Log.e("PerfilFragment", "Error al manejar la imagen: " + e.getMessage(), e);
            Toast.makeText(requireContext(), "Error al cargar la imagen", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Limpia las referencias al destruir la vista para evitar fugas de memoria.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}