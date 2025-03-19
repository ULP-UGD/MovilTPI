package com.example.moviltpi.features.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.moviltpi.core.utils.Validaciones;
import com.example.moviltpi.databinding.ActivityMainBinding;
import com.example.moviltpi.features.posts.HomeActivity;

/**
 * Actividad principal que maneja el inicio de sesión de usuarios.
 */
public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private MainViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        manejarEventos();
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Verifica si hay una sesión activa al iniciar la actividad.
        if (viewModel != null) {
            viewModel.verificarSesionActiva().observe(this, si -> {
                if (Boolean.TRUE.equals(si)) {
                    // Si hay sesión activa, navega directamente a HomeActivity.
                    Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            });
        }
    }

    /**
     * Configura los listeners de eventos para los botones y campos de texto.
     */
    private void manejarEventos() {
        // Navega a la actividad de registro al hacer clic en el texto de registro.
        binding.tvRegistro.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });

        // Maneja el evento de inicio de sesión al hacer clic en el botón de login.
        binding.btLogin.setOnClickListener(v -> {
            String email = obtenerTextoSeguro(binding.itUsuario);
            String pass = obtenerTextoSeguro(binding.itPassword);

            // Valida el formato del email.
            if (!Validaciones.validarMail(email)) {
                showToast("Email incorrecto");
                return;
            }

            // Valida el formato de la contraseña.
            if (!Validaciones.controlarPasword(pass)) {
                showToast("Password incorrecto");
                return;
            }

            // Realiza el inicio de sesión a través del ViewModel.
            viewModel.login(email, pass).observe(this, user_id -> {
                if (user_id != null) {
                    // Si el inicio de sesión es exitoso, navega a HomeActivity.
                    Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                    startActivity(intent);
                } else {
                    // Si el inicio de sesión falla, muestra un mensaje de error.
                    showToast("Login fallido");
                }
            });
        });
    }

    /**
     * Muestra un Toast con el mensaje especificado.
     *
     * @param message El mensaje a mostrar.
     */
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        limpiarCampos();
    }

    /**
     * Limpia los campos de texto de usuario y contraseña.
     */
    private void limpiarCampos() {
        if (binding != null) {
            binding.itUsuario.setText("");
            binding.itPassword.setText("");
        }
    }

    /**
     * Obtiene el texto de un EditText de forma segura, evitando NullPointerExceptions.
     *
     * @param editText El EditText del cual obtener el texto.
     * @return El texto del EditText, o una cadena vacía si el EditText es nulo.
     */
    private String obtenerTextoSeguro(EditText editText) {
        if (editText == null) {
            return "";
        }
        return editText.getText().toString().trim();
    }
}