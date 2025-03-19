package com.example.moviltpi.core.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import androidx.activity.result.ActivityResultLauncher;
import androidx.core.app.ActivityCompat;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Clase de utilidades para el manejo de imágenes, incluyendo permisos, acceso a la galería,
 * subida a Parse y obtención de la ruta real de un URI.
 */
public class ImageUtils {

    /**
     * Solicita permisos para acceder a las imágenes del dispositivo.
     *
     * @param activity    La actividad desde la cual se solicitan los permisos.
     * @param permisos    Un arreglo de permisos a solicitar.
     * @param requestCode El código de solicitud para la respuesta de los permisos.
     */
    public static void pedirPermisos(Activity activity, String[] permisos, int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13 o superior
            permisos = new String[]{Manifest.permission.READ_MEDIA_IMAGES};
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { // Android 6.0 o superior
            permisos = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
        }
        ActivityCompat.requestPermissions(activity, permisos, requestCode);
    }

    /**
     * Abre la galería de imágenes del dispositivo.
     *
     * @param context   El contexto de la aplicación.
     * @param launcher  El ActivityResultLauncher para manejar el resultado de la selección de la imagen.
     */
    public static void openGallery(Context context, ActivityResultLauncher<Intent> launcher) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        launcher.launch(intent);
    }

    /**
     * Sube una imagen a Parse desde un URI.
     *
     * @param context    El contexto de la aplicación.
     * @param imageUri   El URI de la imagen a subir.
     * @param callback   El callback para manejar el resultado de la subida.
     */
    public static void subirImagenAParse(Context context, Uri imageUri, ImageUploadCallback callback) {
        InputStream inputStream = null;
        try {
            inputStream = context.getContentResolver().openInputStream(imageUri);
            byte[] bytes = getBytesFromInputStream(inputStream);

            if (bytes == null) {
                callback.onFailure(new Exception("El arreglo de bytes es null"));
                return;
            }
            ParseFile parseFile = new ParseFile("image.jpg", bytes);
            parseFile.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        String imageUrl = parseFile.getUrl();
                        callback.onSuccess(imageUrl);
                    } else {
                        callback.onFailure(e);
                    }
                }
            });
        } catch (IOException e) {
            callback.onFailure(e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Lee bytes desde un InputStream.
     *
     * @param inputStream El InputStream desde el cual leer los bytes.
     * @return Un arreglo de bytes leído desde el InputStream.
     * @throws IOException Si ocurre un error de E/S.
     */
    private static byte[] getBytesFromInputStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }

        return byteBuffer.toByteArray();
    }

    /**
     * Interfaz para manejar el resultado de la subida de imágenes.
     */
    public interface ImageUploadCallback {
        /**
         * Se llama cuando la subida de la imagen es exitosa.
         *
         * @param imageUrl La URL de la imagen subida.
         */
        void onSuccess(String imageUrl);

        /**
         * Se llama cuando la subida de la imagen falla.
         *
         * @param e La excepción que ocurrió durante la subida.
         */
        void onFailure(Exception e);
    }

    /**
     * Obtiene la ruta real de un archivo desde un URI.
     *
     * @param context El contexto de la aplicación.
     * @param uri     El URI del archivo.
     * @return La ruta real del archivo, o null si no se puede obtener.
     */
    public static String getRealPathFromURI(Context context, Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
        if (cursor == null) return null;
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String path = cursor.getString(column_index);
        cursor.close();
        return path;
    }
}