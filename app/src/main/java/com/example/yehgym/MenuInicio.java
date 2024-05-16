package com.example.yehgym;

import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

public class MenuInicio extends AppCompatActivity {
    private Fragment menuFragment, configuracionFragment;
    private FrameLayout container;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.menu_inicio);

        container = findViewById(R.id.container);
        menuFragment = new menuFragment();
        configuracionFragment = new configuracionFragment();

        getSupportFragmentManager().beginTransaction().replace(R.id.container, menuFragment).commit();
    }

    public void configurar(View view) {
        // Cambiar al Fragment2 (Configuración) al hacer clic en la opción de configuración en el menú
        getSupportFragmentManager().beginTransaction().replace(R.id.container, configuracionFragment).commit();
    }

    public void menu(View view){
        // Cambiar al Fragment2 (Configuración) al hacer clic en la opción de configuración en el menú
        getSupportFragmentManager().beginTransaction().replace(R.id.container, menuFragment).commit();
    }
}