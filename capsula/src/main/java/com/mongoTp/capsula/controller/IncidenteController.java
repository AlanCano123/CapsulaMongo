package com.mongoTp.capsula.controller;

import com.mongoTp.capsula.documentos.Incidente;
import com.mongoTp.capsula.dtos.IncidenteResponse;
import com.mongoTp.capsula.dtos.ReporteResponse;
import com.mongoTp.capsula.service.IncidenteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/incidentes")
public class IncidenteController {
    private final IncidenteService incidenteService;

    public IncidenteController(IncidenteService incidenteService) {
        this.incidenteService = incidenteService;
    }

    @PostMapping("/registrar")
    public ResponseEntity<?> registrarIncidente(@RequestBody Map<String, Object> request) {
        try {
            String nombreRuta = (String) request.get("nombreRuta");
            int kilometro = (int) request.get("kilometro");
            String nombreTipoIncidente = (String) request.get("nombreTipoIncidente");
            String comentarios = (String) request.get("comentarios");

            Incidente incidente = incidenteService.registrarIncidente(nombreRuta, kilometro, nombreTipoIncidente, comentarios);
            return ResponseEntity.ok(incidente);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/eliminar/{incidenteId}")
    public ResponseEntity<?> eliminarIncidente(@PathVariable String incidenteId) {
            incidenteService.eliminarIncidente(incidenteId);
            return ResponseEntity.ok(Map.of("mensaje", "Incidente eliminado"));
    }

    @GetMapping("/buscar/{nombreRuta}/{kilometroInicio}")
    public ResponseEntity<IncidenteResponse> obtenerIncidentes(
            @PathVariable String nombreRuta,
            @PathVariable int kilometroInicio) {

        IncidenteResponse response = incidenteService.obtenerIncidentesPorRuta(nombreRuta, kilometroInicio);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/reporte/{nombreRuta}")
    public ResponseEntity<?> obtenerReporteRuta(@PathVariable String nombreRuta) {
            List<ReporteResponse> reporte = incidenteService.generarReporteRuta(nombreRuta);
            return ResponseEntity.ok(reporte);
    }

    @GetMapping("/obtenerTodosAccidentes")
    public List<Incidente> obtenerTodosIncidentes(){
        return incidenteService.obtenerIncidentes();
    }

}
