package com.example.moviltpi.core.utils;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.viewpager2.widget.ViewPager2;

/**
 * Esta clase implementa la interfaz {@link ViewPager2.PageTransformer} para aplicar un efecto de transformación
 * a las páginas de un {@link ViewPager2}. El efecto consiste en escalar y ajustar la opacidad de las páginas
 * a medida que se desplazan, creando una apariencia de "profundidad" o "zoom out".
 */
public class EfectoTransformer implements ViewPager2.PageTransformer {

    /**
     * La escala mínima que se aplicará a las páginas cuando estén en los bordes del {@link ViewPager2}.
     * Un valor menor a 1 hará que las páginas se encojan.
     */
    private static final float MIN_SCALE = 0.85f;

    /**
     * Aplica la transformación a la página especificada en función de su posición relativa dentro del {@link ViewPager2}.
     *
     * @param page     La vista de la página que se va a transformar.
     * @param position La posición relativa de la página en el {@link ViewPager2}.
     * <ul>
     * <li>-1: La página está inmediatamente a la izquierda de la página visible.</li>
     * <li>0: La página es la página visible actual.</li>
     * <li>1: La página está inmediatamente a la derecha de la página visible.</li>
     * </ul>
     */
    @Override
    public void transformPage(@NonNull View page, float position) {
        // Calcula la escala de la página en función de su posición.
        // Math.abs(position) devuelve el valor absoluto de la posición, asegurando que la escala disminuya tanto a la izquierda como a la derecha.
        float scale = Math.max(MIN_SCALE, 1 - Math.abs(position));

        // Aplica la escala a la página tanto en el eje X como en el eje Y.
        page.setScaleX(scale);
        page.setScaleY(scale);

        // Ajusta la opacidad de la página en función de la escala.
        // Esto crea un efecto de desvanecimiento a medida que las páginas se alejan del centro.
        // La fórmula asegura que la opacidad varíe entre 0.5 (cuando la escala es MIN_SCALE) y 1 (cuando la escala es 1).
        page.setAlpha(0.5f + (scale - MIN_SCALE) / (1 - MIN_SCALE) * (1 - 0.5f));
    }
}