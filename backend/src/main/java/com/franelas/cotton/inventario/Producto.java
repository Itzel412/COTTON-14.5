package com.franelas.cotton.inventario;

public class Producto {

    private long id;
    private String nombre;
    private String color;
    private String talla;     // S, M, L, XL
    private double precio;
    private int stock;

    public Producto() {}

    public Producto(long id, String nombre, String color, String talla,
                    double precio, int stock) {
        this.id = id;
        this.nombre = nombre;
        this.color = color;
        this.talla = talla;
        this.precio = precio;
        this.stock = stock;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getTalla() { return talla; }
    public void setTalla(String talla) { this.talla = talla; }

    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
}
