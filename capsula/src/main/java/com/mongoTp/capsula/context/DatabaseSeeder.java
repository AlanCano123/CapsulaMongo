package com.mongoTp.capsula.context;
import com.mongoTp.capsula.documentos.Incidente;
import com.mongoTp.capsula.documentos.Ruta;
import com.mongoTp.capsula.documentos.TipoIncidente;
import com.mongoTp.capsula.repositoy.IncidenteRepository;
import com.mongoTp.capsula.repositoy.RutaRepository;
import com.mongoTp.capsula.repositoy.TipoIncidenteRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
@Component
public class DatabaseSeeder implements CommandLineRunner {

    private final RutaRepository rutaRepository;
    private final TipoIncidenteRepository tipoIncidenteRepository;
    private final IncidenteRepository incidenteRepository;

    public DatabaseSeeder(RutaRepository rutaRepository, TipoIncidenteRepository tipoIncidenteRepository, IncidenteRepository incidenteRepository) {
        this.rutaRepository = rutaRepository;
        this.tipoIncidenteRepository = tipoIncidenteRepository;
        this.incidenteRepository = incidenteRepository;
    }

    @Override
    public void run(String... args) {

        if (rutaRepository.count() == 0&&tipoIncidenteRepository.count()==0) {
            Ruta ruta1 = new Ruta("Ruta1", "Ciudad A", "Ciudad B", 150);
            Ruta ruta2 = new Ruta("Ruta2", "Ciudad B", "Ciudad C", 200);
            Ruta ruta3 = new Ruta("Ruta3", "Ciudad C", "Ciudad D", 120);
            Ruta ruta4 = new Ruta("Ruta4", "Ciudad D", "Ciudad E", 180);
            Ruta ruta5 = new Ruta("Ruta5", "Ciudad A", "Ciudad E", 300);
            rutaRepository.saveAll(Arrays.asList(ruta1, ruta2, ruta3, ruta4, ruta5));


            ruta1.setIntersecciones(List.of(ruta2));
            ruta2.setIntersecciones(Arrays.asList(ruta1, ruta3));
            ruta3.setIntersecciones(List.of(ruta2));
            ruta4.setIntersecciones(List.of(ruta5));
            ruta5.setIntersecciones(List.of(ruta4));


            rutaRepository.saveAll(Arrays.asList(ruta1, ruta2, ruta3, ruta4, ruta5));

            TipoIncidente Fotomulta = new TipoIncidente("Fotomulta",1);
            TipoIncidente ControlPolicial = new TipoIncidente("ControlPolicial",2);
            TipoIncidente Accidente = new TipoIncidente("Accidente",3);
            TipoIncidente Bache = new TipoIncidente("Bache",1);
            TipoIncidente Incendio = new TipoIncidente("Incendio",3);
            TipoIncidente Neblina  = new TipoIncidente("Neblina",1);
            TipoIncidente Animales = new TipoIncidente("Animales",2);
            TipoIncidente Piquete = new TipoIncidente("Piquete",2);

            tipoIncidenteRepository.saveAll(Arrays.asList(Fotomulta,ControlPolicial,Accidente,Bache,Incendio,Neblina,Animales,Piquete));

            Incidente incidente1 = new Incidente("67a4d1c2579e421babdcb29d", ruta1, 15, Piquete, LocalDateTime.now(), "Corte de Ruta");
            incidenteRepository.saveAll(Arrays.asList(incidente1));
            System.out.println("Rutas precargadas en MongoDB");
        }
    }
}