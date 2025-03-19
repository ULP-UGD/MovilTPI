package com.example.moviltpi.features.posts;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moviltpi.R;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.List;

/**
 * Adaptador para RecyclerView que muestra una lista de comentarios.
 * Cada comentario muestra el texto del comentario y el nombre de usuario del autor.
 */
public class ComentarioAdapter extends RecyclerView.Adapter<ComentarioAdapter.ViewHolder> {

    private List<ParseObject> comentarios;

    /**
     * Constructor para ComentarioAdapter.
     *
     * @param comentarios La lista inicial de comentarios a mostrar.
     */
    public ComentarioAdapter(List<ParseObject> comentarios) {
        this.comentarios = comentarios;
    }

    /**
     * Actualiza la lista de comentarios y notifica al adaptador de los cambios.
     *
     * @param nuevosComentarios La nueva lista de comentarios.
     */
    @SuppressLint("NotifyDataSetChanged")
    public void setComentarios(List<ParseObject> nuevosComentarios) {
        this.comentarios = nuevosComentarios;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comentario, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ParseObject comentario = comentarios.get(position);
        holder.bind(comentario);
    }

    @Override
    public int getItemCount() {
        return comentarios != null ? comentarios.size() : 0;
    }

    /**
     * ViewHolder para los elementos del RecyclerView de comentarios.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView txtComentario;
        private final TextView txtUsuario;

        /**
         * Constructor para ViewHolder.
         *
         * @param itemView La vista del elemento del RecyclerView.
         */
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtComentario = itemView.findViewById(R.id.txtComentario);
            txtUsuario = itemView.findViewById(R.id.txtUsuario);
        }

        /**
         * Vincula los datos del comentario a la vista del elemento.
         *
         * @param comentario El objeto ParseObject que contiene los datos del comentario.
         */
        public void bind(ParseObject comentario) {
            txtComentario.setText(comentario.getString("texto"));
            ParseUser usuario = comentario.getParseUser("user");
            if (usuario != null) {
                txtUsuario.setText(usuario.getUsername());
            }
        }
    }
}