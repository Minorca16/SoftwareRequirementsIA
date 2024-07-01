package example.PSR.Repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import example.PSR.Models.GeneratedSoftReq;

public interface IGeneratedSoftReq extends MongoRepository<GeneratedSoftReq, String> {
	
	@Query("{ '_id' : ?0 }")
	GeneratedSoftReq getSRById(String id);

}
