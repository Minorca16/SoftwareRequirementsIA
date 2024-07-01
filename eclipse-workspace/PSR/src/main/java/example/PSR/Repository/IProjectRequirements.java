package example.PSR.Repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import example.PSR.Models.ProjectRequirements;
import example.PSR.Models.Requirements;

public interface IProjectRequirements extends MongoRepository<ProjectRequirements, String> {
	
	@Query("{ '_id' : ?0 }")
	ProjectRequirements getProjReqById(String id);
	
	@Query("{ 'categoria' : ?0 }")
	List<Requirements> getRequirementsByCategoria(String categoria);

}
