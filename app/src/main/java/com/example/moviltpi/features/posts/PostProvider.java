package com.example.moviltpi.features.posts;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
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

/**
 * Proveedor de datos para la gestión de posts en la aplicación.
 * Maneja operaciones CRUD y consultas relacionadas con posts utilizando Parse como backend.
 */
public class PostProvider {
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
        for (String url : post.getImagenes()) {
            ParseObject imageObject = new ParseObject("Image");
            imageObject.put("url", url);
            imageObject.saveInBackground(imgSaveError -> {
                if (imgSaveError == null) {
                    relation.add(imageObject);
                    post.saveInBackground(saveError -> {
                        if (saveError == null) {
                            result.setValue("Post publicado");
                        } else {
                            result.setValue("Error al guardar la relación con las imágenes: " + saveError.getMessage());
                        }
                    });
                } else {
                    result.setValue("Error al guardar la imagen: " + imgSaveError.getMessage());
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
        ejecutarConsulta(query, result);

        return result;
    }

    /**
     * Elimina un post específico.
     *
     * @param postId ID del post a eliminar
     * @return LiveData con el resultado de la operación
     */
    public LiveData<String> deletePost(String postId) {
        MutableLiveData<String> result = new MutableLiveData<>();

        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        query.getInBackground(postId, (post, e) -> {
            if (e == null) {
                post.deleteInBackground(e1 -> {
                    if (e1 == null) {
                        result.setValue("Post eliminado correctamente");
                    } else {
                        result.setValue("Error al eliminar el post: " + e1.getMessage());
                    }
                });
            } else {
                result.setValue("Error al encontrar el post: " + e.getMessage());
            }
        });

        return result;
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
            if (e == null) {
                cargarImagenesYUsuario(post, result);
            } else {
                result.setValue(null);
                Log.e("PostProvider", "Error al obtener el post: ", e);
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
        try {
            List<ParseObject> images = relation.getQuery().find();
            List<String> imageUrls = new ArrayList<>();
            for (ParseObject imageObject : images) {
                imageUrls.add(imageObject.getString("url"));
            }
            post.setImagenes(imageUrls);
        } catch (ParseException parseException) {
            parseException.printStackTrace();
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
                userFetchException.printStackTrace();
            }
        }
        result.setValue(post);
    }

    // Getters y setters para postsLiveData
    public MutableLiveData<List<Post>> getPostsLiveData() {
        return postsLiveData;
    }

    public void setPostsLiveData(MutableLiveData<List<Post>> postsLiveData) {
        this.postsLiveData = postsLiveData;
    }

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
     * @param postId  ID del post
     * @param callback Callback para manejar el resultado
     */
    public void fetchComments(String postId, CommentsCallback callback) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Comentario");
        query.whereEqualTo("post", ParseObject.createWithoutData("Post", postId));
        query.include("user");
        query.findInBackground((comentarios, e) -> {
            if (e == null) {
                callback.onSuccess(comentarios);
            } else {
                callback.onFailure(e);
            }
        });
    }

    /**
     * Guarda un nuevo comentario para un post.
     *
     * @param postId     ID del post
     * @param commentText Texto del comentario
     * @param currentUser Usuario que realiza el comentario
     * @param callback    Callback para manejar el resultado
     */
    public void saveComment(String postId, String commentText, ParseUser currentUser, SaveCallback callback) {
        ParseObject post = ParseObject.createWithoutData("Post", postId);
        ParseObject comentario = ParseObject.create("Comentario");
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
        query.findInBackground((posts, e) -> {
            if (e == null) {
                result.setValue(posts != null ? posts : new ArrayList<>());
            } else {
                result.setValue(new ArrayList<>());
                Log.e("PostProvider", "Error al recuperar posts filtrados: ", e);
            }
        });

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
                result.setValue(new ArrayList<>());
                Log.e("PostProvider", "Error al recuperar posts: ", e);
            }
        });
    }
}