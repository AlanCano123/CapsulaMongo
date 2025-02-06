package com.mongoTp.capsula.documentos;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "rutas")
    public class Ruta {
        @Id
        private String id;
        private String nombre;
    private String origen;
    private String destino;
    private int distancia;

    @JsonBackReference
    @DBRef(lazy = true)
    private List<Ruta> intersecciones;

    public Ruta(String nombre, String origen, String destino, int distancia) {
        this.nombre = nombre;
        this.origen = origen;
        this.destino = destino;
        this.distancia = distancia;
    }

    public Ruta(String id, String nombre, String origen, String destino, int distancia) {
        this.id = id;
        this.nombre = nombre;
        this.origen = origen;
        this.destino = destino;
        this.distancia = distancia;
    }

    public Ruta() {
    }

    public <E> Ruta(String number, String ruta1, ArrayList<E> es) {
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

    public String getDestino() {
        return destino;
    }

    public void setDestino(String destino) {
        this.destino = destino;
    }

    public String getOrigen() {
        return origen;
    }

    public void setOrigen(String origen) {
        this.origen = origen;
    }

    public int getDistancia() {
        return distancia;
    }

    public void setDistancia(int distancia) {
        this.distancia = distancia;
    }

    public List<Ruta> getIntersecciones() {
        return intersecciones;
    }

    public void setIntersecciones(List<Ruta> intersecciones) {
        this.intersecciones = intersecciones;
    }
}