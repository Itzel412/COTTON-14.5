package com.franelas.cotton.pedidos;

public class Pedido {
    private long id;
    private String usuario;
    private long idProducto;
    private String color;
    private String talla;
    private int cantidad;
    private double precioUnitario;
    private double total;
    

    private String codigo; 

    public Pedido() {}

    // Getters y Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public long getIdProducto() { return idProducto; }
    public void setIdProducto(long idProducto) { this.idProducto = idProducto; }

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

    // Getter y Setter del nuevo campo
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
}