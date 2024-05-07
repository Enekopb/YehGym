package com.example.yehgym;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class Login extends AppCompatActivity {

    private EditText usernameEditText, passwordEditText;
    private Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String languageCode = "en";

        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();
                login(username, password);
            }
        });
    }

    private void login(String username, String password) {
        String languageCode = "en";
        Log.d("Login", languageCode);
        new LoginTask().execute(username, password, languageCode);
    }

    private class LoginTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String urlString = "http://146.148.62.83:81/login.php"; // URL para la solicitud de inicio de sesión
            String username = params[0];
            String password = params[1];
            String languageCode = params[2];

            try {
                URL url = new URL(urlString);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");

                // Parámetros que se enviarán en la solicitud POST
                String postData = "username=" + username + "&password=" + password + "&lang=" + languageCode;
                urlConnection.setDoOutput(true);
                urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                PrintWriter out = new PrintWriter(urlConnection.getOutputStream());
                out.print(postData);
                out.close();

                int statusCode = urlConnection.getResponseCode();
                if (statusCode == HttpURLConnection.HTTP_OK) {
                    // Leer la respuesta del servidor
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    bufferedReader.close();
                    return stringBuilder.toString();
                } else {
                    // Manejar casos de error de conexión
                    Log.e("LoginTask", "Código de estado no válido: " + statusCode);
                    return null;
                }
            } catch (IOException e) {
                Log.e("LoginTask", "Error en la conexión: " + e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                try {
                    // Procesar la respuesta JSON del servidor
                    JSONObject jsonObject = new JSONObject(result);
                    String status = jsonObject.optString("status");
                    String message = jsonObject.optString("message");
                    if ("success".equals(status)) {
                        // Usuario autenticado correctamente
                        Toast.makeText(Login.this, message, Toast.LENGTH_SHORT).show();
                        // Guardar la información de inicio de sesión en las preferencias
                        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        String username = usernameEditText.getText().toString().trim();
                        editor.putString("usuario", username);
                        editor.putBoolean("isLoggedIn", true);
                        editor.apply();
                        // Redirigir a la actividad principal u otra actividad según tu flujo de la aplicación
                        Intent intent = new Intent(Login.this, MainActivity.class);
                        startActivity(intent);
                        finish(); // Finalizar la actividad de inicio de sesión para evitar volver atrás
                    } else {
                        // Usuario no autenticado
                        Toast.makeText(Login.this, message, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Log.e("LoginTask", "Error al procesar JSON: " + e.getMessage());
                    Toast.makeText(Login.this, R.string.server_error, Toast.LENGTH_SHORT).show();
                }
            } else {
                // Error de conexión
                Toast.makeText(Login.this, R.string.con_error, Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Método para ir a la actividad de registro
    public void goToSignUpActivity(View view) {
        Intent intent = new Intent(this, Signup.class);
        startActivity(intent);
    }
}
