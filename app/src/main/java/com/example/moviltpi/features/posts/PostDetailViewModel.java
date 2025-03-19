package com.example.moviltpi.features.posts;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.List;

/**
 * ViewModel para la actividad de detalles de un post.
 * Maneja la lógica de negocio para cargar comentarios, eliminar posts y grabar nuevos comentarios.
 */
public class PostDetailViewModel extends ViewModel {

    private final MutableLiveData<List<ParseObject>> commentsLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> successLiveData = new MutableLiveData<>();
    private final PostProvider postProvider;

    /**
     * Constructor para PostDetailViewModel.
     */
    public PostDetailViewModel() {
        postProvider = new PostProvider();
    }

    /**
     * Obtiene los comentarios del post como un LiveData.
     *
     * @return LiveData que contiene la lista de comentarios.
     */
    public LiveData<List<ParseObject>> getCommentsLiveData() {
        return commentsLiveData;
    }

    /**
     * Obtiene los errores como un LiveData.
     *
     * @return LiveData que contiene el mensaje de error.
     */
    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }

    /**
     * Obtiene los mensajes de éxito como un LiveData.
     *
     * @return LiveData que contiene el mensaje de éxito.
     */
    public LiveData<String> getSuccessLiveData() {
        return successLiveData;
    }

    /**
     * Carga los comentarios de un post específico.
     *
     * @param postId El ID del post para cargar los comentarios.
     */
    public void fetchCommentario(String postId) {
        postProvider.fetchComments(postId, new PostProvider.CommentsCallback() {
            @Override
            public void onSuccess(List<ParseObject> comments) {
                commentsLiveData.postValue(comments);
            }

            @Override
            public void onFailure(Exception e) {
                errorLiveData.postValue(e.getMessage());
            }
        });
    }

    /**
     * Elimina un post específico.
     *
     * @param postId El ID del post a eliminar.
     */
    public void eliminarPost(String postId) {
        postProvider.deletePost(postId).observeForever(mensaje -> {
            if (mensaje.equals("Post eliminado correctamente")) {
                successLiveData.postValue(mensaje);
            } else {
                errorLiveData.postValue(mensaje);
            }
        });
    }

    /**
     * Graba un nuevo comentario en un post específico.
     *
     * @param postId      El ID del post para grabar el comentario.
     * @param commentText El texto del comentario.
     */
    public void grabaComentario(String postId, String commentText) {
        ParseUser currentUser = ParseUser.getCurrentUser();
        postProvider.saveComment(postId, commentText, currentUser, e -> {
            if (e == null) {
                fetchCommentario(postId); // Actualiza la lista de comentarios
            } else {
                errorLiveData.postValue(e.getMessage());
            }
        });
    }
}