package com.example.yehgym;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.DialogFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class entrenamiento extends AppCompatActivity {

    private SQLiteDatabase db;

    private String nombreAtleta;
    private String nombreEntrenador;
    private String idioma;

    private String fecha;

    private String mes;
    private List<String> lista;
    private ArrayAdapter<String> eladaptador;
    private int tema;
    private String urlServidor = "http://146.148.62.83:81/";
    private String languageCode = "es";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            idioma = extras.getString("idioma");
            String pMes= extras.getString("mes");
            String pAño= extras.getString("año");
            String pEntrenador = extras.getString("entrenador");
            String pDia= extras.getString("diaSeleccionado");
            String pAtleta= extras.getString("atleta");
            tema = extras.getInt("tema");
            setTheme(tema);
            mes = pMes;
            fecha = pAño+"-"+pMes+"-"+pDia;
            nombreAtleta = pAtleta;
            nombreEntrenador =pEntrenador;
        }
        if(idioma != null){
            cambiarIdioma(idioma);
        }
        setContentView(R.layout.ver_entrenamiento);

        TextView tv = findViewById(R.id.nombreAtleta);
        tv.setText(nombreAtleta);

       Button guardar = findViewById(R.id.btGuardar);
        guardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String estadoMemoriaExterna = Environment.getExternalStorageState();

                if (Environment.MEDIA_MOUNTED.equals(estadoMemoriaExterna) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(estadoMemoriaExterna)) {
                    // Ejecutar la consulta al servidor PHP
                    new ConsultarEntrenamientoPhp().execute(urlServidor,mes, nombreAtleta,languageCode);
                }
            }
        });

        lista = new ArrayList<>();
        new conseguirNombreEjer().execute(urlServidor,fecha,nombreAtleta,languageCode);

        eladaptador = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,lista);
        ListView lalista = (ListView) findViewById(R.id.listaEjercicios);
        lalista.setAdapter(eladaptador);

        //para borrar atletas al clickar mucho en uno
        lalista.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                // cogemos el nombre del atleta
                String nombreEjer = (String) adapterView.getItemAtPosition(i);
                lista.clear();
                new BorrarEjercicioTask().execute(urlServidor,fecha,nombreAtleta,nombreEjer,languageCode);
                int tiempo= Toast.LENGTH_SHORT;
                Toast aviso = Toast.makeText(entrenamiento.this, "Se ha eliminado el ejercicio", tiempo);
                aviso.show();
                return true;
            }
        });

        ImageButton btAgregar = findViewById(R.id.btAgregar); //boton para agregarAtletas
        btAgregar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText et = findViewById(R.id.NombreAgregarEjer);
                if(!et.getText().toString().equals("")){
                    lista.clear();
                    new AgregarEjercicioAsyncTask().execute(urlServidor,fecha,nombreAtleta,et.getText().toString(),languageCode);
                }else{
                    dialogoAlerta dialogo = new dialogoAlerta();
                    dialogo.setMensaje("El ejercicio no puede estar vacio");
                    dialogo.show(getSupportFragmentManager(), "etiqueta2");
                }

            }
        });
    }

    private void crearNotificacion(){
        NotificationManager elManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder elBuilder = new NotificationCompat.Builder(this, "1");
        elBuilder.setSmallIcon(android.R.drawable.stat_sys_warning);
        elBuilder.setContentTitle("Entrenamiento añadido");
        elBuilder.setAutoCancel(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel elCanal = new NotificationChannel("1", "NombreCanal", NotificationManager.IMPORTANCE_DEFAULT);
            elCanal.enableLights(true);
            elCanal.setLightColor(Color.RED);
            elCanal.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            elCanal.enableVibration(true);
            elManager.createNotificationChannel(elCanal);
        }
        elManager.notify(1, elBuilder.build());
    }



    protected void cambiarIdioma(String idioma){
        Locale nuevaloc = new Locale(idioma);
        Locale.setDefault(nuevaloc);

        Configuration configuration = getBaseContext().getResources().getConfiguration();
        configuration.setLocale(nuevaloc);
        configuration.setLayoutDirection(nuevaloc);

        Context context = getBaseContext().createConfigurationContext(configuration);
        getBaseContext().getResources().updateConfiguration(configuration, context.getResources().getDisplayMetrics());
    }

    public class ConsultarEntrenamientoPhp extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String urlServidor = params[0];
            String mes = params[1];
            String nombreAtleta = params[2];
            String languageCode = params [3];

            HttpURLConnection connection = null;
            urlServidor = urlServidor + "conseguirEntrenoMes.php";

            try {
                URL url = new URL(urlServidor);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);

                // Parámetros de la consulta
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("mes", mes)
                        .appendQueryParameter("nombreAtleta", nombreAtleta)
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
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            // Manejar la respuesta del servidor aquí
            if (result != null) {
                try {
                    // Verifica si la memoria externa está disponible
                    String estadoMemoriaExterna = Environment.getExternalStorageState();

                    if (Environment.MEDIA_MOUNTED.equals(estadoMemoriaExterna) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(estadoMemoriaExterna)) {
                        // Obtiene el directorio de la memoria externa
                        File directorioExterno = getExternalFilesDir(null);
                        Log.d("Directorio externo", directorioExterno.getAbsolutePath());

                        // Crea el archivo en la memoria externa
                        File archivoExterno = new File(directorioExterno, "nombrefichero.txt");

                        // Crear un flujo de escritura para escribir en el archivo
                        OutputStreamWriter fichero = new OutputStreamWriter(new FileOutputStream(archivoExterno));

                        // Convertir la respuesta JSON a objetos Java
                        JSONObject jsonResponse = new JSONObject(result);
                        String status = jsonResponse.getString("status");
                        if (status.equals("success")) {
                            JSONArray entrenamientos = jsonResponse.getJSONArray("entrenamientos");

                            String fechaAnterior = "";

                            // Procesar cada entrenamiento
                            for (int i = 0; i < entrenamientos.length(); i++) {
                                JSONObject entrenamiento = entrenamientos.getJSONObject(i);
                                String fecha = entrenamiento.getString("fecha");
                                String nombreEjercicio = entrenamiento.getString("nombreEjercicio");
                                String series = entrenamiento.getString("series");
                                String repeticiones = entrenamiento.getString("repeticiones");
                                String rpe = entrenamiento.getString("RPE");

                                // Escribir en el archivo en el mismo formato que se hace en el código original
                                if (!fecha.equals(fechaAnterior)) {
                                    fichero.write("\n" + fecha + "\n");
                                    fechaAnterior = fecha;
                                }

                                String linea = nombreEjercicio + " " + series + " x " + repeticiones + " @" + rpe;
                                fichero.write(linea + "\n");
                                Log.d("fila", linea);
                            }
                        }
                        // Cerrar el flujo de escritura
                        fichero.close();
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public class conseguirNombreEjer extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String urlServidor = params[0];
            String fecha = params[1];
            String nombreAtleta = params[2];
            String languageCode = params[3];

            HttpURLConnection connection = null;
            urlServidor = urlServidor + "conseguirNombreEjer.php";

            try {
                URL url = new URL(urlServidor);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);

                // Parámetros de la consulta
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("fecha", fecha)
                        .appendQueryParameter("nombreUsuario", nombreAtleta)
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
                return null;
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.i("lista ejer",result);
            // Manejar la respuesta del servidor aquí
            if (result != null) {
                try {
                    JSONObject jsonResponse = new JSONObject(result);
                    String status = jsonResponse.getString("status");

                    if (status.equals("success")) {
                        JSONArray ejerciciosArray = jsonResponse.getJSONArray("ejercicios");

                        // Limpiar la lista actual de ejercicios
                        lista.clear();

                        // Procesar cada ejercicio y agregarlo a la lista
                        for (int i = 0; i < ejerciciosArray.length(); i++) {
                            String ejercicio = ejerciciosArray.getString(i);
                            lista.add(ejercicio);
                        }

                        // Notificar al adaptador que los datos han cambiado
                        eladaptador.notifyDataSetChanged();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public class BorrarEjercicioTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String urlServidor = params[0];
            String fecha = params[1];
            String nombreAtleta = params[2];
            String nombreEjercicio = params[3];
            String languageCode = params[4];

            HttpURLConnection connection = null;
            urlServidor = urlServidor + "borrarEjercicio.php";

            try {
                URL url = new URL(urlServidor);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);

                // Parámetros de la consulta
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("fecha", fecha)
                        .appendQueryParameter("nombreAtleta", nombreAtleta)
                        .appendQueryParameter("nombreEjercicio", nombreEjercicio)
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
                return null;
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.i("pruebaa",result);
            // Manejar la respuesta del servidor aquí
            if (result != null) {
                // Verificar el estado de la respuesta
                try {
                    JSONObject jsonResponse = new JSONObject(result);
                    String status = jsonResponse.getString("status");
                    if (status.equals("success")) {
                        new conseguirNombreEjer().execute(urlServidor,fecha,nombreAtleta,languageCode);
                        recreate();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class AgregarEjercicioAsyncTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String urlServidor = params[0];
            String fecha = params[1];
            String nombreUsuario = params[2];
            String nombreEjercicio = params[3];
            String languageCode = params[4];

            HttpURLConnection connection = null;
            urlServidor = urlServidor + "agregarEjercicio.php";

            try {
                URL url = new URL(urlServidor);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);

                // Parámetros de la consulta
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("fecha", fecha)
                        .appendQueryParameter("nombreUsuario", nombreUsuario)
                        .appendQueryParameter("nombreEjercicio", nombreEjercicio)
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
                return null;
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.i("mensajeee",result);
            // Manejar la respuesta del servidor aquí
            if (result != null) {
                try {
                    JSONObject jsonResponse = new JSONObject(result);
                    String status = jsonResponse.getString("status");
                    if (status.equals("success")) {
                        // Ejercico agregado
                        new conseguirNombreEjer().execute(urlServidor,fecha,nombreAtleta,languageCode);
                        recreate();
                        Toast.makeText(getApplicationContext(), "Ejercicio agregado", Toast.LENGTH_SHORT).show();
                        crearNotificacion();
                    } else {
                        // Error al agregar el ejercicio
                        Toast.makeText(getApplicationContext(), "Error al agregar el ejercicio", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }







}
