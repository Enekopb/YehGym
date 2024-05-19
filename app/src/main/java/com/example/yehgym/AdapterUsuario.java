package com.example.yehgym;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.os.AsyncTask;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class AdapterUsuario extends RecyclerView.Adapter<AdapterUsuario.MyViewHolder> {

    private Context contexto;
    private List<ModeloUsuario> lista;

    public AdapterUsuario(Context contexto) {
        this.contexto = contexto;
        this.lista = new ArrayList<>();
    }

    public void add(ModeloUsuario usuario) {
        lista.add(usuario);
    }

    public void clear() {
        lista.clear();
        notifyDataSetChanged();
    }

    public void eliminate(String username) {
        for (int i = 0; i < lista.size(); i++) {
            ModeloUsuario usuario = lista.get(i);
            if (usuario.getUsername().equals(username)) {
                lista.remove(usuario);
                notifyDataSetChanged();
                break;  // Salimos del bucle una vez que se elimina el usuario
            }
        }
    }


    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.lista_usuarios, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        ModeloUsuario usuario = lista.get(position);
        holder.name.setText(usuario.getUsername());
        holder.email.setText(usuario.getEmail());

        if (usuario.hasUnreadMessages()) {
            holder.puntoRojo.setVisibility(View.VISIBLE);
            Log.e("AdapterUsuario", "Sin leer");
        } else {
            holder.puntoRojo.setVisibility(View.GONE);
        }

        // Agregar el OnClickListener al ImageView eliminarAmigo
        holder.eliminarAmigo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new EliminarAmigoTask().execute(usuario.getUsername()); // Llama a execute con un array de String
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(contexto, Chat.class);
                intent.putExtra("id", usuario.getId());
                intent.putExtra("user", usuario.getUsername());
                contexto.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public List<ModeloUsuario> getLista() {
        return lista;
    }

    public boolean has(String username) {
        for (ModeloUsuario usuario : lista) {
            if (usuario.getUsername().equals(username)) {
                return true;
            }
        }
        return false;
    }


    public class MyViewHolder extends  RecyclerView.ViewHolder{
        private TextView name, email;
        private ImageView puntoRojo, eliminarAmigo;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.user);
            email = itemView.findViewById(R.id.email);
            puntoRojo = itemView.findViewById(R.id.punto_rojo);
            eliminarAmigo = itemView.findViewById(R.id.eliminarAmigo);
        }
    }

    public class EliminarAmigoTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            String usuario = strings[0];
            String urlString = "http://146.148.62.83:81/eliminarAmigo.php";
            String languageCode = "en";
            try {
                URL url = new URL(urlString);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                String postData = "user=" + FirebaseAuth.getInstance().getCurrentUser().getDisplayName() + "&amigo=" + usuario + "&lang=" + languageCode;
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
                    return "Error: Código de estado no válido: " + statusCode;
                }
            } catch (IOException e) {
                Log.e("EliminarAmigoTask", "Error en la conexión o procesando JSON: " + e.getMessage());
                return "Error: " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            try {
                JSONObject jsonObject = new JSONObject(result);
                String status = jsonObject.optString("status");
                String message = jsonObject.optString("message");
                if ("success".equals(status)) {
                    Toast.makeText(contexto, message, Toast.LENGTH_SHORT).show();
                    //Recargar MainActivity
                    if (contexto instanceof ListaAmigos) {
                        Log.e("EliminarAmigoTask", "Amigo borrado");
                        lista.clear();
                    }
                } else {
                    Toast.makeText(contexto, message, Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                Log.e("EliminarAmigoTask", "Error al procesar JSON: " + e.getMessage());
            }
        }
    }
}
