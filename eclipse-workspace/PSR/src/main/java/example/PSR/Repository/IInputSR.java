package example.PSR.Repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import example.PSR.Models.InputSR;

public interface IInputSR extends MongoRepository<InputSR, String> {
	
	@Query("{ '_id' : ?0 }")
	List<InputSR> getInputSRById(String id);

}
