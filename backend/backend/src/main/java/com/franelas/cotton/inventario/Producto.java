package com.franelas.cotton.inventario;
 //Esto lo puso Itzel para tomarlo de ejemplo
// Nota: Aún no importamos nada, es una clase simple.

public class Producto {

    // Atributos (Los campos de tu JSON)
    private long id;          // id
    private String nombre;    // nombre
    private String color;     // color
    private String talla;     // "S", "M", "L", "X"
    private double precio;    // precio
    private int stock;        // cantidad en inventario

    // --- Constructor Vacío ---
    // (Librerías como Jackson lo necesitan para crear el objeto desde JSON)
    public Producto() {}

    // --- Constructor Completo (Útil para crear objetos rápido) ---
    public Producto(long id, String nombre, String color, String talla, double precio, int stock) {
        this.id = id;
        this.nombre = nombre;
        this.color = color;
        this.talla = talla;
        this.precio = precio;
        this.stock = stock;
    }

    // --- Getters y Setters ---

    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public String getNombre() {
        return nombre;
    }
    public void setNombre(String nombre) {
        this.nombre = nombre;
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