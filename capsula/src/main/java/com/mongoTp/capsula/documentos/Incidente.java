package com.mongoTp.capsula.documentos;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "incidentes")
public class Incidente {
    @Id
    private String id;

    @DBRef
    @JsonBackReference
    private Ruta ruta;

    private int kilometro;

    @DBRef
    private TipoIncidente tipoIncidente;;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    private String comentarios;

    public Incidente(String id, Ruta rutaId, int kilometro, TipoIncidente tipoIncidente, LocalDateTime timestamp, String comentarios) {
        this.id = id;
        this.ruta = rutaId;
        this.kilometro = kilometro;
        this.tipoIncidente = tipoIncidente;
        this.timestamp = timestamp;
        this.comentarios = comentarios;
    }

    public Incidente() {
    }

    public Incidente(String number, int i, TipoIncidente bache, String s, String comentario) {
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Ruta getRuta() {
        return ruta;
    }

    public void setRuta(Ruta rutaId) {
        this.ruta = rutaId;
    }

    public int getKilometro() {
        return kilometro;
    }

    public void setKilometro(int kilometro) {
        this.kilometro = kilometro;
    }

    public TipoIncidente getTipoIncidente() {
        return tipoIncidente;
    }

    public void setTipoIncidente(TipoIncidente tipoIncidente) {
        this.tipoIncidente = tipoIncidente;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getComentarios() {
        return comentarios;
    }

    public void setComentarios(String comentarios) {
        this.comentarios = comentarios;
    }
}