package com.franelas.cotton.inventario;

public class Producto {

    private String color;
    private String talla;
    private double precio;
    private int stock;

    public Producto() {
    }

    public Producto(String color, String talla, double precio, int stock) {
        this.color = color;
        this.talla = talla;
        this.precio = precio;
        this.stock = stock;
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

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }
}
