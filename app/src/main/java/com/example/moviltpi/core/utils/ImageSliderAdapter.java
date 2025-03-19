package com.example.moviltpi.core.utils;

import static com.example.moviltpi.core.utils.ImageUtils.getRealPathFromURI;

import android.net.Uri;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.moviltpi.R;

import java.util.List;

/**
 * Adaptador para RecyclerView que muestra un slider de imágenes cargadas desde URLs o URIs usando Glide.
 * Intenta obtener la ruta real del archivo desde la URI y utiliza Glide para cargar las imágenes.
 */
public class ImageSliderAdapter extends RecyclerView.Adapter<ImageSliderAdapter.ImageViewHolder> {

    private final List<String> imageUrls;

    /**
     * Constructor del adaptador.
     *
     * @param imageUrls Lista de URLs o URIs de las imágenes a mostrar en el slider.
     */
    public ImageSliderAdapter(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    /**
     * Crea una nueva vista para un elemento del RecyclerView (un ImageView).
     *
     * @param parent   El ViewGroup padre al que se añadirá la nueva vista.
     * @param viewType El tipo de vista de la nueva vista.
     * @return Un nuevo ImageViewHolder que contiene el ImageView.
     */
    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ImageView imageView = new ImageView(parent.getContext());
        imageView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        return new ImageViewHolder(imageView);
    }

    /**
     * Vincula los datos de la imagen a la vista del elemento del RecyclerView.
     *
     * @param holder   El ImageViewHolder que se actualizará para representar los contenidos del elemento en la posición dada.
     * @param position La posición del elemento dentro del conjunto de datos del adaptador.
     */
    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        String imageUrl = imageUrls.get(position);
        Log.d("ImageSliderAdapter", "Cargando imagen: " + imageUrl);

        Uri uri = Uri.parse(imageUrl);
        String realPath = getRealPathFromURI(holder.imageView.getContext(), uri);

        if (realPath != null) {
            // Intenta cargar la imagen desde la ruta real del archivo.
            Glide.with(holder.imageView.getContext())
                    .load(realPath)
                    .centerCrop()
                    .apply(new RequestOptions()
                            .placeholder(R.drawable.uploadimg) // Imagen de carga mientras se carga
                            .error(R.drawable.ic_close)) // Imagen de error si falla la carga
                    .into(holder.imageView);
        } else {
            // Si no se puede obtener la ruta real, carga la imagen directamente desde la URI.
            Glide.with(holder.imageView.getContext())
                    .load(uri)
                    .apply(new RequestOptions()
                            .placeholder(R.drawable.uploadimg)
                            .error(R.drawable.ic_close))
                    .into(holder.imageView);
        }
    }

    /**
     * Devuelve el número total de elementos en el conjunto de datos mantenido por el adaptador.
     *
     * @return El tamaño de la lista de URLs o URIs de imágenes.
     */
    @Override
    public int getItemCount() {
        return imageUrls.size();
    }

    /**
     * Clase interna que representa la vista de un elemento del RecyclerView (un ImageView).
     */
    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        /**
         * Constructor del ImageViewHolder.
         *
         * @param itemView El ImageView que representa la vista del elemento.
         */
        public ImageViewHolder(@NonNull ImageView itemView) {
            super(itemView);
            this.imageView = itemView;
        }
    }
}