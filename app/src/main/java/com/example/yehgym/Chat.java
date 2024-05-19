package com.example.yehgym;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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

import java.util.ArrayList;
import java.util.List;

public class Chat extends AppCompatActivity {

    String recibidorId, recibidorNombre, envioRoom, recibidorRoom;
    DatabaseReference dbRecibidor, dbEnvio, dbUsuario;
    ImageView envioIcono, toolbarBackIcon;
    EditText mensajeTexto;
    RecyclerView recyclerView;
    MensajeAdaptador mensajeAdaptador;
    private boolean isInForeground = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat);
        Toolbar toolbar = findViewById(R.id.toolbarMensaje);

        dbUsuario = FirebaseDatabase.getInstance().getReference("users");
        recibidorId = getIntent().getStringExtra("id");
        recibidorNombre = getIntent().getStringExtra("user");
        toolbar.setTitle(recibidorNombre);
        if (recibidorNombre!=null){
            envioRoom = FirebaseAuth.getInstance().getCurrentUser().getDisplayName()+recibidorNombre;
            recibidorRoom = recibidorNombre+FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        }
        toolbarBackIcon = findViewById(R.id.toolbar_icon);
        toolbarBackIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
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
                    Log.d("Chat", "Id: " + mensajeActual.getEnvioId() + " User" + FirebaseAuth.getInstance().getUid());
                    // Marcar como leído si el mensaje no ha sido leído
                    if (!mensajeActual.isRead() && isInForeground && !mensajeActual.getEnvioId().equals(FirebaseAuth.getInstance().getUid())) {
                        dbRecibidor.child(mensajeActual.getMensajeId()).child("read").setValue(true);
                    }

                }
                mensajeAdaptador.clear();
                for (ModeloMensaje mens:mensajes){
                    mensajeAdaptador.add(mens);
                }
                mensajeAdaptador.notifyDataSetChanged();
                recyclerView.scrollToPosition(mensajeAdaptador.getItemCount() - 1);
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

    @Override
    protected void onResume() {
        super.onResume();
        isInForeground = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        isInForeground = false;
    }


    private void EnviarMensaje(String mensaje) {
        String idMensaje = String.valueOf(System.currentTimeMillis()); // Obtener la fecha actual en milisegundos como identificador
        long timestamp = Long.parseLong(idMensaje); // Convertir el idMensaje a long (si es necesario)
        Date fecha = new Date(timestamp);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss:SSS", Locale.getDefault());
        String fechaFormateada = sdf.format(fecha);
        Log.e("Chat", fechaFormateada);
        ModeloMensaje mensajeModelo = new ModeloMensaje(idMensaje, FirebaseAuth.getInstance().getUid(), mensaje, false);
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
    public void menu(View view){
        finish();
    }
}
