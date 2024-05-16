package com.example.yehgym;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class dialogoAlerta extends DialogFragment {

    private String mensaje;

    public void setMensaje(String pMensaje) {
        this.mensaje = pMensaje;
    }
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Mensaje de aviso")
                .setMessage(mensaje)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Cierra el diálogo al hacer clic en el botón OK
                        dialog.dismiss();
                    }
                });
        return builder.create();
    }

    
}
