package com.mongoTp.capsula.dtos;

import com.mongoTp.capsula.documentos.Incidente;
import com.mongoTp.capsula.documentos.Ruta;

import java.util.List;

public class IncidenteResponse {
    private List<Incidente> incidentes;
    private List<RutaResponse> intersecciones;

    public IncidenteResponse() {
    }

    public IncidenteResponse(List<Incidente> incidentes, List<RutaResponse> intersecciones) {
        this.incidentes = incidentes;
        this.intersecciones = intersecciones;
    }

    public List<Incidente> getIncidentes() {
        return incidentes;
    }

    public List<RutaResponse> getIntersecciones() {
        return intersecciones;
    }
}