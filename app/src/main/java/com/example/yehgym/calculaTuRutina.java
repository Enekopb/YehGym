package com.example.yehgym;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class calculaTuRutina extends AppCompatActivity {

    private String nombreUsuario = "Eneko";

    private EditText editTextFechaInicio;
    private EditText editTextFechaFinal;
    private EditText editTextFrecuencia;
    private String languageCode = "es";
    private String urlServidor = "http://146.148.62.83:81/";
    private static final String PREFERENCIAS_TEMA = "preferencias_tema";
    private static final String TEMA_PREF_KEY = "tema_pref_key";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPreferences = getSharedPreferences(PREFERENCIAS_TEMA, MODE_PRIVATE);
        String temaGuardado = sharedPreferences.getString(TEMA_PREF_KEY, "DEFAULT");

        // Aplicar el tema correspondiente
        if (temaGuardado.equals("DEFAULT")) {
            setTheme(R.style.AppThemeLight);
        } else {
            setTheme(R.style.AppThemeDark);
        }

        setContentView(R.layout.calcula_tu_rutina);

        Button buttonEnviar = findViewById(R.id.botonEnviar);

        // Acción del botón enviar
        buttonEnviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Inicialización de vistas
                editTextFechaInicio = findViewById(R.id.editTextFechaInicio);
                editTextFechaFinal = findViewById(R.id.editTextFechaFinal);
                editTextFrecuencia = findViewById(R.id.editTextFrecuencia);

                // Obtener valores de los EditText
                String fechaInicio = editTextFechaInicio.getText().toString();
                String fechaFinal = editTextFechaFinal.getText().toString();
                String frecuencia = editTextFrecuencia.getText().toString();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Calendar calendarInicio = Calendar.getInstance();
                Calendar calendarFinal = Calendar.getInstance();

                LinearLayout linearLayout1 = findViewById(R.id.LL1);
                EditText editTextEjercicio1 = linearLayout1.findViewById(R.id.ETEjer1);
                EditText editTextSeries1 = linearLayout1.findViewById(R.id.ETSeries1);
                EditText editTextRepeticiones1 = linearLayout1.findViewById(R.id.ETReps1);
                EditText editTextRPE1 = linearLayout1.findViewById(R.id.ETRPE1);

                // Comprobaciones para el primer conjunto de EditText
                String ejercicio1 = editTextEjercicio1.getText().toString().trim();
                String series1 = editTextSeries1.getText().toString().trim();
                String repeticiones1 = editTextRepeticiones1.getText().toString().trim();
                String rpe1 = editTextRPE1.getText().toString().trim();

                // Obtener valores de los EditText del segundo LinearLayout (similar al anterior)
                LinearLayout linearLayout2 = findViewById(R.id.LL2);
                EditText editTextEjercicio2 = linearLayout2.findViewById(R.id.ETEjer2);
                EditText editTextSeries2 = linearLayout2.findViewById(R.id.ETSeries2);
                EditText editTextRepeticiones2 = linearLayout2.findViewById(R.id.ETReps2);
                EditText editTextRPE2 = linearLayout2.findViewById(R.id.ETRPE2);

                // Comprobaciones para el primer conjunto de EditText
                String ejercicio2 = editTextEjercicio2.getText().toString().trim();
                String series2 = editTextSeries2.getText().toString().trim();
                String repeticiones2 = editTextRepeticiones2.getText().toString().trim();
                String rpe2 = editTextRPE2.getText().toString().trim();

                // Obtener valores de los EditText del tercer LinearLayout (similar al anterior)
                LinearLayout linearLayout3 = findViewById(R.id.LL3);
                EditText editTextEjercicio3 = linearLayout3.findViewById(R.id.ETEjer3);
                EditText editTextSeries3 = linearLayout3.findViewById(R.id.ETSeries3);
                EditText editTextRepeticiones3 = linearLayout3.findViewById(R.id.ETReps3);
                EditText editTextRPE3 = linearLayout3.findViewById(R.id.ETRPE3);

                // Comprobaciones para el primer conjunto de EditText
                String ejercicio3 = editTextEjercicio3.getText().toString().trim();
                String series3 = editTextSeries3.getText().toString().trim();
                String repeticiones3 = editTextRepeticiones3.getText().toString().trim();
                String rpe3 = editTextRPE3.getText().toString().trim();

                // Obtener valores de los EditText del cuarto LinearLayout (similar al anterior)
                LinearLayout linearLayout4 = findViewById(R.id.LL4);
                EditText editTextEjercicio4 = linearLayout4.findViewById(R.id.ETEjer4);
                EditText editTextSeries4 = linearLayout4.findViewById(R.id.ETSeries4);
                EditText editTextRepeticiones4 = linearLayout4.findViewById(R.id.ETReps4);
                EditText editTextRPE4 = linearLayout4.findViewById(R.id.ETRPE4);

                // Comprobaciones para el primer conjunto de EditText
                String ejercicio4 = editTextEjercicio4.getText().toString().trim();
                String series4 = editTextSeries4.getText().toString().trim();
                String repeticiones4 = editTextRepeticiones4.getText().toString().trim();
                String rpe4 = editTextRPE4.getText().toString().trim();

                try {
                    calendarInicio.setTime(sdf.parse(fechaInicio));
                    calendarFinal.setTime(sdf.parse(fechaFinal));

                    // Ejecutar AsyncTask para insertar los datos en la base de datos
                    while (calendarInicio.getTime().before(calendarFinal.getTime())) {
                        String fechaActual = sdf.format(calendarInicio.getTime());
                        Log.i("infor fecha",fechaActual);

                        new InsertarDatos().execute(urlServidor, fechaActual, editTextEjercicio1.getText().toString(), editTextSeries1.getText().toString(), editTextRepeticiones1.getText().toString(), editTextRPE1.getText().toString(),nombreUsuario, languageCode);
                        //new InsertarDatos().execute(urlServidor, fechaActual, editTextEjercicio2.getText().toString(), editTextSeries2.getText().toString(), editTextRepeticiones2.getText().toString(), editTextRPE2.getText().toString(),nombreUsuario ,languageCode);
                        //new InsertarDatos().execute(urlServidor, fechaActual, editTextEjercicio3.getText().toString(), editTextSeries3.getText().toString(), editTextRepeticiones3.getText().toString(), editTextRPE3.getText().toString(),nombreUsuario, languageCode);
                        //new InsertarDatos().execute(urlServidor, fechaActual, editTextEjercicio4.getText().toString(), editTextSeries4.getText().toString(), editTextRepeticiones4.getText().toString(), editTextRPE4.getText().toString(),nombreUsuario, languageCode);
                        calendarInicio.add(Calendar.DAY_OF_MONTH, Integer.parseInt(frecuencia));
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void menu(View view){
        finish();
    }

    private class InsertarDatos extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String urlServidor = params[0];
            String fecha = params[1];
            String nombreEjercicio = params[2];
            String series = params[3];
            String repeticiones = params[4];
            String RPE = params[5];
            String usuario = params[6];
            String languageCode = params [7];

            HttpURLConnection connection = null;
            urlServidor = urlServidor + "calcularRutina.php";
            try {
                URL url = new URL(urlServidor);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);

                // Parámetros de la consulta
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("fecha", fecha)
                        .appendQueryParameter("nombreEjercicio", nombreEjercicio)
                        .appendQueryParameter("series", series)
                        .appendQueryParameter("repeticiones", repeticiones)
                        .appendQueryParameter("RPE", RPE)
                        .appendQueryParameter("usuario", usuario)
                        .appendQueryParameter("lang", languageCode);
                String parametrosURL = builder.build().getEncodedQuery();

                OutputStream outputStream = connection.getOutputStream();
                outputStream.write(parametrosURL.getBytes());
                outputStream.flush();
                outputStream.close();

                // Obtener respuesta del servidor
                InputStream inputStream = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                inputStream.close();

                return response.toString();
            } catch (IOException e) {
                e.printStackTrace();
                return e.getMessage();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            // Manejar el resultado de la inserción, mostrar un Toast u otra acción según sea necesario
            Log.i("infor",result);
            if (result != null) {
                try {
                    JSONObject jsonResponse = new JSONObject(result);
                    String status = jsonResponse.getString("status");
                    String message = jsonResponse.getString("message");
                    if (status.equals("success")) {
                        Toast.makeText(calculaTuRutina.this, message, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(calculaTuRutina.this, message, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}

