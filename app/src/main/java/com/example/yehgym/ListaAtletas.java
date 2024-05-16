package com.example.yehgym;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.DialogFragment;

import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ListaAtletas extends AppCompatActivity {
    private SQLiteDatabase db;

    private String entrenador;
    private ArrayAdapter<String> eladaptador;

    private String idioma;

    private List<String> lista = new ArrayList<>();
    private int tema;

    private String languageCode = "es";
    private String urlServidor = "http://146.148.62.83:81/";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String pUsuario= extras.getString("entrenador");
           this.entrenador = pUsuario;
           idioma = extras.getString("idioma");
           tema = extras.getInt("tema");
           setTheme(tema);

        }
        if(idioma != null){
            cambiarIdioma(idioma);
        }
        setContentView(R.layout.listausuarios);

        new getAtletas().execute(urlServidor, entrenador, languageCode);
        eladaptador = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,lista);
        ListView lalista = (ListView) findViewById(R.id.listView_atletas);
        lalista.setAdapter(eladaptador);

        lalista.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id){
                Log.i("etiqueta", ((TextView)view).getText().toString()+", "+position+", "+id);
                Intent i = new Intent(ListaAtletas.this, ListaAmigos.class);
                i.putExtra("atleta", ((TextView)view).getText().toString());
                i.putExtra("entrenador",entrenador);
                i.putExtra("idioma",idioma);
                i.putExtra("tema",tema);
                startActivity(i);
                finish();
            }
        });

        //para borrar atletas al clickar mucho en uno
        lalista.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                // cogemos el nombre del atleta
                String nombreAtleta = (String) adapterView.getItemAtPosition(i);
                lista.clear();
                new removeAtletas().execute(urlServidor, nombreAtleta, languageCode);
                new getAtletas().execute(urlServidor, entrenador, languageCode);
                int tiempo= Toast.LENGTH_SHORT;
                Toast aviso = Toast.makeText(ListaAtletas.this, "Se ha eliminado el atleta", tiempo);
                aviso.show();
                return true;
            }
        });

        Button btBuscar = findViewById(R.id.btBuscar);
        btBuscar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText et = findViewById(R.id.NombreAtleta);
                String pNombre = et.getText().toString();

                // Llamar a la clase AsyncTask para obtener el usuario con el entrenador especificado
                new buscarUsuario().execute(urlServidor, pNombre, entrenador,languageCode);

                et.setText("");
            }
        });

        Button btCalcular = findViewById(R.id.googleCalendarButton);
        btCalcular.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ListaAtletas.this, calculaTuRutina.class);
                startActivity(i);
                finish();
            }
        });


        ImageButton btAgregar = findViewById(R.id.btAgregar); //boton para agregarAtletas
        btAgregar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText et = findViewById(R.id.NombreAgregarAtleta);
                String usuario = et.getText().toString();

                // Comprobar si el usuario existe
                new ComprobarUsuario().execute(urlServidor, usuario,languageCode);
            }
        });

        Button btChat = findViewById(R.id.chatsButton); //boton para agregarAtletas
        btChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ListaAtletas.this, ListaAmigos.class);
                startActivity(intent);
            }
        });

    }

    private void crearNotificacion(){
        NotificationManager elManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder elBuilder = new NotificationCompat.Builder(this, "1");
        elBuilder.setSmallIcon(android.R.drawable.stat_sys_warning);
        elBuilder.setContentTitle("Atleta añadido correctamente");
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


    public void onBackPressed() {
        super.onBackPressed();
        DialogFragment dialogoAlerta = new salirAplicacion();
        dialogoAlerta.show(getSupportFragmentManager(), "etiqueta");

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

    //// Obtener el usuario que busca en la lupa ////
    private class buscarUsuario extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String urlServidor = params[0];
            String usuario = params[1];
            String entrenador = params[2];
            String languageCode = params[3];
            HttpURLConnection connection = null;
            urlServidor = urlServidor + "obtenerUsuarioEntrenador.php";
            try {
                URL url = new URL(urlServidor);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);

                // Parámetros de la consulta
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("nombre", usuario)
                        .appendQueryParameter("entrenador", entrenador)
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
        protected void onPostExecute(String jsonResponse) {
            super.onPostExecute(jsonResponse);
            // Analizar el JSON
            JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();

            // Verificar si la operación fue exitosa
            if (jsonObject.has("status") && jsonObject.get("status").getAsString().equals("success")) {

                // Si el usuario existe con el entrenador especificado, agregarlo a la lista
                String usuarioRespuesta = jsonObject.get("usuario").getAsString();
                lista.clear();
                lista.add(usuarioRespuesta);
                eladaptador.notifyDataSetChanged();
            }else {
                lista.clear();
                // Si el usuario no existe con el entrenador especificado, obtener la lista de todos los usuarios
                new getAtletas().execute(urlServidor, entrenador, languageCode);
            }
        }
    }


    //// Obtener el usuario que busca en la lupa ////


    ////  Comprobar si el usuario existe ////
    private class ComprobarUsuario extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String urlServidor = params[0];
            String usuario = params[1];
            String languageCode = params[2];

            HttpURLConnection connection = null;
            urlServidor = urlServidor + "comprobarUsuario.php";
            try {
                URL url = new URL(urlServidor);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);

                // Parámetros de la consulta
                Uri.Builder builder = new Uri.Builder()
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
                return null;
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }

        @Override
        protected void onPostExecute(String jsonResponse) {
            super.onPostExecute(jsonResponse);
            Log.i("entrenador exis",jsonResponse);
            JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();
            if (jsonObject.has("status") && jsonObject.get("status").getAsString().equals("success")) {
                // El usuario existe
                EditText et = findViewById(R.id.NombreAgregarAtleta);
                String usuario = et.getText().toString();
                new atletaTieneEntrenador().execute(urlServidor,usuario,languageCode);
            } else {
                // El usuario no existe
                dialogoAlerta dialogo = new dialogoAlerta();
                dialogo.setMensaje("El usuario introducido no existe");
                dialogo.show(getSupportFragmentManager(), "etiqueta2");
            }

        }
    }

    //// Comprobar si el usuario existe ////

    //// comprobar si el atleta tiene entrenador ////

    private class atletaTieneEntrenador extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String urlServidor = params[0];
            String atleta = params[1];
            String languageCode = params[2];

            HttpURLConnection connection = null;
            urlServidor = urlServidor + "tieneEntrenador.php";
            try {
                URL url = new URL(urlServidor);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);

                // Parámetros de la consulta
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("atleta", atleta)
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

                // Devolver la respuesta como cadena
                return response.toString();
            } catch (IOException e) {
                e.printStackTrace();
                return null; // Retorna null en caso de error
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.i("entrenador",result);
            // Analizar la respuesta del servidor
            JsonObject jsonObject = JsonParser.parseString(result).getAsJsonObject();

            // Verificar si el usuario tiene entrenador
            boolean tieneEntrenador = jsonObject.get("tieneEntrenador").getAsBoolean();
            if (!tieneEntrenador) {
                // El usuario no tiene un entrenador asignado, podemos agregarlo
                EditText et = findViewById(R.id.NombreAgregarAtleta);
                String usuario = et.getText().toString();
                lista.clear();
                new addAtleta().execute(urlServidor, usuario,entrenador, languageCode);
                new getAtletas().execute(urlServidor, entrenador, languageCode);
                et.setText("");
            } else {
                // El usuario ya tiene un entrenador asignado
                dialogoAlerta dialogo = new dialogoAlerta();
                dialogo.setMensaje("El usuario ya tiene un entrenador");
                dialogo.show(getSupportFragmentManager(), "etiqueta2");
            }


        }
    }


    //// comprobar si el atleta tiene entrenador ////


    //// añadir un atleta a un entrenador ////
    private class addAtleta extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String urlServidor = params[0];
            String atleta = params[1];
            String entrenador = params[2];
            String languageCode = params [3];
            HttpURLConnection connection = null;
            urlServidor = urlServidor + "addAtletaEntrenador.php";
            try {
                URL url = new URL(urlServidor);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);

                // Parámetros de la consulta
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("atleta", atleta)
                        .appendQueryParameter("entrenador", entrenador)
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
            eladaptador.notifyDataSetChanged();
        }
    }

    //// añadir un atleta a un entrenador ////

    //// borrar un atleta a un entrenador ////

    private class removeAtletas extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String urlServidor = params[0];
            String atleta = params[1];
            String languageCode = params [2];
            HttpURLConnection connection = null;
            urlServidor = urlServidor + "removeAtleta.php";
            try {
                URL url = new URL(urlServidor);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);

                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("atleta", atleta)
                        .appendQueryParameter("lang", languageCode);
                String parametrosURL = builder.build().getEncodedQuery();

                OutputStream outputStream = connection.getOutputStream();
                outputStream.write(parametrosURL.getBytes());
                outputStream.flush();
                outputStream.close();

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
            Log.i("remove",result);
        }
    }

    //// borrar un atleta a un entrenador ////

    //// obtener lista de atletas de un entrenador ////

    private class getAtletas extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String urlServidor = params[0];
            String entrenador = params[1];
            String languageCode = params [2];
            HttpURLConnection connection = null;
            urlServidor = urlServidor+"getAtletas.php";
            try {
                URL url = new URL(urlServidor);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);

                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("entrenador", entrenador)
                        .appendQueryParameter("lang", languageCode);
                String parametrosURL = builder.build().getEncodedQuery();

                OutputStream outputStream = connection.getOutputStream();
                outputStream.write(parametrosURL.getBytes());
                outputStream.flush();
                outputStream.close();

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
            Log.i("JSON recibido", result);
            // Analizar el JSON
            JsonObject jsonObject = JsonParser.parseString(result).getAsJsonObject();

            // Obtener la lista de usuarios
            if (jsonObject.has("usuarios")) {
                for (JsonElement element : jsonObject.getAsJsonArray("usuarios")) {
                    lista.add(element.getAsString());
                }
                eladaptador.notifyDataSetChanged();
            }

        }
    }

    //// obtener lista de atletas de un entrenador ////

}
