package com.example.yehgym;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class MensajeAdaptador extends RecyclerView.Adapter<MensajeAdaptador.MyViewHolder> {

    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;
    private Context contexto;
    private List<ModeloMensaje> lista;

    public MensajeAdaptador(Context contexto) {
        this.contexto = contexto;
        this.lista = new ArrayList<>();
    }

    public void add(ModeloMensaje mensaje) {
        lista.add(mensaje);
        notifyDataSetChanged();
    }

    public void clear() {
        lista.clear();
        notifyDataSetChanged();
    }

    public MensajeAdaptador.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType==VIEW_TYPE_SENT)
        {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_row_sent, parent, false);
            return new MyViewHolder(view);
        }
        else
        {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_row_received, parent, false);
            return new MyViewHolder(view);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull MensajeAdaptador.MyViewHolder holder, int position) {
        ModeloMensaje mensaje = lista.get(position);
        if (mensaje.getEnvioId().equals(FirebaseAuth.getInstance().getUid())){
            holder.textViewEnvioMensaje.setText(mensaje.getMensaje());
        }
        else {
            holder.textViewReciboMensaje.setText(mensaje.getMensaje());
        }
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public List<ModeloMensaje> getLista()    {
        return lista;
    }

    @Override
    public int getItemViewType(int position) {
        if (lista.get(position).getEnvioId().equals(FirebaseAuth.getInstance().getUid())){
            return VIEW_TYPE_SENT;
        }
        else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    public class MyViewHolder extends  RecyclerView.ViewHolder{
        private TextView textViewEnvioMensaje, textViewReciboMensaje;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewEnvioMensaje = itemView.findViewById(R.id.textViewSentMessage);
            textViewReciboMensaje = itemView.findViewById(R.id.textViewReceivedMessage);
        }
    }
}
