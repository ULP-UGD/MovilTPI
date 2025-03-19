package com.example.moviltpi.core.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moviltpi.R;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Adaptador para RecyclerView que muestra una lista de imágenes cargadas desde URLs usando Picasso.
 */
public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {
    private List<String> imageUrls;
    private final Context context;

    /**
     * Constructor del adaptador.
     *
     * @param imageUrls Lista de URLs de las imágenes a mostrar.
     * @param context   Contexto de la aplicación.
     */
    public ImageAdapter(List<String> imageUrls, Context context) {
        this.imageUrls = imageUrls;
        this.context = context;
    }

    /**
     * Crea una nueva vista para un elemento del RecyclerView.
     *
     * @param parent   El ViewGroup padre al que se añadirá la nueva vista.
     * @param viewType El tipo de vista de la nueva vista.
     * @return Un nuevo ImageViewHolder que contiene la vista del elemento.
     */
    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_image, parent, false);
        return new ImageViewHolder(view);
    }

    /**
     * Vincula los datos de la imagen a la vista del elemento del RecyclerView.
     *
     * @param holder   El ImageViewHolder que se actualizará para representar los contenidos del elemento en la posición dada.
     * @param position La posición del elemento dentro del conjunto de datos del adaptador.
     */
    @Override
    public void onBindViewHolder(ImageViewHolder holder, int position) {
        String imageUrl = imageUrls.get(position);
        Picasso.get()
                .load(imageUrl)  // Carga la imagen desde la URL
                .into(holder.imageView);  // Muestra la imagen en el ImageView
    }

    /**
     * Devuelve el número total de elementos en el conjunto de datos mantenido por el adaptador.
     *
     * @return El tamaño de la lista de URLs de imágenes.
     */
    @Override
    public int getItemCount() {
        return imageUrls.size();
    }

    /**
     * Actualiza la lista de URLs de imágenes y notifica al adaptador que los datos han cambiado.
     *
     * @param newImageUrls La nueva lista de URLs de imágenes.
     */
    @SuppressLint("NotifyDataSetChanged")
    public void updateImages(List<String> newImageUrls) {
        this.imageUrls = newImageUrls;
        notifyDataSetChanged();
    }

    /**
     * Clase interna que representa la vista de un elemento del RecyclerView.
     */
    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        /**
         * Constructor del ImageViewHolder.
         *
         * @param itemView La vista del elemento.
         */
        public ImageViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_view);  // Encuentra el ImageView por su ID
        }
    }
}