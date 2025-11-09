package com.franelas.cotton.pedidos;

import java.util.UUID;

public class Pedido {
    private String id;          // Identificador único
    private String usuario;     // Usuario que realiza el pedido
    private long idProducto;    // ID del producto (referencia del inventario)
    private String nombre;      // Nombre del producto
    private String color;       // Color de la prenda
    private String talla;       // "S", "M", "L", "XL"
    private int cantidad;       // Máximo 10 unidades
    private double precioUnitario;
    private double total;       // Calculado automáticamente
    private String fecha;       // Fecha del pedido

    // --- Getters y Setters ---
    public String getId() { return id; }
    public String getUsuario() { return usuario; }
    public long getIdProducto() { return idProducto; }
    public String getNombre() { return nombre; }
    public String getColor() { return color; }
    public String getTalla() { return talla; }
    public int getCantidad() { return cantidad; }
    public double getPrecioUnitario() { return precioUnitario; }
    public double getTotal() { return total; }
    public String getFecha() { return fecha; }

    public void setId(String id) { this.id = id; }
    public void setUsuario(String usuario) { this.usuario = usuario; }
    public void setIdProducto(long idProducto) { this.idProducto = idProducto; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setColor(String color) { this.color = color; }
    public void setTalla(String talla) { this.talla = talla; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    public void setPrecioUnitario(double precioUnitario) { this.precioUnitario = precioUnitario; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    public Pedido(String usuario, long idProducto, String nombre, String color, String talla, int cantidad, double precioUnitario, String fecha) {
        this.id = UUID.randomUUID().toString();
        this.usuario = usuario;
        this.idProducto = idProducto;
        this.nombre = nombre;
        this.color = color;
        this.talla = talla;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.total = cantidad * precioUnitario;
        this.fecha = fecha;
    }

}
