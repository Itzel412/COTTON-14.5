package com.franelas.cotton.reclamos;

public class Reclamo {

    private long id;
    private String usuario;
    private String titulo;
    private String descripcion;
    private String fechaCreacion;
    private String estado;

    public Reclamo() {}

    public Reclamo(long id, String usuario, String titulo, String descripcion,
                   String fechaCreacion, String estado) {
        this.id = id;
        this.usuario = usuario;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.fechaCreacion = fechaCreacion;
        this.estado = estado;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(String fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}
