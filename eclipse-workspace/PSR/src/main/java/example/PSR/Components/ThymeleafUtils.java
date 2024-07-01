package example.PSR.Components;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import example.PSR.Models.ProjectRequirements;
import example.PSR.Repository.IProjectRequirements;

@Component
public class ThymeleafUtils {
	
	@Autowired
	private IProjectRequirements rProReq;

	public String replaceNewLines(String input) {
		
		return input.replaceAll("\n", "<br/>");
		
	}
	
	public String returnRequirements(String id) {
		
		String fPhrase = "";
		
		ProjectRequirements pr = rProReq.getProjReqById(id);
		
		for(int i = 0 ; i < pr.getListReq().size() ; i++) {
			if(pr.getListReq().get(i).getCategoria().equalsIgnoreCase("Requisitos Funcionais")) {
				if(i == 0) {
					fPhrase = pr.getListReq().get(i).getReferencia() + ": " + pr.getListReq().get(i).getMsg();
				}else {
					fPhrase = fPhrase + "\n" + pr.getListReq().get(i).getReferencia() + ": " + pr.getListReq().get(i).getMsg();
				}
			}
		}
		
		for(int m = 0 ; m < pr.getListReq().size() ; m++) {
			if(pr.getListReq().get(m).getCategoria().equalsIgnoreCase("Requisitos Não-Funcionais")) {
				fPhrase = fPhrase + "\n" + pr.getListReq().get(m).getReferencia() + ": " + pr.getListReq().get(m).getMsg();
			}
		}
		
		for(int n = 0 ; n < pr.getListReq().size() ; n++) {
			if(pr.getListReq().get(n).getCategoria().equalsIgnoreCase("Regras de Negócio")) {
				fPhrase = fPhrase + "\n" + pr.getListReq().get(n).getReferencia() + ": " + pr.getListReq().get(n).getMsg();
			}
		}
		
		return fPhrase;
		
	}
	
}