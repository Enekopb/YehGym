package com.example.yehgym;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class AdapterUsuario extends RecyclerView.Adapter<AdapterUsuario.MyViewHolder> {

    private Context contexto;
    private List<ModeloUsuario> lista;

    public AdapterUsuario(Context contexto) {
        this.contexto = contexto;
        this.lista = new ArrayList<>();
    }

    public void add(ModeloUsuario usuario) {
        lista.add(usuario);
    }

    public void clear() {
        lista.clear();
        notifyDataSetChanged();
    }

    public AdapterUsuario.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.lista_usuarios, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterUsuario.MyViewHolder holder, int position) {
        ModeloUsuario usuario = lista.get(position);
        holder.name.setText(usuario.getUsername());
        holder.email.setText(usuario.getEmail());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(contexto, Chat.class);
                intent.putExtra("id", usuario.getId());
                intent.putExtra("user", usuario.getUsername());
                contexto.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public List<ModeloUsuario> getLista() {
        return lista;
    }

    public class MyViewHolder extends  RecyclerView.ViewHolder{
        private TextView name, email;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.user);
            email = itemView.findViewById(R.id.email);
        }
    }
}
