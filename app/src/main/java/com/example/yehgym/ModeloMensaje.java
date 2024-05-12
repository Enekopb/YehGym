package com.example.yehgym;

public class ModeloMensaje {

    private String mensajeId, envioId, mensaje;

    public ModeloMensaje() {
    }

    public ModeloMensaje(String mensajeId, String envioId, String mensaje) {
        this.mensajeId = mensajeId;
        this.envioId = envioId;
        this.mensaje = mensaje;
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





}
