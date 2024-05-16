package com.example.yehgym;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import java.util.Timer;
import java.util.TimerTask;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.Timer;
import java.util.TimerTask;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    AdapterUsuario adapter;
    DatabaseReference db;
    ImageView amigoIcono;
    EditText amigoTexto;
    private String username;
    private String uID;
    private Timer timer;



    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        Toolbar toolbar = findViewById(R.id.toolbar);
        username = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        uID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Log.e("Main", "Username: " + username);
        Intent intent = getIntent();
        if (intent != null) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                username = extras.get("name").toString();
                Log.d("MainActivity", "Todos los extras del Intent:");
                for (String key : extras.keySet()) {
                    Object value = extras.get(key);
                    Log.d("MainActivity", String.format("%s : %s (%s)", key, value.toString(), value.getClass().getSimpleName()));
                }
            } else {
                Log.e("MainActivity", "No hay extras en el Intent");
            }
        } else {
            Log.e("MainActivity", "El Intent es nulo");
        }

        toolbar.setTitle(username);

        // Iniciar el Timer para obtener amigos cada 5 segundos
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                new ObtenerAmigosTask().execute(username, "en");
            }
        }, 0, 5000); // Iniciar de inmediato y repetir cada 5 segundos

        adapter = new AdapterUsuario(this);
        recyclerView = findViewById(R.id.view);
        amigoIcono = findViewById(R.id.iconoAmigoEnvio);
        amigoTexto = findViewById(R.id.amigo);

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Iniciar el Timer para obtener amigos cada 5 segundos
        amigoIcono.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mensaje = amigoTexto.getText().toString();
                if (mensaje.trim().length() > 0){
                    if (mensaje.equals(username))
                    {
                        Toast.makeText(MainActivity.this, "No se puede añadir a uno mismo", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        new AmigoTask().execute(mensaje, "en");
                    }
                }
                else
                {
                    Toast.makeText(MainActivity.this, "La solicitud no puede estar vacía", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Método para cargar la lista de chats (llamado cuando llega un nuevo mensaje)
    void cargarListaChats() {
        // Lógica para cargar la lista de chats nuevamente
        new ObtenerAmigosTask().execute(username, "en");
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Detener la ejecución del Timer al pausar la actividad
        if (timer != null) {
            timer.cancel(); // Cancelar el Timer
            timer = null; // Asignar null para indicar que se ha detenido
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Si el Timer es nulo (no se inicializó o se canceló), lo volvemos a iniciar
        if (timer == null) {
            timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    new ObtenerAmigosTask().execute(username, "en");
                }
            }, 0, 5000); // Iniciar de inmediato y repetir cada 5 segundos
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("MainActivity", "Destroy");
        // Detener el Timer al destruir la actividad si aún no se ha detenido
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.logout) {
            if (timer != null) {
                timer.cancel(); // Cancelar el Timer
            }
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(MainActivity.this, Login.class));
            finish();
            return true;
        }
        return false;
    }

    // En el método actualizarListaAmigos:
    private void actualizarListaAmigos(List<String> friendList) {
        if (friendList == null){
            adapter.clear();
        }
        else {
            db = FirebaseDatabase.getInstance().getReference("users");
            db.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot data : snapshot.getChildren()) {
                        ModeloUsuario user = data.getValue(ModeloUsuario.class);
                        if (user != null && friendList.contains(user.getUsername())) {
                            checkUnreadMessages(user);
                        }
                    }
                    recyclerView.setAdapter(adapter);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("MainActivity", "Error en la base de datos: " + error.getMessage());
                }
            });
        }

    }

    private void checkUnreadMessages(ModeloUsuario user) {
        String currentUserId = username;
        if(!adapter.has(user.getUsername())){
            adapter.add(user);
        }
        DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("chats")
                .child(user.getUsername() + currentUserId);
        chatRef.orderByKey().limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean hasUnread = false;
                for (DataSnapshot data : snapshot.getChildren()) {
                    Object readValue = data.child("read").getValue();
                    ModeloMensaje lastMessage = data.getValue(ModeloMensaje.class);
                    if (lastMessage != null && readValue instanceof Boolean && !(Boolean) readValue && !lastMessage.getEnvioId().equals(uID)) {
                        hasUnread = true;
                        user.setHasUnreadMessages(hasUnread);
                        adapter.eliminate(user.getUsername());
                        adapter.add(user);
                        recyclerView.setAdapter(adapter);
                        break;
                    }
                    else {
                        adapter.eliminate(user.getUsername());
                        adapter.add(user);
                        recyclerView.setAdapter(adapter);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("MainActivity", "Error checking unread messages: " + error.getMessage());
            }
        });
    }

    public void recargarDatos() {
        // Aquí puedes recargar los datos de la lista de usuarios
        adapter.clear();
        new ObtenerAmigosTask().execute(username, "en");
    }

    private class AmigoTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String urlString = "http://146.148.62.83:81/añadirAmigo.php";
            String amigo = params[0];
            String languageCode = params[1];
            String usuario = username;
            try {
                URL url = new URL(urlString);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                String postData = "user=" + usuario + "&amigo=" + amigo + "&lang=" + languageCode;
                urlConnection.setDoOutput(true);
                urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                PrintWriter out = new PrintWriter(urlConnection.getOutputStream());
                out.print(postData);
                out.close();
                int statusCode = urlConnection.getResponseCode();
                if (statusCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    bufferedReader.close();
                    return stringBuilder.toString();
                } else {
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
                    JSONObject jsonObject = new JSONObject(result);
                    String status = jsonObject.optString("status");
                    String message = jsonObject.optString("message");
                    if ("success".equals(status)) {
                        Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                        new ObtenerAmigosTask().execute(username, "en");
                    } else {
                        Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Log.e("LoginTask", "Error al procesar JSON: " + e.getMessage());
                    Toast.makeText(MainActivity.this, R.string.server_error, Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(MainActivity.this, R.string.con_error, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class ObtenerAmigosTask extends AsyncTask<String, Void, List<String>> {

        @Override
        protected List<String> doInBackground(String... params) {
            String urlString = "http://146.148.62.83:81/getAmigos.php";
            String usuario = params[0];
            String languageCode = params[1];
            try {
                URL url = new URL(urlString);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                String postData = "user=" + usuario + "&lang=" + languageCode;
                urlConnection.setDoOutput(true);
                urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                PrintWriter out = new PrintWriter(urlConnection.getOutputStream());
                out.print(postData);
                out.close();
                int statusCode = urlConnection.getResponseCode();
                if (statusCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    bufferedReader.close();
                    String result = stringBuilder.toString();
                    JSONObject jsonObject = new JSONObject(result);
                    String status = jsonObject.optString("status");
                    if ("success".equals(status)) {
                        JSONArray friends = jsonObject.optJSONArray("friends");
                        List<String> friendList = new ArrayList<>();
                        if (friends != null) {
                            for (int i = 0; i < friends.length(); i++) {
                                friendList.add(friends.getString(i));
                            }
                        }
                        return friendList;
                    } else {
                        String message = jsonObject.optString("message");
                        Log.e("ObtenerAmigosTask", "Error en la respuesta del servidor: " + message);
                        return null;
                    }
                } else {
                    Log.e("ObtenerAmigosTask", "Código de estado no válido: " + statusCode);
                    return null;
                }
            } catch (IOException | JSONException e) {
                Log.e("ObtenerAmigosTask", "Error en la conexión o procesando JSON: " + e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<String> friendList) {
            if (friendList != null) {
                actualizarListaAmigos(friendList);
            } else {
                actualizarListaAmigos(null);
            }
        }
    }
}