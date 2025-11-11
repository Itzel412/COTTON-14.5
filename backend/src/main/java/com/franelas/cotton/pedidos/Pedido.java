package com.franelas.cotton.pedidos;

public class Pedido {
    private long id;
    private String usuario;
    private long idProducto;
    private String nombre;
    private String color;
    private String talla;
    private int cantidad;
    private double precioUnitario;
    private double total;
    private String fecha;

    public Pedido() {}

    public Pedido(long id, String usuario, long idProducto, String nombre,
                  String color, String talla, int cantidad,
                  double precioUnitario, String fecha) {
        this.id = id;
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

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public long getIdProducto() { return idProducto; }
    public void setIdProducto(long idProducto) { this.idProducto = idProducto; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getTalla() { return talla; }
    public void setTalla(String talla) { this.talla = talla; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public double getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(double precioUnitario) { this.precioUnitario = precioUnitario; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }
}
