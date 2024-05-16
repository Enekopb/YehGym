package com.example.yehgym;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
        View view;
        if (viewType==VIEW_TYPE_SENT)
        {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_row_sent, parent, false);
        }
        else
        {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_row_received, parent, false);
        }
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MensajeAdaptador.MyViewHolder holder, int position) {
        ModeloMensaje mensaje = lista.get(position);

        // Verificar si es el primer mensaje del día y mostrar la fecha si es así
        if (esPrimerMensajeDelDia(position)) {
            long timestamp = Long.parseLong(mensaje.getMensajeId());
            Date fecha = new Date(timestamp);
            SimpleDateFormat sdfFecha = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String fechaFormateada = sdfFecha.format(fecha);
            holder.textViewFecha.setText(fechaFormateada);  // textViewFecha es el TextView donde se mostrará la fecha
            holder.textViewFecha.setVisibility(View.VISIBLE);  // Mostrar el TextView de la fecha
        } else {
            holder.textViewFecha.setVisibility(View.GONE);  // Ocultar el TextView de la fecha si no es el primer mensaje del día
        }

        if (mensaje.getEnvioId().equals(FirebaseAuth.getInstance().getUid())) {
            holder.textViewEnvioMensaje.setText(mensaje.getMensaje());
            long timestamp = Long.parseLong(mensaje.getMensajeId());
            Date fecha = new Date(timestamp);
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            String fechaFormateada = sdf.format(fecha);
            holder.textViewEnvioMensajeTime.setText(fechaFormateada);
        } else {
            holder.textViewReciboMensaje.setText(mensaje.getMensaje());
            long timestamp = Long.parseLong(mensaje.getMensajeId());
            Date fecha = new Date(timestamp);
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            String fechaFormateada = sdf.format(fecha);
            holder.textViewReciboMensajeTime.setText(fechaFormateada);
        }
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public List<ModeloMensaje> getLista()    {
        return lista;
    }

    // Método auxiliar para verificar si un mensaje es el primero de un nuevo día
    private boolean esPrimerMensajeDelDia(int position) {
        if (position == 0) {
            return true;  // El primer mensaje siempre es el primero del día
        } else {
            ModeloMensaje mensajeActual = lista.get(position);
            ModeloMensaje mensajeAnterior = lista.get(position - 1);

            // Obtener la fecha del mensaje actual y anterior
            long timestampActual = Long.parseLong(mensajeActual.getMensajeId());
            long timestampAnterior = Long.parseLong(mensajeAnterior.getMensajeId());

            // Comparar solo las partes de fecha (día, mes y año) para determinar si es un nuevo día
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String fechaActual = sdf.format(new Date(timestampActual));
            String fechaAnterior = sdf.format(new Date(timestampAnterior));

            return !fechaActual.equals(fechaAnterior);  // Devuelve verdadero si es un nuevo día
        }
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

    public void ordenarMensajesPorFecha() {
        Collections.sort(lista, new Comparator<ModeloMensaje>() {
            @Override
            public int compare(ModeloMensaje mensaje1, ModeloMensaje mensaje2) {
                return mensaje1.getMensajeId().compareTo(mensaje2.getMensajeId());
            }
        });
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewEnvioMensaje, textViewReciboMensaje, textViewEnvioMensajeTime, textViewReciboMensajeTime, textViewFecha;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewEnvioMensaje = itemView.findViewById(R.id.textViewSentMessage);
            textViewReciboMensaje = itemView.findViewById(R.id.textViewReceivedMessage);
            textViewEnvioMensajeTime = itemView.findViewById(R.id.textViewSentMessageTime);
            textViewReciboMensajeTime = itemView.findViewById(R.id.textViewReceivedMessageTime);
            textViewFecha = itemView.findViewById(R.id.textViewFecha);

        }
    }


}
