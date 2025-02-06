package com.mongoTp.capsula.repositoy;
import com.mongoTp.capsula.documentos.TipoIncidente;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface TipoIncidenteRepository extends MongoRepository<TipoIncidente, String> {
    @Aggregation(pipeline = {
            "{ '$match': { 'nombre': ?0 } }",
            "{ '$limit': 1 }"
    })
    Optional<TipoIncidente> findByNombre(String nombre);
}
