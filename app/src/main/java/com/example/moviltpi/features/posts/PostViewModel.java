package com.example.moviltpi.features.posts;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.moviltpi.core.models.Post;
import java.util.List;

/**
 * ViewModel para la gestión de posts en la UI.
 * Actúa como intermediario entre la UI y el PostProvider, manejando la lógica de presentación.
 */
public class PostViewModel extends ViewModel {
    private final PostProvider postProvider;
    private final MutableLiveData<List<Post>> postsLiveData;
    private final MutableLiveData<String> postSuccess;
    private String currentCategoria = "Todas";  // Categoría actual para filtrado
    private String currentOrden = "Más recientes";  // Orden actual para filtrado

    /**
     * Constructor del ViewModel.
     * Inicializa el proveedor de posts y los LiveData, y carga los posts iniciales.
     */
    public PostViewModel() {
        postProvider = new PostProvider();
        postsLiveData = new MutableLiveData<>();
        postSuccess = new MutableLiveData<>();
        loadPosts();  // Carga inicial de posts
    }

    /**
     * Obtiene la lista de posts como LiveData.
     *
     * @return LiveData con la lista actual de posts
     */
    public LiveData<List<Post>> getPosts() {
        return postsLiveData;
    }

    /**
     * Obtiene el estado de éxito de operaciones con posts.
     *
     * @return LiveData con mensajes de éxito o error
     */
    public LiveData<String> getPostSuccess() {
        return postSuccess;
    }

    /**
     * Publica un nuevo post.
     *
     * @param post El post a publicar
     * @return LiveData con el resultado de la operación
     */
    public LiveData<String> publicar(Post post) {
        MutableLiveData<String> resultLiveData = new MutableLiveData<>();
        postProvider.addPost(post).observeForever(result -> {
            postSuccess.setValue(result);
            resultLiveData.setValue(result);
            // Si la publicación es exitosa, recargar los posts
            if (result.equals("Post publicado")) {
                loadPosts();
            }
        });
        return resultLiveData;
    }

    /**
     * Obtiene todos los posts disponibles.
     *
     * @return LiveData con todos los posts
     */
    public LiveData<List<Post>> getAllPosts() {
        return postProvider.getAllPosts();
    }

    /**
     * Obtiene los posts del usuario actual.
     *
     * @return LiveData con los posts del usuario actual
     */
    public LiveData<List<Post>> getPostsByCurrentUser() {
        return postProvider.getPostsByCurrentUser();
    }

    /**
     * Aplica filtros de categoría y orden a la lista de posts.
     *
     * @param categoria Categoría para filtrar ("Todas" para no filtrar por categoría)
     * @param orden     Criterio de ordenación ("Más recientes" o "Más antiguos")
     */
    public void aplicarFiltros(String categoria, String orden) {
        Log.d("PostViewModel", "Aplicando filtros: Categoría=" + categoria + ", Orden=" + orden);
        this.currentCategoria = categoria;
        this.currentOrden = orden;
        loadPosts();  // Recargar posts con los nuevos filtros
    }

    /**
     * Resetea los filtros a sus valores predeterminados.
     */
    public void resetFilters() {
        Log.d("PostViewModel", "Reseteando filtros");
        this.currentCategoria = "Todas";
        this.currentOrden = "Más recientes";

    }

    /**
     * Carga los posts aplicando los filtros actuales.
     */
    public void loadPosts() {
        Log.d("PostViewModel", "Cargando posts con filtros: Categoría=" + currentCategoria + ", Orden=" + currentOrden);
        postProvider.getPostsFiltrados(currentCategoria, currentOrden).observeForever(posts -> {
            Log.d("PostViewModel", "Posts cargados: " + (posts != null ? posts.size() : 0));
            postsLiveData.setValue(posts);  // Actualizar la lista observable
        });
    }

    /**
     * Verifica si hay filtros activos diferentes a los predeterminados.
     *
     * @return true si hay filtros activos, false en caso contrario
     */
    public boolean isFiltered() {
        boolean filtered = !currentCategoria.equals("Todas") || !currentOrden.equals("Más recientes");
        Log.d("PostViewModel", "¿Hay filtros activos? " + filtered);
        return filtered;
    }
}