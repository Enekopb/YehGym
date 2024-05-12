package com.example.yehgym;

import android.content.Intent;
import android.media.metrics.EditingSession;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Chat extends AppCompatActivity {

    String recibidorId, recibidorNombre, envioRoom, recibidorRoom;
    DatabaseReference dbRecibidor, dbEnvio, dbUsuario;
    ImageView envioIcono;
    EditText mensajeTexto;
    RecyclerView recyclerView;
    MensajeAdaptador mensajeAdaptador;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat);

        Toolbar toolbar = findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        dbUsuario = FirebaseDatabase.getInstance().getReference("users");
        recibidorId = getIntent().getStringExtra("id");
        recibidorNombre = getIntent().getStringExtra("user");
        Log.e("Chat", "Id: " + recibidorId.toString() + " User:" + recibidorNombre);
        getSupportActionBar().setTitle(recibidorNombre);
        if (recibidorNombre!=null){
            envioRoom = FirebaseAuth.getInstance().getUid()+recibidorId;
            recibidorRoom = recibidorId+FirebaseAuth.getInstance().getUid();
        }
        envioIcono = findViewById(R.id.iconoMensajeEnvio);
        mensajeAdaptador = new MensajeAdaptador(this);
        recyclerView = findViewById(R.id.recycler);
        mensajeTexto = findViewById(R.id.mensaje);

        recyclerView.setAdapter(mensajeAdaptador);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        dbEnvio = FirebaseDatabase.getInstance().getReference("chats").child(envioRoom);
        dbRecibidor = FirebaseDatabase.getInstance().getReference("chats").child(recibidorRoom);

        dbEnvio.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<ModeloMensaje> mensajes = new ArrayList<>();
                for (DataSnapshot dataSnapshot:snapshot.getChildren()){
                    ModeloMensaje mensajeActual = dataSnapshot.getValue(ModeloMensaje.class);
                    mensajes.add(mensajeActual);
                }
                mensajeAdaptador.clear();
                for (ModeloMensaje mens:mensajes){
                    mensajeAdaptador.add(mens);
                }
                mensajeAdaptador.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        envioIcono.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mensaje = mensajeTexto.getText().toString();
                if (mensaje.trim().length() > 0){
                    EnviarMensaje(mensaje);
                }
                else
                {
                    Toast.makeText(Chat.this, "El mensaje no puede estar vacio", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void EnviarMensaje(String mensaje) {
        String idMensaje = UUID.randomUUID().toString();
        ModeloMensaje mensajeModelo = new ModeloMensaje(idMensaje, FirebaseAuth.getInstance().getUid(), mensaje);
        mensajeAdaptador.add(mensajeModelo);

        dbEnvio.child(idMensaje).setValue(mensajeModelo)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Chat.this, "Error al enviar mensaje", Toast.LENGTH_SHORT).show();
                    }
                });
        dbRecibidor.child(idMensaje).setValue(mensajeModelo);
        recyclerView.scrollToPosition(mensajeAdaptador.getItemCount() - 1);
        mensajeTexto.setText("");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId()==R.id.logout){
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(Chat.this, Login.class));
            finish();
            return true;
        }
        return false;
    }
}
