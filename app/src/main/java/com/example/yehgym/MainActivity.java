package com.example.yehgym;


import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.net.Uri;
import android.os.AsyncTask;
import android.view.MenuItem;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private String dia ;
    private String mes;
    private String año;
    private String fecha;

    private String idioma;

    private int tema;

    private SQLiteDatabase db;
    private String nombreAtleta;
    private String nombreEntrenador;
    private String[] diaMes;
    private int[] imagenes ;
    private String urlServidor = "http://146.148.62.83:81/";
    private String languageCode = "es";
    private adaptadorRecycler eladaptador ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            //idioma = extras.getString("idioma");
            String pMes= extras.getString("mes");
            String pAño= extras.getString("año");
            String pNombre = extras.getString("atleta");
            String pEntrenador = extras.getString("entrenador");
            //tema = extras.getInt("tema");

            //setTheme(tema);
            if(pMes != null && pAño != null){
                if (pMes.length() == 2) { // Verificar si pMes tiene dos dígitos
                    mes = pMes;
                } else if (pMes.length() == 1) { // Si pMes tiene un solo dígito, agregar un 0 al principio
                    mes = "0" + pMes;
                }
                año=pAño;
            }
            nombreAtleta = pNombre;
            nombreEntrenador = pEntrenador;
            fecha = "00/" + mes + "/"+ año;
        }
        /*
        if(idioma != null){
            cambiarIdioma(idioma);
        }*/
        setContentView(R.layout.activity_main);
        solicitarPermisosNotificaciones(); //Pedimos permisos, si todavia no tiene

        RecyclerView laLista = findViewById(R.id.rv);
        //Conseguir y poner fecha
        if(mes==null || año==null) {
            fecha =consigueFecha();
        }
        String[] fechaLista = fecha.split("/");
        mes = fechaLista[1];
        año = fechaLista[2];
        TextView tv = findViewById(R.id.fecha);
        Log.i("mes y año",mes + " " + año);
        tv.setText(obtenerNombreMes(Integer.parseInt(mes)) + " "+ año);
        diaMes = new String[obtenerDiasMes(Integer.parseInt(mes),Integer.parseInt(año))];
        for (int i = 0; i < diaMes.length ; i++) {
            diaMes[i] = String.valueOf(i + 1);
        }

        imagenes = new int[diaMes.length];
        for (int i = 1; i < diaMes.length + 1 ; i++){
            Log.d("entreno",año+mes+i);
            String d;
            if (i <10){
                d = "0"+i;
            }else{
                d = Integer.toString(i);
            }
            new hayEntreno(i).execute(urlServidor, año + "-" + mes + "-" + d, nombreAtleta,languageCode);
        }
        Log.i("hayEntre img", Arrays.toString(imagenes));
        //Conseguir y poner fecha

        ImageButton btAbrirNav = findViewById(R.id.btAbrirNavegador);
        btAbrirNav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?client=firefox-b-d&q=gimnasios+cerca+de+mi"));
                startActivity(i);
            }
        });

        //Botones navegación
        Button botonAnt = findViewById(R.id.bt_ant);
        botonAnt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int MesNum = Integer.parseInt(mes) - 1;
                int añoNum = Integer.parseInt(año);
                if(MesNum <= 0){
                    añoNum --;
                    MesNum = 12;

                }
                Intent i = getIntent();
                i.putExtra("idioma",idioma);
                i.putExtra("mes",Integer.toString(MesNum));
                i.putExtra("año",Integer.toString(añoNum));
                i.putExtra("atleta",nombreAtleta);
                i.putExtra("tema",tema);
                finish();
                startActivity(i);
            }
        });
        Button botonSig = findViewById(R.id.bt_sig);
        botonSig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int MesNum = Integer.parseInt(mes) + 1;
                int añoNum = Integer.parseInt(año);
                if(MesNum >= 13){
                    añoNum ++;
                    MesNum = 1;

                }
                Intent i = getIntent();
                i.putExtra("idioma",idioma);
                i.putExtra("mes",Integer.toString(MesNum));
                i.putExtra("año",Integer.toString(añoNum));
                i.putExtra("atleta",nombreAtleta);
                i.putExtra("tema",tema);
                finish();
                startActivity(i);

            }
        });
        //Botones navegación

        eladaptador = new adaptadorRecycler(diaMes,imagenes,activityLauncher,mes,año,nombreAtleta,idioma,tema,nombreEntrenador);
        laLista.setAdapter(eladaptador);

        RecyclerView.LayoutManager elLayoutRejillaIgual= new GridLayoutManager(this,7,GridLayoutManager.VERTICAL,false);
        laLista.setLayoutManager(elLayoutRejillaIgual);


    }

    private final ActivityResultLauncher<Intent> activityLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    // para recrear la actividad al agregar ejercicios en un dia en concreto
                    recreate();
                }
            });
    private void solicitarPermisosNotificaciones(){
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)!= PackageManager.PERMISSION_GRANTED) {
            //PEDIR EL PERMISO
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 11);
        }
    }
    private String consigueFecha(){
        Date fechaActual = new Date();
        SimpleDateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy");
        String fechaFormateada = formatoFecha.format(fechaActual);
        return fechaFormateada;
    }

    private String obtenerNombreMes(int numeroMes) {
        Locale locale = new Locale("es", "ES");
        DateFormatSymbols symbols = new DateFormatSymbols(locale);
        String[] nombresMeses = symbols.getMonths();
        return nombresMeses[numeroMes - 1];
    }

    private int obtenerDiasMes(int mes, int año) {
        YearMonth añoMes = YearMonth.of(año, mes);
        return añoMes.lengthOfMonth();
    }

    public void onChangeThemeClick(MenuItem item) {
        int currentTheme = tema; // tema actual

        if (currentTheme == R.style.Calendario_tema1) {
            currentTheme = R.style.Calendario_tema2;

        } else {
            currentTheme = R.style.Calendario_tema1;
        }
        getIntent().putExtra("idioma",idioma);
        getIntent().putExtra("tema",currentTheme);
        finish();
        startActivity(getIntent());
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

    private class hayEntreno extends AsyncTask<String, Void, String> {
        private int i;

        public hayEntreno(int i) {
            this.i = i;
        }
        @Override
        protected String doInBackground(String... params) {
            String urlServidor = params[0];
            String fecha = params[1];
            String nombre = params[2];
            String languageCode = params[3];
            HttpURLConnection connection = null;
            urlServidor = urlServidor + "hayEntreno.php";
            try {
                URL url = new URL(urlServidor);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);

                // Parámetros de la consulta
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("fecha", fecha)
                        .appendQueryParameter("nombre", nombre)
                        .appendQueryParameter("lang", languageCode);
                String parametrosURL = builder.build().getEncodedQuery();

                // Enviar datos al servidor
                connection.getOutputStream().write(parametrosURL.getBytes());

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
            }catch (IOException e) {
                e.printStackTrace();
                return e.getMessage();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }

        @Override
        protected void onPostExecute(String hayEntreno) {
            super.onPostExecute(hayEntreno);

            try {
                // Convertir la cadena JSON a un objeto JSONObject
                JSONObject jsonObject = new JSONObject(hayEntreno);

                // Obtener el valor booleano de la clave "hay_entreno"
                boolean entreno = jsonObject.getBoolean("hay_entreno");

                // Utilizar el valor booleano en tu lógica
                if (entreno) {
                    imagenes[i - 1] = R.drawable.pesa;
                } else {
                    imagenes[i - 1] = R.drawable.descanso;
                }
                if(i == diaMes.length){
                    eladaptador.notifyDataSetChanged();
                }
            } catch (JSONException e) {
                // Manejar la excepción si ocurre un error al procesar el JSON
                e.printStackTrace();
            }

        }
    }
}