package com.mongoTp.capsula.documentos;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.annotation.Id;
@Document(collection = "tipos_incidente")
public class TipoIncidente {
    @Id
    private String id;
    private String nombre;
    private int importancia;

    public TipoIncidente(String nombre, int importancia) {
        this.nombre = nombre;
        this.importancia = importancia;
    }

    public TipoIncidente() {
    }

    public TipoIncidente(String number, String bache, int i) {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getImportancia() {
        return importancia;
    }

    public void setImportancia(int importancia) {
        this.importancia = importancia;
    }
}