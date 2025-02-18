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
    private final Map<String, Integer> rutaConsultaContador = new ConcurrentHashMap<>();

    private static final int UMBRAL_CONSULTAS = 5;
    private static final Duration CACHE_TIEMPO_NORMAL = Duration.ofHours(1);
    private static final Duration CACHE_TIEMPO_INTENSO = Duration.ofMinutes(10);

    public IncidenteService(IncidenteRepository incidenteRepository, RutaRepository rutaRepository, TipoIncidenteRepository tipoIncidenteRepository, RedisTemplate<String, Object> redisTemplate) {
        this.incidenteRepository = incidenteRepository;
        this.rutaRepository = rutaRepository;
        this.tipoIncidenteRepository = tipoIncidenteRepository;
        this.redisTemplate = redisTemplate;
    }

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
        invalidarCache(nombreRuta);
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

        if (consultasPrevias >= UMBRAL_CONSULTAS) {
            redisTemplate.expire(cacheKey, CACHE_TIEMPO_INTENSO);
        }

        // Intentar obtener incidentes desde caché
        IncidenteResponse cacheado = (IncidenteResponse) redisTemplate.opsForValue().get(cacheKey);
        if (cacheado != null) {
            return cacheado;
        }

        Ruta ruta = rutaRepository.findByNombre(nombreRuta)
                .orElseThrow(() -> new RuntimeException("Ruta no encontrada: " + nombreRuta));

        int kmFin = kilometroInicio + 100;
        List<Incidente> incidentes = incidenteRepository.buscarIncidentesPorRuta(ruta.getId(), kilometroInicio, kmFin);

        List<RutaResponse> interseccionesPersonalizadas = ruta.getIntersecciones().stream()
                .map(interseccion -> new RutaResponse(
                        interseccion.getNombre(),
                        interseccion.getOrigen(),
                        interseccion.getDestino(),
                        interseccion.getDistancia()
                ))
                .collect(Collectors.toList());

        IncidenteResponse response = new IncidenteResponse(incidentes, interseccionesPersonalizadas);

        redisTemplate.opsForValue().set(cacheKey, response, CACHE_TIEMPO_NORMAL);
        redisTemplate.opsForValue().increment(contadorKey);

        return response;
    }

    public List<ReporteResponse> generarReporteRuta(String nombreRuta) {
        String cacheKey = "reporte:" + nombreRuta;
        Integer consultasPrevias = rutaConsultaContador.getOrDefault(nombreRuta, 0);

        if (consultasPrevias >= UMBRAL_CONSULTAS) {
            redisTemplate.expire(cacheKey, CACHE_TIEMPO_INTENSO);
        }

        List<ReporteResponse> cacheado = (List<ReporteResponse>) redisTemplate.opsForValue().get(cacheKey);
        if (cacheado != null) {
            return cacheado;
        }

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

        redisTemplate.opsForValue().set(cacheKey, reporte, CACHE_TIEMPO_NORMAL);
        rutaConsultaContador.put(nombreRuta, consultasPrevias + 1);

        return reporte;
    }

    public void invalidarCache(String rutaId) {
        redisTemplate.delete("contador:" + rutaId);
        redisTemplate.delete("incidentes:" + rutaId);
        redisTemplate.delete("reporte:" + rutaId);
        rutaConsultaContador.remove(rutaId);
    }

    public List<Incidente> obtenerIncidentes() {
        return incidenteRepository.findAll();
    }
}
