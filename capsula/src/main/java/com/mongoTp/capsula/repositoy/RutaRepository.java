package com.mongoTp.capsula.repositoy;

import com.mongoTp.capsula.documentos.Ruta;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface RutaRepository extends MongoRepository<Ruta, String> {
    @Aggregation(pipeline = {
            "{ '$match': { 'nombre': ?0 } }",
            "{ '$lookup': { " +
                    "'from': 'rutas', " +
                    "'localField': 'intersecciones.$id', " +
                    "'foreignField': '_id', " +
                    "'as': 'intersecciones' " +
                    "} }",
            // Elimina intersecciones dentro de intersecciones para evitar el loop
            "{ '$addFields': { 'intersecciones': { " +
                    "$map: { " +
                    "input: '$intersecciones', " +
                    "as: 'interseccion', " +
                    "in: { " +
                    "_id: '$$interseccion._id', " +
                    "nombre: '$$interseccion.nombre', " +
                    "origen: '$$interseccion.origen', " +
                    "destino: '$$interseccion.destino', " +
                    "distancia: '$$interseccion.distancia' " +
                    "} " +
                    "} " +
                    "} } }",
            "{ '$project': { " +
                    "'_id': 1, " +
                    "'nombre': 1, " +
                    "'origen': 1, " +
                    "'destino': 1, " +
                    "'distancia': 1, " +
                    "'intersecciones': 1 " +
                    "} }"
    })
    Optional<Ruta> findByNombre(String nombre);
}
