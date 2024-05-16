package com.example.yehgym;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ModeloMensaje {

    private String mensajeId, envioId, mensaje;
    private boolean isRead; // Nuevo campo


    public ModeloMensaje() {
    }

    public ModeloMensaje(String mensajeId, String envioId, String mensaje, boolean isRead) {
        this.mensajeId = mensajeId;
        this.envioId = envioId;
        this.mensaje = mensaje;
        this.isRead = isRead;
    }

    public String getMensajeId() {
        return mensajeId;
    }

    public String getEnvioId() {
        return envioId;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensajeId(String mensajeId) {
        this.mensajeId = mensajeId;
    }

    public void setEnvioId(String envioId) {
        this.envioId = envioId;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }
}
