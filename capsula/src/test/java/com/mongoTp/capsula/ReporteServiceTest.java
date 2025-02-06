package com.mongoTp.capsula;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.mongoTp.capsula.documentos.Incidente;
import com.mongoTp.capsula.documentos.Ruta;
import com.mongoTp.capsula.documentos.TipoIncidente;
import com.mongoTp.capsula.dtos.IncidenteResponse;
import com.mongoTp.capsula.dtos.ReporteResponse;
import com.mongoTp.capsula.dtos.RutaResponse;
import com.mongoTp.capsula.repositoy.IncidenteRepository;
import com.mongoTp.capsula.repositoy.RutaRepository;
import com.mongoTp.capsula.repositoy.TipoIncidenteRepository;
import com.mongoTp.capsula.service.IncidenteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
public class ReporteServiceTest {


    @InjectMocks
    private IncidenteService incidenteService;

    @Mock
    private IncidenteRepository incidenteRepository;

    @Mock
    private TipoIncidenteRepository tipoIncidenteRepository;

    @Mock
    private RutaRepository rutaRepository;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void testObtenerIncidentesPorRutaDesdeCache() {
        String nombreRuta = "Ruta1";
        int kilometroInicio = 10;
        String cacheKey = "incidentes:" + nombreRuta + ":" + kilometroInicio;

        IncidenteResponse incidenteResponse = new IncidenteResponse(List.of(), List.of());
        when(valueOperations.get(cacheKey)).thenReturn(incidenteResponse);

        IncidenteResponse result = incidenteService.obtenerIncidentesPorRuta(nombreRuta, kilometroInicio);

        assertNotNull(result);
        assertEquals(incidenteResponse, result);
        verify(incidenteRepository, never()).buscarIncidentesPorRuta(any(), anyInt(), anyInt());
    }


    @Test
    void testObtenerIncidentesPorRutaRutaNoEncontrada() {
        String nombreRuta = "RutaInexistente";
        int kilometroInicio = 10;

        when(rutaRepository.findByNombre(nombreRuta)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class,
                () -> incidenteService.obtenerIncidentesPorRuta(nombreRuta, kilometroInicio));

        assertEquals("Ruta no encontrada: RutaInexistente", exception.getMessage());
    }


    @Test
    void testExpiracionCacheSiMasDe5Consultas() {
        String nombreRuta = "Ruta1";
        int kilometroInicio = 10;
        String cacheKey = "incidentes:" + nombreRuta + ":" + kilometroInicio;
        String contadorKey = "contador:" + nombreRuta;

        // Mock de la Ruta con lista vacía de intersecciones para evitar NullPointerException
        Ruta rutaMock = mock(Ruta.class);
        when(rutaMock.getIntersecciones()).thenReturn(Collections.emptyList());
        when(rutaRepository.findByNombre(nombreRuta)).thenReturn(Optional.of(rutaMock));

        // Mock de consultas previas en Redis (5 consultas previas para forzar la expiración)
        when(valueOperations.get(contadorKey)).thenReturn(5);

        // Mock de la consulta a MongoDB
        List<Incidente> incidentes = List.of(new Incidente());
        when(incidenteRepository.buscarIncidentesPorRuta(any(), anyInt(), anyInt())).thenReturn(incidentes);

        // Ejecutar el método
        IncidenteResponse response = incidenteService.obtenerIncidentesPorRuta(nombreRuta, kilometroInicio);

        // Verificar que la respuesta no sea nula
        assertNotNull(response);

        // Verificar que la caché expiró tras 5 consultas
        verify(redisTemplate).expire(eq(cacheKey), eq(Duration.ofMinutes(10)));


        // Verificar que la consulta se realizó a la base de datos y no desde caché
        verify(incidenteRepository).buscarIncidentesPorRuta(any(), anyInt(), anyInt());

        // Verificar que el contador de consultas se incrementó
        verify(valueOperations).increment(contadorKey);
    }


    @Test
    void testGenerarReporteRuta_SinCache_ConsultaMongo() {
        String nombreRuta = "Ruta1";
        String cacheKey = "reporte:" + nombreRuta;

        Ruta ruta = new Ruta("1", "Ruta1", new ArrayList<>());
        List<Map<String, Object>> resultadoMongo = List.of(
                Map.of("_id", 0, "totalGravedad", 50),
                Map.of("_id", 100, "totalGravedad", 30)
        );

        when(redisTemplate.opsForValue().get(cacheKey)).thenReturn(null);
        when(rutaRepository.findByNombre(nombreRuta)).thenReturn(Optional.of(ruta));
        when(incidenteRepository.obtenerReporteRuta(ruta.getId())).thenReturn(resultadoMongo);

        List<ReporteResponse> result = incidenteService.generarReporteRuta(nombreRuta);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(50, result.get(0).getTotalGravedad());
        assertEquals(30, result.get(1).getTotalGravedad());

        verify(redisTemplate.opsForValue()).set(eq(cacheKey), any(List.class), any(Duration.class));
    }

    @Test
    void testGenerarReporteRuta_DesdeCache() {
        String nombreRuta = "Ruta1";
        String cacheKey = "reporte:" + nombreRuta;
        List<ReporteResponse> reporteCacheado = List.of(new ReporteResponse(0, 50), new ReporteResponse(100, 30));

        when(redisTemplate.opsForValue().get(cacheKey)).thenReturn(reporteCacheado);

        List<ReporteResponse> result = incidenteService.generarReporteRuta(nombreRuta);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(50, result.get(0).getTotalGravedad());
        assertEquals(30, result.get(1).getTotalGravedad());

        verify(rutaRepository, never()).findByNombre(anyString());
        verify(incidenteRepository, never()).obtenerReporteRuta(anyString());
    }

    @Test
    void testGenerarReporteRuta_RutaNoEncontrada() {
        String nombreRuta = "RutaInexistente";

        when(redisTemplate.opsForValue().get("reporte:" + nombreRuta)).thenReturn(null);
        when(rutaRepository.findByNombre(nombreRuta)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> incidenteService.generarReporteRuta(nombreRuta));
    }

    @Test
    void testRegistrarIncidente_RutaNoEncontrada() {
        when(rutaRepository.findByNombre("RutaInexistente")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                incidenteService.registrarIncidente("RutaInexistente", 50, "Bache", "Comentario"));
    }

    @Test
    void testRegistrarIncidente_TipoIncidenteNoEncontrado() {
        String nombreRuta = "Ruta1";
        when(rutaRepository.findByNombre(nombreRuta)).thenReturn(Optional.of(new Ruta("1", nombreRuta, String.valueOf(new ArrayList<>()), 200)));
        when(tipoIncidenteRepository.findByNombre("TipoInexistente")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                incidenteService.registrarIncidente(nombreRuta, 50, "TipoInexistente", "Comentario"));
    }

    @Test
    void testRegistrarIncidente_KilometroFueraDeRango() {
        String nombreRuta = "Ruta1";
        Ruta ruta = new Ruta("1", nombreRuta, String.valueOf(new ArrayList<>()), 100);
        TipoIncidente tipoIncidente = new TipoIncidente("1", "Bache", 2);

        when(rutaRepository.findByNombre(nombreRuta)).thenReturn(Optional.of(ruta));
        when(tipoIncidenteRepository.findByNombre("Bache")).thenReturn(Optional.of(tipoIncidente));

        assertThrows(IllegalArgumentException.class, () ->
                incidenteService.registrarIncidente(nombreRuta, 150, "Bache", "Comentario"));
    }
}
