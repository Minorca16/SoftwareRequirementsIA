package example.PSR.Models;

import java.util.ArrayList;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "ProjectRequirements")
public class ProjectRequirements {

	@Transient
	public static final String SEQUENCE_NAME = "projectrequirements_sequence";
	
	@Id
	private String id;
	
	private String desc;
	
	private String nome;
	
	private ArrayList<Requirements> ListReq = new ArrayList<>();
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}
	
	public String getNome() {
		return nome;
	}
	
	public void setNome(String nome) {
		this.nome = nome;
	}

	public ArrayList<Requirements> getListReq() {
		return ListReq;
	}

	public void setListReq(ArrayList<Requirements> listReq) {
		ListReq = listReq;
	}
	
}
