package com.example.yehgym;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class adaptadorRecycler extends RecyclerView.Adapter<ElViewHolder> {
    private String[] losEntrenamientos;
    private int[] lasImagenes;
    private boolean[] seleccionados;
    private final ActivityResultLauncher<Intent> activityLauncher;

    private String mes;
    private String año;
    private String atleta;
    private String nombreEntrenador;
    private String idioma;

    private int tema;


    public adaptadorRecycler(String[] entrenos, int[] imagenes, ActivityResultLauncher<Intent> activityLauncher,String pMes,String pAño,String pAtleta,String pIdioma,int pTema,String pEntrenador){
        losEntrenamientos=entrenos;
        lasImagenes=imagenes;
        seleccionados = new boolean[entrenos.length];
        this.activityLauncher = activityLauncher;
        this.mes = pMes;
        this.año = pAño;
        this.atleta = pAtleta;
        this.idioma = pIdioma;
        this.tema=pTema;
        this.nombreEntrenador = pEntrenador;
    }
    public ElViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View elLayoutDeCadaItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.dia_entrenamiento,parent,false);
        ElViewHolder evh = new ElViewHolder(elLayoutDeCadaItem,activityLauncher,mes,año,atleta,idioma,tema,nombreEntrenador);
        evh.seleccion = seleccionados;
        return evh;
    }

    public void onBindViewHolder(@NonNull ElViewHolder holder, int position) {
        holder.textoDia.setText(losEntrenamientos[position]);
        holder.imagenPesa.setImageResource(lasImagenes[position]);
    }
    @Override
    public int getItemCount() {
        return losEntrenamientos.length;
    }



}
