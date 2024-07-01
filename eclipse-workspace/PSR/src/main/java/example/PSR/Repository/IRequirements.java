package example.PSR.Repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import example.PSR.Models.Requirements;

public interface IRequirements extends MongoRepository<Requirements,String>{

}
