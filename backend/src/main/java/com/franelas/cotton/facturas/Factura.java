package com.franelas.cotton.facturas;

public class Factura {

    private long id;
    private long idPedido;
    private String clienteCorreo;

    private String color;
    private String talla;
    private int cantidad;
    private double precioUnitario;

    private double subtotal;
    private double iva;
    private double total;

    private String fechaEmision; // formato YYYY-MM-DD

    public Factura() {
    }

    public Factura(long id,
                   long idPedido,
                   String clienteCorreo,
                   String color,
                   String talla,
                   int cantidad,
                   double precioUnitario,
                   double subtotal,
                   double iva,
                   double total,
                   String fechaEmision) {
        this.id = id;
        this.idPedido = idPedido;
        this.clienteCorreo = clienteCorreo;
        this.color = color;
        this.talla = talla;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal = subtotal;
        this.iva = iva;
        this.total = total;
        this.fechaEmision = fechaEmision;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(long idPedido) {
        this.idPedido = idPedido;
    }

    public String getClienteCorreo() {
        return clienteCorreo;
    }

    public void setClienteCorreo(String clienteCorreo) {
        this.clienteCorreo = clienteCorreo;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getTalla() {
        return talla;
    }

    public void setTalla(String talla) {
        this.talla = talla;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public double getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(double precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public double getIva() {
        return iva;
    }

    public void setIva(double iva) {
        this.iva = iva;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public String getFechaEmision() {
        return fechaEmision;
    }

    public void setFechaEmision(String fechaEmision) {
        this.fechaEmision = fechaEmision;
    }
}
