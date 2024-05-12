package com.example.yehgym;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    AdapterUsuario adapter;
    String usuario;

    DatabaseReference db;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        Toolbar tool = findViewById(R.id.toolbar);
        String username = getIntent().getStringExtra("name");
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

        getSupportActionBar().setTitle(username);

        adapter = new AdapterUsuario(this);
        recyclerView = findViewById(R.id.view);

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        db = FirebaseDatabase.getInstance().getReference("users");
        db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                adapter.clear();
                for (DataSnapshot data: snapshot.getChildren()){
                    String uId = data.getKey();
                    ModeloUsuario user = data.getValue(ModeloUsuario.class);
                    if(user != null && user.getId() != null && !user.getId().equals(FirebaseAuth.getInstance().getUid())){
                        adapter.add(user);
                    }
                }
                List<ModeloUsuario> lista = adapter.getLista();
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.logout)
        {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(MainActivity.this, Login.class));
            finish();
            return true;
        }
        return false;
    }
}
