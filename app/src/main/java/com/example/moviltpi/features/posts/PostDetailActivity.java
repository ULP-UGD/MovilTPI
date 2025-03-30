package com.example.moviltpi.features.posts;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.moviltpi.R;
import com.example.moviltpi.core.utils.EfectoTransformer;
import com.example.moviltpi.core.utils.ImageSliderAdapter;
import com.example.moviltpi.databinding.ActivityPostDetailBinding;
import com.google.android.material.tabs.TabLayoutMediator;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Actividad que muestra los detalles de una publicación (post).
 * Permite visualizar la información del post, las imágenes, los comentarios y permite al usuario comentar o eliminar el post.
 */
public class PostDetailActivity extends AppCompatActivity {
    private ActivityPostDetailBinding binding;
    private PostDetailViewModel postDetailViewModel;
    private ComentarioAdapter comentarioAdapter;
    private String postId;
    private final AtomicBoolean isNavigating = new AtomicBoolean(false);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        postDetailViewModel = new ViewModelProvider(this).get(PostDetailViewModel.class);

        binding = ActivityPostDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        postId = getIntent().getStringExtra("idPost");
        if (postId != null) {
            postDetailViewModel.fetchCommentario(postId);
        }

        binding.recyclerComentarios.setLayoutManager(new LinearLayoutManager(this));
        comentarioAdapter = new ComentarioAdapter(new ArrayList<>());
        binding.recyclerComentarios.setAdapter(comentarioAdapter);

        // Observando los comentarios
        postDetailViewModel.getCommentsLiveData().observe(this, comentarios -> {
            comentarioAdapter.setComentarios(comentarios);
            comentarioAdapter.notifyDataSetChanged();
        });

        String currentUser = ParseUser.getCurrentUser().getUsername();
        String perfilUserId = getIntent().getStringExtra("username");

        if (currentUser != null && currentUser.equals(perfilUserId)) {
            binding.btnEliminarPost.setVisibility(View.VISIBLE);
            binding.btnEliminarPost.setOnClickListener(v -> confirmaBorrar());
        } else {
            binding.btnEliminarPost.setVisibility(View.GONE);
        }

        detailInfo();
        setupObservers();

        binding.fabComentar.setOnClickListener(v -> comentar());
    }

    /**
     * Muestra un diálogo de confirmación para eliminar el post.
     */
    private void confirmaBorrar() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Confirmación");
        alert.setMessage("¿Estás seguro de que deseas eliminar este post?");

        alert.setPositiveButton("Eliminar", (dialog, which) -> {
            // Cambiar el texto del botón y deshabilitarlo mientras se elimina
            binding.btnEliminarPost.setText("Eliminando...");
            binding.btnEliminarPost.setEnabled(false);

            postDetailViewModel.eliminarPost(postId);

            // Observe the success LiveData for deletion result
            postDetailViewModel.getSuccessLiveData().observe(this, message -> {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            });

            // Establecer un tiempo máximo de espera para la navegación (por si acaso)
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (!isNavigating.get() && !isFinishing()) {
                    binding.btnEliminarPost.setText("Eliminar");
                    binding.btnEliminarPost.setEnabled(true);
                }
                Intent intent = new Intent(this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }, 5000); // 5 segundos de tiempo máximo de espera
        });

        alert.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());
        alert.show();
    }

    /**
     * Muestra un diálogo para que el usuario ingrese un comentario.
     */
    private void comentar() {
        AlertDialog.Builder alert = new AlertDialog.Builder(PostDetailActivity.this);
        alert.setTitle("¡COMENTARIO!");
        alert.setMessage("Ingresa tu comentario: ");

        EditText editText = new EditText(PostDetailActivity.this);
        editText.setHint("Texto");

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        editText.setLayoutParams(params);

        params.setMargins(36, 0, 36, 36);

        RelativeLayout container = new RelativeLayout(PostDetailActivity.this);
        RelativeLayout.LayoutParams relativeParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        container.setLayoutParams(relativeParams);
        container.addView(editText);
        alert.setView(container);

        alert.setPositiveButton("Ok", (dialog, which) -> {
            String value = editText.getText().toString().trim();
            if (!value.isEmpty()) {
                postDetailViewModel.grabaComentario(postId, value);
            } else {
                Toast.makeText(PostDetailActivity.this, "El comentario no puede estar vacío", Toast.LENGTH_SHORT).show();
            }
        });

        alert.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());

        alert.show();
    }

    /**
     * Configura los observadores para los LiveData del ViewModel.
     */
    private void setupObservers() {
        postDetailViewModel.getCommentsLiveData().observe(this, comments -> {
            // updateUI(comments);
        });

        postDetailViewModel.getErrorLiveData().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        isNavigating.set(false); // Restablecer la variable al detener la actividad
    }

    /**
     * Llena la información del post en la interfaz de usuario.
     */
    private void detailInfo() {
        binding.nameUser.setText(getIntent().getStringExtra("username"));
        binding.emailUser.setText(getIntent().getStringExtra("email"));
        binding.insta.setText(getIntent().getStringExtra("redsocial"));

        String fotoUrl = getIntent().getStringExtra("foto_perfil");
        if (fotoUrl != null) {
            Picasso.get().load(fotoUrl).placeholder(R.drawable.ic_person).error(R.drawable.ic_person).into(binding.circleImageView);
        } else {
            binding.circleImageView.setImageResource(R.drawable.ic_person);
        }

        ArrayList<String> urls = getIntent().getStringArrayListExtra("imagenes");
        String titulo = "Lugar: " + getIntent().getStringExtra("titulo");
        binding.lugar.setText(titulo);
        String categoria = "Categoria: " + getIntent().getStringExtra("categoria");
        binding.categoria.setText(categoria);
        String comentario = "descripción: " + getIntent().getStringExtra("descripcion");
        binding.description.setText(comentario);
        String duracion = "Duración: " + getIntent().getIntExtra("duracion", 0) + " día/s";
        binding.duracion.setText(duracion);
        String presupuesto = "Presupuesto: U$ " + getIntent().getDoubleExtra("presupuesto", 0.0);
        binding.presupuesto.setText(presupuesto);

        if (urls != null && !urls.isEmpty()) {
            ImageSliderAdapter imageSliderAdapter = new ImageSliderAdapter(urls);
            binding.viewPager.setAdapter(imageSliderAdapter);
            binding.viewPager.setPageTransformer(new EfectoTransformer());

            // Conexión TabLayout con ViewPager2
            new TabLayoutMediator(binding.tabLayout, binding.viewPager, (tab, position) -> {
            }).attach();
        }
    }
}