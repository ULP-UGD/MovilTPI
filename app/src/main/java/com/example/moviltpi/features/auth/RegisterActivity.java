package com.example.moviltpi.features.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.moviltpi.core.models.User;
import com.example.moviltpi.core.utils.Validaciones;
import com.example.moviltpi.databinding.ActivityRegisterBinding;

import java.util.Objects;

/**
 * Actividad para el registro de nuevos usuarios.
 */
public class RegisterActivity extends AppCompatActivity {
    private ActivityRegisterBinding binding;
    private RegisterViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        viewModel = new ViewModelProvider(this).get(RegisterViewModel.class);
        // Observa el resultado del registro y muestra un Toast con el mensaje.
        viewModel.getRegisterResult().observe(this, this::showToast);
        manejarEventos();
    }

    /**
     * Configura los listeners de eventos para los botones y otros elementos interactivos.
     */
    private void manejarEventos() {
        // Evento para volver a la pantalla de inicio de sesión.
        binding.circleImageBack.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        });

        // Evento para realizar el registro al hacer clic en el botón de registrar.
        binding.btRegistrar.setOnClickListener(v -> realizarRegistro());
    }

    /**
     * Realiza el proceso de registro del usuario, validando los datos ingresados.
     */
    private void realizarRegistro() {
        String usuario = Objects.requireNonNull(binding.itUsuario.getText()).toString().trim();
        String email = Objects.requireNonNull(binding.itEmail.getText()).toString().trim();
        String pass = Objects.requireNonNull(binding.itPassword.getText()).toString().trim();
        String pass1 = Objects.requireNonNull(binding.itPassword1.getText()).toString().trim();

        // Validaciones de entrada
        if (!Validaciones.validarTexto(usuario)) {
            showToast("Usuario incorrecto");
            return;
        }
        if (!Validaciones.validarMail(email)) {
            showToast("El correo no es válido");
            return;
        }
        String passError = Validaciones.validarPass(pass, pass1);
        if (passError != null) {
            showToast(passError);
            return;
        }

        User user = new User();
        user.setEmail(email);
        user.setUsername(usuario);
        user.setPassword(pass);
        Log.d("RegisterActivity", "Usuario registrado: " + usuario + ", Email: " + email + " pass: " + pass);
        viewModel.register(user);
    }

    /**
     * Muestra un Toast con el mensaje proporcionado.
     *
     * @param message El mensaje a mostrar.
     */
    private void showToast(String message) {
        if (message != null) {
            Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_LONG).show();
        }
    }
}