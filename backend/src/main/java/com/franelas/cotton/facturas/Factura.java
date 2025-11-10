package com.franelas.cotton.facturas;

import java.util.UUID;

public class Factura {
    private String id;
    private String usuario;
    private String pedidoId;
    private double subtotal;
    private double impuestos;
    private double total;
    private String fecha;
    private String estado;

    public Factura() {}

    public Factura(String usuario, String pedidoId, double subtotal, double impuestos, String fecha, String estado) {
        this.id = UUID.randomUUID().toString();
        this.usuario = usuario;
        this.pedidoId = pedidoId;
        this.subtotal = subtotal;
        this.impuestos = impuestos;
        this.total = subtotal + impuestos;
        this.fecha = fecha;
        this.estado = estado;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public String getPedidoId() { return pedidoId; }
    public void setPedidoId(String pedidoId) { this.pedidoId = pedidoId; }

    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
        this.total = this.subtotal + this.impuestos;
    }

    public double getImpuestos() { return impuestos; }
    public void setImpuestos(double impuestos) {
        this.impuestos = impuestos;
        this.total = this.subtotal + this.impuestos;
    }

    public double getTotal() { return total; }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}