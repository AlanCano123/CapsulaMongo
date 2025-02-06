package com.mongoTp.capsula.dtos;

public class ReporteResponse {

    private String tramo;
    private int totalGravedad;

    public ReporteResponse() {
    }

    public ReporteResponse(int id, int totalGravedad) {
        this.tramo = (id * 100) + "-" + ((id + 1) * 100) + " KM";
        this.totalGravedad = totalGravedad;
    }

    public String getTramo() {
        return tramo;
    }

    public int getTotalGravedad() {
        return totalGravedad;
    }

}
