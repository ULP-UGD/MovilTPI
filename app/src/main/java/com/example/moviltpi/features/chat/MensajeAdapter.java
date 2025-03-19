package com.example.moviltpi.features.chat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moviltpi.R;
import com.example.moviltpi.core.models.Mensaje;
import com.parse.ParseUser;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Adaptador para RecyclerView que muestra mensajes de chat.
 * Diferencia entre mensajes enviados y recibidos, y aplica DiffUtil para actualizaciones eficientes.
 */
public class MensajeAdapter extends RecyclerView.Adapter<MensajeAdapter.ViewHolder> {
    private final List<Mensaje> mensajes;
    private final ParseUser currentUser;

    /**
     * Constructor para MensajeAdapter.
     *
     * @param mensajes    Lista inicial de mensajes.
     * @param currentUser El ParseUser actual.
     */
    public MensajeAdapter(List<Mensaje> mensajes, ParseUser currentUser) {
        this.mensajes = mensajes;
        this.currentUser = currentUser;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_mensaje, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return mensajes.size();
    }

    /**
     * Actualiza la lista de mensajes utilizando DiffUtil para una actualización eficiente.
     *
     * @param newMensajes La nueva lista de mensajes.
     */
    @SuppressLint("NotifyDataSetChanged")
    public void setMensajes(List<Mensaje> newMensajes) {
        MensajeDiffCallback diffCallback = new MensajeDiffCallback(this.mensajes, newMensajes);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

        this.mensajes.clear();
        this.mensajes.addAll(newMensajes);
        diffResult.dispatchUpdatesTo(this);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Mensaje mensaje = mensajes.get(position);
        boolean esRemitente = mensaje.getRemitente().getObjectId().equals(currentUser.getObjectId());
        holder.bind(mensaje, esRemitente);
    }

    /**
     * ViewHolder para los elementos del RecyclerView de mensajes.
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvMensajeEnviado;
        private final TextView tvMensajeRecibido;
        private final TextView tvFechaEnviado;
        private final TextView tvFechaRecibido;
        private final Context context;

        /**
         * Constructor para ViewHolder.
         *
         * @param itemView La vista del elemento del RecyclerView.
         */
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMensajeEnviado = itemView.findViewById(R.id.tvMensajeEnviado);
            tvMensajeRecibido = itemView.findViewById(R.id.tvMensajeRecibido);
            tvFechaEnviado = itemView.findViewById(R.id.tvFechaEnviado);
            tvFechaRecibido = itemView.findViewById(R.id.tvFechaRecibido);
            ConstraintLayout layoutMensaje = itemView.findViewById(R.id.mensajeContainer);
            context = itemView.getContext();
        }

        /**
         * Vincula los datos del mensaje a la vista del elemento.
         *
         * @param mensaje     El mensaje a mostrar.
         * @param esRemitente Indica si el mensaje fue enviado por el usuario actual.
         */
        public void bind(Mensaje mensaje, boolean esRemitente) {
            // Ocultar todos los elementos primero
            tvMensajeEnviado.setVisibility(View.GONE);
            tvMensajeRecibido.setVisibility(View.GONE);
            tvFechaEnviado.setVisibility(View.GONE);
            tvFechaRecibido.setVisibility(View.GONE);

            // Formatear la fecha y hora
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault());
            String fechaHora = sdf.format(mensaje.getFecha());

            // Determinar si estamos en modo oscuro
            boolean isDarkMode = (context.getResources().getConfiguration().uiMode &
                    Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;

            // Elegir color de texto para fechas según el modo
            int textColor = isDarkMode ? Color.LTGRAY : Color.BLACK;

            if (esRemitente) {
                // Mensaje enviado por el usuario actual
                tvMensajeEnviado.setVisibility(View.VISIBLE);
                tvFechaEnviado.setVisibility(View.VISIBLE);
                tvMensajeEnviado.setText(mensaje.getTexto());
                tvFechaEnviado.setText(fechaHora);
                tvFechaEnviado.setTextColor(textColor);
            } else {
                // Mensaje recibido de otro usuario
                tvMensajeRecibido.setVisibility(View.VISIBLE);
                tvFechaRecibido.setVisibility(View.VISIBLE);
                tvMensajeRecibido.setText(mensaje.getTexto());
                tvFechaRecibido.setText(fechaHora);
                tvFechaRecibido.setTextColor(textColor);
            }
        }
    }
}