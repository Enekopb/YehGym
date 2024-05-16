package com.example.yehgym;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class menuFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.menufragment, container, false);
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("config_tema", MODE_PRIVATE);
        String temaGuardado = sharedPreferences.getString("tema", "DEFAULT");


        // Aplicar el tema correspondiente
        if (temaGuardado.equals("DEFAULT")) {
            requireActivity().setTheme(R.style.AppThemeLight);
            view.setBackgroundColor(Color.WHITE);
        } else {
            requireActivity().setTheme(R.style.AppThemeDark);
            view.setBackgroundColor(Color.BLACK);
        }

        Button botonCalendario = view.findViewById(R.id.calendario);
        botonCalendario.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calendarioFragment cf = new calendarioFragment();
                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                FragmentTransaction replace = transaction.replace(R.id.container, cf);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) Button botonMapa = view.findViewById(R.id.mapa);
        botonMapa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapaFragment mf = new mapaFragment();
                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                FragmentTransaction replace = transaction.replace(R.id.container, mf);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) Button botonChat = view.findViewById(R.id.chats);
        botonChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(requireActivity(), MainActivity.class);
                startActivity(intent);
            }
        });
        return view;
    }
}