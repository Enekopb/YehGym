package com.example.yehgym;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
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

public class Signup extends AppCompatActivity {

    private EditText usernameEditText, emailEditText, passwordEditText, weightEditText;
    private RadioGroup genderRadioGroup;
    private Button signupButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.signup);

        // Obtener referencias a los elementos de la interfaz
        usernameEditText = findViewById(R.id.usernameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        weightEditText = findViewById(R.id.weightEditText);
        genderRadioGroup = findViewById(R.id.genderRadioGroup);
        signupButton = findViewById(R.id.signupButton);

        // Configurar listener para el botón de registro
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Obtener los datos ingresados por el usuario
                String username = usernameEditText.getText().toString().trim();
                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();
                String weight = weightEditText.getText().toString().trim();
                int selectedGenderId = genderRadioGroup.getCheckedRadioButtonId();
                String gender = "";
                if (selectedGenderId == R.id.maleRadioButton) {
                    gender = "Hombre";
                    // Iniciar el proceso de registro
                    signup(username, email, password, weight, gender);
                } else if (selectedGenderId == R.id.femaleRadioButton) {
                    gender = "Mujer";
                    // Iniciar el proceso de registro
                    signup(username, email, password, weight, gender);
                } else if (selectedGenderId == R.id.otroRadioButton) {
                    gender = "Otro";
                    // Iniciar el proceso de registro
                    signup(username, email, password, weight, gender);
                } else {
                    Toast.makeText(Signup.this, R.string.genero_no_insertado, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Método para iniciar el proceso de registro en segundo plano
    private void signup(String username, String email, String password, String peso, String genero) {
        String languageCode = "en";
        // Iniciar la tarea de registro en segundo plano
        new SignUpTask().execute(username, email, password, languageCode, peso, genero);
    }

    // Clase interna para realizar la tarea de registro en segundo plano
    private class SignUpTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            // URL para la solicitud de registro
            String urlString = "http://146.148.62.83:81/signup.php"; // Reemplaza con la URL correcta
            String username = params[0];
            String email = params[1];
            String password = params[2];
            String lang = params[3];
            String peso = params[4];
            String genero = params[5];
            try {
                URL url = new URL(urlString);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");

                // Parámetros que se enviarán en la solicitud POST
                String postData = "atleta=" + username + "&email=" + email + "&password=" + password + "&lang=" + lang + "&peso=" + peso + "&genero=" + genero;
                urlConnection.setDoOutput(true);
                urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                PrintWriter out = new PrintWriter(urlConnection.getOutputStream());
                out.print(postData);
                out.close();
                int statusCode = urlConnection.getResponseCode();
                if (statusCode == HttpURLConnection.HTTP_OK) { // 200
                    // Leer la respuesta del servidor si es necesario
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    bufferedReader.close();
                    return stringBuilder.toString();
                } else {
                    // El código de estado no es 200, manejar el caso según tu lógica
                    Log.e("SignUpTask", "Código de estado no válido: " + statusCode);
                    return null;
                }
            } catch (IOException e) {
                Log.e("SignUpTask", "Error en la conexión: " + e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    String status = jsonObject.optString("status");
                    if ("success".equals(status)) {
                        // Usuario creado correctamente
                        String message = jsonObject.optString("message");
                        Toast.makeText(Signup.this, message, Toast.LENGTH_SHORT).show();
                        // Redirigir a la actividad de inicio de sesión
                        Intent intent = new Intent(Signup.this, Login.class);
                        startActivity(intent);
                        finish(); // Finaliza la actividad de registro para que no pueda volver atrás
                    } else {
                        // Usuario no creado
                        String message = jsonObject.optString("message");
                        Toast.makeText(Signup.this, message, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Log.e("SignUpTask", "Error al procesar JSON: " + e.getMessage());
                    Toast.makeText(Signup.this, R.string.server_error, Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(Signup.this, R.string.con_error, Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Método para ir a la actividad de inicio de sesión
    public void goToLogInActivity(View view) {
        Intent intent = new Intent(this, Login.class);
        startActivity(intent);
    }
}