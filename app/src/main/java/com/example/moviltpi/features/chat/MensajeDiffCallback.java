package com.example.moviltpi.features.chat;

import androidx.recyclerview.widget.DiffUtil;

import com.example.moviltpi.core.models.Mensaje;

import java.util.List;

/**
 * Clase de utilidad para DiffUtil que compara dos listas de objetos Mensaje para determinar las diferencias.
 * Se utiliza para actualizar eficientemente el RecyclerView de mensajes en la interfaz de chat.
 */
public class MensajeDiffCallback extends DiffUtil.Callback {
    private final List<Mensaje> oldList;
    private final List<Mensaje> newList;

    /**
     * Constructor para MensajeDiffCallback.
     *
     * @param oldList La lista antigua de mensajes.
     * @param newList La nueva lista de mensajes.
     */
    public MensajeDiffCallback(List<Mensaje> oldList, List<Mensaje> newList) {
        this.oldList = oldList;
        this.newList = newList;
    }

    @Override
    public int getOldListSize() {
        return oldList.size();
    }

    @Override
    public int getNewListSize() {
        return newList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        // Compara si los objetos son el mismo elemento (tienen el mismo ID).
        return oldList.get(oldItemPosition).getObjectId().equals(newList.get(newItemPosition).getObjectId());
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        // Compara si los contenidos de los objetos son los mismos (todos los campos son iguales).
        return oldList.get(oldItemPosition).equals(newList.get(newItemPosition));
    }
}