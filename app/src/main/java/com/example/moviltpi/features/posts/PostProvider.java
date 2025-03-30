package com.example.moviltpi.features.posts;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.moviltpi.core.models.Comentario;
import com.example.moviltpi.core.models.Post;
import com.example.moviltpi.core.models.User;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Proveedor de datos para la gestión de posts en la aplicación.
 * Maneja operaciones CRUD y consultas relacionadas con posts utilizando Parse como backend.
 */
public class PostProvider {

    private static final String TAG = "PostProvider";
    private MutableLiveData<List<Post>> postsLiveData = new MutableLiveData<>();

    /**
     * Agrega un nuevo post al sistema.
     *
     * @param post El objeto Post a agregar
     * @return LiveData con el resultado de la operación (mensaje de éxito o error)
     */
    public LiveData<String> addPost(Post post) {
        MutableLiveData<String> result = new MutableLiveData<>();

        // Verificar si hay un usuario autenticado
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser == null) {
            result.setValue("Error: Usuario no autenticado.");
            return result;
        }
        post.put("user", currentUser);

        // Guardar el post de forma asíncrona
        post.saveInBackground(e -> {
            if (e == null) {
                guardarImagenes(post, result);
            } else {
                Log.e(TAG, "Error al guardar el post", e);
                result.setValue("Error al guardar el post: " + e.getMessage());
            }
        });

