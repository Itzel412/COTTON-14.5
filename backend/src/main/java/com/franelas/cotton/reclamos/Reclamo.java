package com.franelas.cotton.reclamos;

import java.util.UUID;

public class Reclamo {
    private String id;
    private String usuario;
    private String pedidoId;
    private String motivo;
    private String descripcion;
    private String estado;
    private String fecha;

    public Reclamo() {}

    public Reclamo(String usuario, String pedidoId, String motivo, String descripcion, String fecha) {
        this.id = UUID.randomUUID().toString();
        this.usuario = usuario;
        this.pedidoId = pedidoId;
        this.motivo = motivo;
        this.descripcion = descripcion;
        this.estado = "ABIERTO";
        this.fecha = fecha;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public String getPedidoId() { return pedidoId; }
    public void setPedidoId(String pedidoId) { this.pedidoId = pedidoId; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }
}