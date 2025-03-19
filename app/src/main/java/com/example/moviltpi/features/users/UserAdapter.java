package com.example.moviltpi.features.users;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.moviltpi.R;
import com.parse.ParseUser;
import java.util.List;

/**
 * Adaptador para mostrar una lista de usuarios en un RecyclerView.
 * Maneja la vinculación de datos de usuarios ParseUser a vistas individuales.
 */
public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private final List<ParseUser> users;  // Lista de usuarios a mostrar
    private final OnUserClickListener listener;  // Listener para manejar clics en usuarios

    /**
     * Interfaz para manejar eventos de clic en usuarios.
     */
    public interface OnUserClickListener {
        /**
         * Se llama cuando se hace clic en un usuario.
         *
         * @param user El usuario en el que se hizo clic
         */
        void onUserClick(ParseUser user);
    }

    /**
     * Constructor del adaptador.
     *
     * @param users    Lista de usuarios a mostrar
     * @param listener Listener para manejar clics en usuarios
     */
    public UserAdapter(List<ParseUser> users, OnUserClickListener listener) {
        this.users = users;
        this.listener = listener;
        Log.d("UsersAdapter", "Adapter creado con " + (users != null ? users.size() : 0) + " usuarios");
    }

    /**
     * Crea un nuevo ViewHolder para un elemento de usuario.
     *
     * @param parent   El ViewGroup padre
     * @param viewType El tipo de vista (no usado en esta implementación)
     * @return Un nuevo UserViewHolder
     */
    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflar el layout del item de usuario
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    /**
     * Vincula los datos de un usuario a un ViewHolder específico.
     *
     * @param holder   El ViewHolder a vincular
     * @param position La posición del usuario en la lista
     */
    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        ParseUser user = users.get(position);
        Log.d("UsersAdapter", "Binding usuario en posición " + position + ": " + user.getUsername());
        holder.bind(user);  // Vincular los datos del usuario al ViewHolder
    }

    /**
     * Obtiene el número total de elementos en la lista.
     *
     * @return El tamaño de la lista de usuarios, o 0 si es nula
     */
    @Override
    public int getItemCount() {
        return users != null ? users.size() : 0;
    }

    /**
     * Clase interna que representa un ViewHolder para un usuario.
     */
    class UserViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvUsername;  // TextView para mostrar el nombre de usuario

        /**
         * Constructor del ViewHolder.
         *
         * @param itemView La vista del elemento individual
         */
        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tvUsername);  // Encontrar el TextView en el layout
        }

        /**
         * Vincula los datos de un usuario a la vista.
         *
         * @param user El usuario cuyos datos se mostrarán
         */
        void bind(ParseUser user) {
            String username = user.getUsername();
            // Mostrar el nombre de usuario o un texto por defecto si es nulo
            tvUsername.setText(username != null ? username : "Usuario sin nombre");

            // Configurar el listener de clic para el elemento completo
            itemView.setOnClickListener(v -> {
                Log.d("UsersAdapter", "Click en usuario: " + user.getUsername());
                if (listener != null) {
                    listener.onUserClick(user);  // Notificar al listener del clic
                }
            });
        }
    }
}