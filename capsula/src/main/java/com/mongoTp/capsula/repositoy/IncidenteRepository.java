package com.mongoTp.capsula.repositoy;

import com.mongoTp.capsula.documentos.Incidente;
import com.mongoTp.capsula.documentos.Ruta;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface IncidenteRepository extends MongoRepository<Incidente, String> {

    @Aggregation(pipeline = {
            "{ '$lookup': { 'from': 'rutas', 'localField': 'ruta', 'foreignField': '_id', 'as': 'ruta_info' } }",
            "{ '$lookup': { 'from': 'tipos_incidente', 'localField': 'tipoIncidente', 'foreignField': '_id', 'as': 'tipo_info' } }",
            "{ '$match': { 'ruta': ?0, 'kilometro': { '$gte': ?1, '$lte': ?2 } } }",
            "{ '$sort': { 'kilometro': 1 } }"
    })
    List<Incidente> buscarIncidentesPorRuta(String rutaId, int kmInicio, int kmFin);

    @Aggregation(pipeline = {
            "{ '$match': { 'ruta.$id': ObjectId(?0) } }",
            "{ '$lookup': { 'from': 'tipos_incidente', 'localField': 'tipoIncidente.$id', 'foreignField': '_id', 'as': 'tipoInfo' } }",
            "{ '$unwind': '$tipoInfo' }",
            "{ '$project': { " +
                    "'tramo': { '$floor': { '$divide': ['$kilometro', 100] } }, " +
                    "'gravedad': '$tipoInfo.importancia' " +
                    "} }",
            "{ '$group': { '_id': '$tramo', 'totalGravedad': { '$sum': '$gravedad' } } }",
            "{ '$sort': { 'totalGravedad': -1 } }"
    })
    List<Map<String, Object>> obtenerReporteRuta(String rutaId);

}