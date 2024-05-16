package com.example.yehgym;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

public class ElViewHolder extends RecyclerView.ViewHolder {

    public TextView textoDia;
    public ImageView imagenPesa;

    public boolean[] seleccion;

    private String mes;
    private String año;
    private String atleta;
    private String nombreEntrenador;
    private int tema;

    private String idioma;

    private final ActivityResultLauncher<Intent> activityLauncher;

    public ElViewHolder(@NonNull View itemView, ActivityResultLauncher<Intent> activityLauncher,String pMes,String pAño,String pAtleta,String pIdioma,int pTema,String pEntrenador) {
        super(itemView);
        textoDia = itemView.findViewById(R.id.textoDia);
        imagenPesa = itemView.findViewById(R.id.imagenPesa);
        this.activityLauncher = activityLauncher;
        this.mes = pMes;
        this.año = pAño;
        this.atleta = pAtleta;
        this.idioma = pIdioma;
        this.tema = pTema;
        this.nombreEntrenador = pEntrenador;

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = getAdapterPosition();

                Intent intent = new Intent(itemView.getContext(), entrenamiento.class);
                intent.putExtra("mes", mes);
                intent.putExtra("año", año);
                intent.putExtra("diaSeleccionado", String.format("%02d", position + 1));
                intent.putExtra("atleta", atleta);
                intent.putExtra("entrenador",nombreEntrenador);
                intent.putExtra("idioma",idioma);
                intent.putExtra("tema",tema);

                // Lanza la actividad con el ActivityResultLauncher
                activityLauncher.launch(intent);
            }
        });
    }
}