        return result;
    }

    /**
     * Guarda las imágenes asociadas a un post en la base de datos.
     *
     * @param post   El post al que se asociarán las imágenes
     * @param result LiveData para reportar el resultado de la operación
     */
    private void guardarImagenes(Post post, MutableLiveData<String> result) {
        ParseRelation<ParseObject> relation = post.getRelation("images");
        List<String> imageUrls = post.getImagenes();
        if (imageUrls != null && !imageUrls.isEmpty()) {
            int totalImages = imageUrls.size();
            AtomicInteger uploadedImages = new AtomicInteger();
            MutableLiveData<String> internalResult = new MutableLiveData<>();

            for (String url : imageUrls) {
                ParseObject imageObject = new ParseObject("Image");
                imageObject.put("url", url);
                imageObject.saveInBackground(imgSaveError -> {
                    if (imgSaveError == null) {
                        relation.add(imageObject);
                        uploadedImages.getAndIncrement();
                        if (uploadedImages.get() == totalImages) {
                            post.saveInBackground(saveError -> {
                                if (saveError == null) {
                                    internalResult.setValue("Post publicado");
                                } else {
                                    Log.e(TAG, "Error al guardar la relación con las imágenes", saveError);
                                    internalResult.setValue("Error al guardar la relación con las imágenes: " + saveError.getMessage());
                                }
                            });
                        }
                    } else {
                        Log.e(TAG, "Error al guardar la imagen", imgSaveError);
                        internalResult.setValue("Error al guardar la imagen: " + imgSaveError.getMessage());
                    }
                });
            }
            // Emitir el resultado final después de intentar guardar todas las imágenes
            internalResult.observeForever(result::setValue);
        } else {
            // Si no hay imágenes, simplemente guardar el post
            post.saveInBackground(saveError -> {
                if (saveError == null) {
                    result.setValue("Post publicado");
                } else {
                    Log.e(TAG, "Error al guardar el post (sin imágenes)", saveError);
                    result.setValue("Error al guardar el post: " + saveError.getMessage());
                }
            });
        }
    }

    /**
     * Obtiene los posts del usuario actual.
     *
     * @return LiveData con la lista de posts del usuario actual
     */
    public LiveData<List<Post>> getPostsByCurrentUser() {
        MutableLiveData<List<Post>> result = new MutableLiveData<>();
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser == null) {
            result.setValue(new ArrayList<>());
            return result;
        }

        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        query.whereEqualTo("user", currentUser);
        query.include("user");
        query.orderByDescending("createdAt");
        ejecutarConsulta(query, result);

        return result;
    }

    /**
     * Obtiene todos los posts disponibles en el sistema.
     *
     * @return LiveData con la lista de todos los posts
     */
    public LiveData<List<Post>> getAllPosts() {
        MutableLiveData<List<Post>> result = new MutableLiveData<>();
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        query.include("user");
        query.orderByDescending("createdAt"); // Añadir orden por defecto
        ejecutarConsulta(query, result);

        return result;
    }

    /**
     * Elimina un post específico y todos los comentarios asociados a él.
     *
     * @param postId ID del post a eliminar.
     * @return LiveData<String> que emite un mensaje indicando el resultado de la operación.
     */
    public LiveData<String> deletePost(String postId) {
        MutableLiveData<String> result = new MutableLiveData<>();

        ParseQuery<Post> queryPost = ParseQuery.getQuery(Post.class);
        queryPost.getInBackground(postId, (post, e) -> {
            if (e == null && post != null) {
                eliminarComentariosAsociados(post, result);
            } else {
                String errorMessage = (e != null) ? e.getMessage() : "Post no encontrado";
                Log.e(TAG, "Error al encontrar el post", e);
                result.setValue("Error al encontrar el post: " + errorMessage);
            }
        });
        return result;
    }

    private void eliminarComentariosAsociados(Post post, MutableLiveData<String> result) {
        ParseQuery<Comentario> queryComentarios = ParseQuery.getQuery(Comentario.class);
        queryComentarios.whereEqualTo(Comentario.KEY_POST, post);
        queryComentarios.findInBackground((comentarios, errorComentarios) -> {
            if (errorComentarios == null) {
                if (comentarios != null && !comentarios.isEmpty()) {
                    ParseObject.deleteAllInBackground(comentarios, eDeleteComments -> {
                        if (eDeleteComments == null) {
                            eliminarPost(post, result, true);
                        } else {
                            Log.e(TAG, "Error al eliminar los comentarios", eDeleteComments);
                            result.setValue("Error al eliminar los comentarios: " + eDeleteComments.getMessage());
                        }
                    });
                } else {
                    eliminarPost(post, result, false);
                }
            } else {
                Log.e(TAG, "Error al buscar los comentarios del post", errorComentarios);
                result.setValue("Error al buscar los comentarios del post: " + errorComentarios.getMessage());
            }
        });
    }

    private void eliminarPost(Post post, MutableLiveData<String> result, boolean commentsDeleted) {
        post.deleteInBackground(eDeletePost -> {
            if (eDeletePost == null) {
                result.setValue(commentsDeleted ? "Post y comentarios asociados eliminados correctamente" : "Post eliminado correctamente (sin comentarios asociados)");
            } else {
                Log.e(TAG, "Error al eliminar el post", eDeletePost);
                result.setValue("Error al eliminar el post: " + eDeletePost.getMessage());
            }
        });
    }

    /**
     * Obtiene los detalles de un post específico.
     *
     * @param postId ID del post a consultar
     * @return LiveData con el post completo incluyendo usuario e imágenes
     */
    public LiveData<Post> getPostDetail(String postId) {
        MutableLiveData<Post> result = new MutableLiveData<>();
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        query.include("user");
        query.include("images");
        query.getInBackground(postId, (post, e) -> {
            if (e == null && post != null) {
                cargarImagenesYUsuario(post, result);
            } else {
                Log.e(TAG, "Error al obtener el post", e);
                result.setValue(null);
            }
        });

        return result;
    }

    /**
     * Carga las imágenes y la información del usuario asociada a un post.
     *
     * @param post   El post a procesar
     * @param result LiveData para retornar el post actualizado
     */
    private void cargarImagenesYUsuario(Post post, MutableLiveData<Post> result) {
        ParseRelation<ParseObject> relation = post.getRelation("images");
        List<String> imageUrls = new ArrayList<>();
        try {
            List<ParseObject> images = relation.getQuery().find();
            for (ParseObject imageObject : images) {
                imageUrls.add(imageObject.getString("url"));
            }
            post.setImagenes(imageUrls);
        } catch (ParseException parseException) {
            Log.e(TAG, "Error al cargar las imágenes del post", parseException);
        }

        ParseObject userObject = post.getParseObject("user");
        if (userObject != null) {
            try {
                userObject.fetchIfNeeded();
                User user = new User();
                user.setUsername(userObject.getString("username"));
                user.setEmail(userObject.getString("email"));
                user.setFotoperfil(userObject.getString("foto_perfil"));
                post.setUser(user);
            } catch (ParseException userFetchException) {
                Log.e(TAG, "Error al cargar la información del usuario del post", userFetchException);
            }
        }
        result.setValue(post);
    }

    // Getters y setters para postsLiveData (considerar si realmente se necesitan setters públicos)
    public LiveData<List<Post>> getPostsLiveData() {
        return postsLiveData;
    }

    // Si se necesita modificar la lista desde fuera, considerar la lógica y seguridad
    // public void setPostsLiveData(MutableLiveData<List<Post>> postsLiveData) {
    //     this.postsLiveData = postsLiveData;
    // }

    /**
     * Interfaz para manejar callbacks de operaciones con comentarios.
     */
    public interface CommentsCallback {
        void onSuccess(List<ParseObject> comments);

        void onFailure(Exception e);
    }

    /**
     * Obtiene los comentarios de un post específico.
     *
     * @param postId   ID del post
     * @param callback Callback para manejar el resultado
     */
    public void fetchComments(String postId, CommentsCallback callback) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Comentario");
        query.whereEqualTo("post", ParseObject.createWithoutData("Post", postId));
        query.include("user");
        query.orderByDescending("createdAt"); // Ordenar comentarios por fecha de creación
        query.findInBackground((comentarios, e) -> {
            if (e == null) {
                callback.onSuccess(comentarios);
            } else {
                Log.e(TAG, "Error al obtener los comentarios del post", e);
                callback.onFailure(e);
            }
        });
    }

    /**
     * Guarda un nuevo comentario para un post.
     *
     * @param postId      ID del post
     * @param commentText Texto del comentario
     * @param currentUser Usuario que realiza el comentario
     * @param callback    Callback para manejar el resultado
     */
    public void saveComment(String postId, String commentText, ParseUser currentUser, SaveCallback callback) {
        ParseObject post = ParseObject.createWithoutData("Post", postId);
        ParseObject comentario = new ParseObject("Comentario");
        comentario.put("texto", commentText);
        comentario.put("post", post);
        comentario.put("user", currentUser);
        comentario.saveInBackground(callback);
    }

    /**
     * Obtiene posts filtrados por categoría y orden.
     *
     * @param categoria Categoría de los posts ("Todas" para no filtrar)
     * @param orden     Criterio de ordenación ("Más recientes" o "Más antiguos")
     * @return LiveData con la lista de posts filtrados
     */
    public LiveData<List<Post>> getPostsFiltrados(String categoria, String orden) {
        MutableLiveData<List<Post>> result = new MutableLiveData<>();
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);

        if (!categoria.equals("Todas")) {
            query.whereEqualTo("categoria", categoria);
        }

        switch (orden) {
            case "Más recientes":
                query.orderByDescending("createdAt");
                break;
            case "Más antiguos":
                query.orderByAscending("createdAt");
                break;
            default:
                query.orderByDescending("createdAt");
                break;
        }

        query.include("user");
        ejecutarConsulta(query, result);

        return result;
    }

    /**
     * Ejecuta una consulta genérica de posts y actualiza el resultado.
     *
     * @param query  La consulta a ejecutar
     * @param result LiveData para almacenar los resultados
     */
    private void ejecutarConsulta(ParseQuery<Post> query, MutableLiveData<List<Post>> result) {
        query.findInBackground((posts, e) -> {
            if (e == null) {
                result.setValue(posts);
            } else {
                Log.e(TAG, "Error al recuperar posts", e);
                result.setValue(new ArrayList<>());
            }
        });
    }
}