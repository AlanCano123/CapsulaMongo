package com.mongoTp.capsula.service;

import com.mongoTp.capsula.documentos.Incidente;
import com.mongoTp.capsula.documentos.Ruta;
import com.mongoTp.capsula.documentos.TipoIncidente;
import com.mongoTp.capsula.dtos.IncidenteResponse;
import com.mongoTp.capsula.dtos.ReporteResponse;
import com.mongoTp.capsula.dtos.RutaResponse;
import com.mongoTp.capsula.repositoy.IncidenteRepository;
import com.mongoTp.capsula.repositoy.RutaRepository;
import com.mongoTp.capsula.repositoy.TipoIncidenteRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class IncidenteService {
    private final IncidenteRepository incidenteRepository;
    private final RutaRepository rutaRepository;
    private final TipoIncidenteRepository tipoIncidenteRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    public IncidenteService(IncidenteRepository incidenteRepository, RutaRepository rutaRepository, TipoIncidenteRepository tipoIncidenteRepository,RedisTemplate<String, Object> redisTemplate) {
        this.incidenteRepository = incidenteRepository;
        this.rutaRepository = rutaRepository;
        this.tipoIncidenteRepository = tipoIncidenteRepository;
        this.redisTemplate = redisTemplate;
    }
    private final Map<String, Integer> rutaConsultaContador = new ConcurrentHashMap<>();


    public Incidente registrarIncidente(String nombreRuta, int kilometro, String nombreTipoIncidente, String comentarios) {
        Optional<Ruta> rutaOpt = rutaRepository.findByNombre(nombreRuta);
        Optional<TipoIncidente> tipoOpt = tipoIncidenteRepository.findByNombre(nombreTipoIncidente);

        if (rutaOpt.isEmpty() || tipoOpt.isEmpty()) {
            throw new IllegalArgumentException("Ruta o Tipo de Incidente no encontrado");
        }

        Ruta ruta = rutaOpt.get();
        if (kilometro < 0 || kilometro > ruta.getDistancia()) {
            throw new IllegalArgumentException("Kilómetro fuera del rango de la ruta");
        }

        Incidente incidente = new Incidente();
        incidente.setRuta(ruta);
        incidente.setKilometro(kilometro);
        incidente.setTipoIncidente(tipoOpt.get());
        incidente.setTimestamp(LocalDateTime.now());
        incidente.setComentarios(comentarios);
        // Eliminar cache de incidentes y reportes de esta ruta
        redisTemplate.delete("contador:" + nombreRuta);
        redisTemplate.delete("incidentes:" + nombreRuta + ":" + (kilometro / 100) * 100);
        redisTemplate.delete("reporte:" + nombreRuta);
        return incidenteRepository.save(incidente);
    }

    public void eliminarIncidente(String incidenteId) {
        incidenteRepository.deleteById(incidenteId);
        if(incidenteRepository.existsById(incidenteId)) {
            throw new IllegalArgumentException("Ya se encuentra eliminado");
        }
        redisTemplate.delete("contador:" + incidenteId);
        redisTemplate.delete("incidentes:" + incidenteId);
        redisTemplate.delete("reporte:" + incidenteId);
    }


    public IncidenteResponse obtenerIncidentesPorRuta(String nombreRuta, int kilometroInicio) {
        String cacheKey = "incidentes:" + nombreRuta + ":" + kilometroInicio;
        String contadorKey = "contador:" + nombreRuta;

        // Obtener contador de consultas de Redis
        Integer consultasPrevias = (Integer) redisTemplate.opsForValue().get(contadorKey);
        if (consultasPrevias == null) {
            consultasPrevias = 0;
        }

        if (consultasPrevias >= 5) {
            redisTemplate.expire(cacheKey, Duration.ofMinutes(10));
        }

        // Intentar obtener incidentes desde caché
        IncidenteResponse cacheado = (IncidenteResponse) redisTemplate.opsForValue().get(cacheKey);
        if (cacheado != null) {
            return cacheado;
        }

        // Obtener la ruta de MongoDB
        Ruta ruta = rutaRepository.findByNombre(nombreRuta)
                .orElseThrow(() -> new RuntimeException("Ruta no encontrada: " + nombreRuta));

        // Definir el rango de kilómetros
        int kmFin = kilometroInicio + 100;

        // Consultar incidentes en MongoDB
        List<Incidente> incidentes = incidenteRepository.buscarIncidentesPorRuta(ruta.getId(), kilometroInicio, kmFin);

        // Obtener intersecciones
        List<RutaResponse> interseccionesPersonalizadas = ruta.getIntersecciones().stream()
                .map(interseccion -> new RutaResponse(
                        interseccion.getNombre(),
                        interseccion.getOrigen(),
                        interseccion.getDestino(),
                        interseccion.getDistancia()
                ))
                .collect(Collectors.toList());

        IncidenteResponse response = new IncidenteResponse(incidentes, interseccionesPersonalizadas);

        // Guardar en caché
        redisTemplate.opsForValue().set(cacheKey, response, Duration.ofHours(1));

        // Incrementar contador de consultas en Redis
        redisTemplate.opsForValue().increment(contadorKey);

        return response;
    }

    public List<ReporteResponse> generarReporteRuta(String nombreRuta) {
        String cacheKey = "reporte:" + nombreRuta;
        Integer consultasPrevias = rutaConsultaContador.getOrDefault(nombreRuta, 0);

        if (consultasPrevias >= 5) {
            redisTemplate.expire(cacheKey, Duration.ofMinutes(10));
        }

        // Intentar obtener de caché
        List<ReporteResponse> cacheado = (List<ReporteResponse>) redisTemplate.opsForValue().get(cacheKey);
        if (cacheado != null) {
            return cacheado;
        }

        // Obtener datos de MongoDB
        Optional<Ruta> rutaOpt = rutaRepository.findByNombre(nombreRuta);
        if (rutaOpt.isEmpty()) {
            throw new RuntimeException("Ruta no encontrada");
        }

        List<Map<String, Object>> resultado = incidenteRepository.obtenerReporteRuta(rutaOpt.get().getId());

        List<ReporteResponse> reporte = resultado.stream()
                .map(r -> new ReporteResponse(
                        ((Number) r.get("_id")).intValue(),
                        ((Number) r.get("totalGravedad")).intValue()))
                .collect(Collectors.toList());

        // Guardar en caché por 1 hora si no se ha superado el límite
        redisTemplate.opsForValue().set(cacheKey, reporte, Duration.ofHours(1));

        // Incrementar contador de consultas
        rutaConsultaContador.put(nombreRuta, consultasPrevias + 1);

        return reporte;
 }
    public List<Incidente> obtenerIncidentes() {
         List<Incidente> lista = incidenteRepository.findAll();
         return lista;
    }
}
