package com.example.moviltpi.features.posts;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moviltpi.R;
import com.example.moviltpi.core.models.Post;
import com.example.moviltpi.core.models.User;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Adaptador para RecyclerView que muestra una lista de publicaciones (posts).
 * Permite visualizar el título, descripción e imágenes de cada post, y navegar al detalle del post.
 */
public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private List<Post> posts;

    /**
     * Constructor para PostAdapter.
     *
     * @param posts La lista inicial de posts a mostrar.
     */
    public PostAdapter(List<Post> posts) {
        this.posts = posts != null ? posts : new ArrayList<>();
    }

    /**
     * Actualiza la lista de posts y notifica al adaptador de los cambios.
     *
     * @param posts La nueva lista de posts.
     */
    @SuppressLint("NotifyDataSetChanged")
    public void setPosts(List<Post> posts) {
        this.posts = posts != null ? posts : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = posts.get(position);
        holder.tvTitulo.setText(post.getTitulo());
        holder.tvDescripcion.setText(post.getDescripcion());

        // Limpiar imágenes anteriores
        holder.ivImage1.setVisibility(View.GONE);
        holder.ivImage1.setImageResource(0);
        holder.ivImage2.setVisibility(View.GONE);
        holder.ivImage2.setImageResource(0);
        holder.ivImage3.setVisibility(View.GONE);
        holder.ivImage3.setImageResource(0);

        if (post.getImagenes() != null) {
            if (!post.getImagenes().isEmpty()) {
                Picasso.get().load(post.getImagenes().get(0)).placeholder(R.drawable.uploadimg).into(holder.ivImage1);
                holder.ivImage1.setVisibility(View.VISIBLE);
            }

            if (post.getImagenes().size() > 1) {
                Picasso.get().load(post.getImagenes().get(1)).placeholder(R.drawable.uploadimg).into(holder.ivImage2);
                holder.ivImage2.setVisibility(View.VISIBLE);
            }

            if (post.getImagenes().size() > 2) {
                Picasso.get().load(post.getImagenes().get(2)).placeholder(R.drawable.uploadimg).into(holder.ivImage3);
                holder.ivImage3.setVisibility(View.VISIBLE);
            }
        }

        holder.itemView.setOnClickListener(v -> {
            Context context = holder.itemView.getContext();
            PostProvider postProvider = new PostProvider();

            LiveData<Post> postDetailLiveData = postProvider.getPostDetail(post.getId());
            postDetailLiveData.observe((LifecycleOwner) context, postDetail -> {
                if (postDetail != null) {
                    Intent intent = new Intent(context, PostDetailActivity.class);

                    // Datos del Post
                    intent.putExtra("idPost", post.getId());
                    intent.putExtra("titulo", postDetail.getTitulo());
                    intent.putExtra("descripcion", postDetail.getDescripcion());
                    intent.putExtra("categoria", postDetail.getCategoria());
                    intent.putExtra("duracion", postDetail.getDuracion());
                    intent.putExtra("presupuesto", postDetail.getPresupuesto());

                    // Datos del Usuario
                    User user = postDetail.getUser();
                    if (user != null) {
                        Log.d("Postadapter", user.getUsername());
                        intent.putExtra("username", user.getUsername());
                        intent.putExtra("email", user.getEmail());
                        intent.putExtra("redsocial", user.getRedSocial());
                        intent.putExtra("foto_perfil", user.getString("foto_perfil"));
                    } else {
                        Log.d("Postadapter", "User is null");
                    }

                    // Lista de imágenes
                    ArrayList<String> imageUrls = new ArrayList<>(postDetail.getImagenes());
                    intent.putStringArrayListExtra("imagenes", imageUrls);

                    // Lanza la actividad
                    context.startActivity(intent);
                } else {
                    Log.e("PostDetail", "No se pudo obtener el detalle del post.");
                }
            });
        });
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    /**
     * ViewHolder para los elementos del RecyclerView de posts.
     */
    public static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitulo, tvDescripcion;
        ImageView ivImage1, ivImage2, ivImage3;

        /**
         * Constructor para PostViewHolder.
         *
         * @param itemView La vista del elemento del RecyclerView.
         */
        public PostViewHolder(View itemView) {
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.tvTitulo);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcion);
            ivImage1 = itemView.findViewById(R.id.ivImage1);
            ivImage2 = itemView.findViewById(R.id.ivImage2);
            ivImage3 = itemView.findViewById(R.id.ivImage3);
        }
    }

    /**
     * Actualiza la lista de posts y notifica al adaptador de los cambios.
     *
     * @param newPosts La nueva lista de posts.
     */
    @SuppressLint("NotifyDataSetChanged")
    public void updatePosts(List<Post> newPosts) {
        if (newPosts != null) {
            this.posts.clear();
            this.posts.addAll(newPosts);
            notifyDataSetChanged();
        }
    }

    /**
     * Limpia completamente la lista de posts y notifica al adaptador.
     */
    @SuppressLint("NotifyDataSetChanged")
    public void clearPosts() {
        this.posts.clear();
        notifyDataSetChanged();
    }
}