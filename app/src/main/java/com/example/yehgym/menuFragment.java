package com.example.yehgym;

import android.annotation.SuppressLint;
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
        return view;
    }
}
