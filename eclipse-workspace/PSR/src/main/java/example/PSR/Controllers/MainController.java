package example.PSR.Controllers;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import example.PSR.ChatGPT.ChatGPTRequest;
import example.PSR.ChatGPT.ChatGPTResponse;
import example.PSR.Models.GeneratedSoftReq;
import example.PSR.Models.InputSR;
import example.PSR.Models.PerplexityAPI;
import example.PSR.Models.ProjectRequirements;
import example.PSR.Models.Requirements;
import example.PSR.Repository.IGeneratedSoftReq;
import example.PSR.Repository.IInputSR;
import example.PSR.Repository.IProjectRequirements;
import example.PSR.Repository.IRequirements;
import example.PSR.Repository.SequenceGeneratorService;

@Controller
@RequestMapping(path="/")
public class MainController {

    @Value(("${openai.api.url}"))
    private String apiURL;

    @Autowired
    private RestTemplate template;
	
	@Autowired
	private IInputSR rInputSR;
	
	@Autowired
	private IProjectRequirements rProReq;
	
	@Autowired
	private IRequirements rRequirements;
	
	@Autowired
	private IGeneratedSoftReq rGenSoftReq;
	
	private GeneratedSoftReq gsf;
	
	private Integer CurrIdPR;
	
	private List<String> LReqGPT;
	
	private List<Requirements> ListaReqGPT;
	
	private List<Requirements> ListaReqPplx;
	
	private List<String> LReqPplx;
	
	private List<String> LKnown;
	
	private String LResp;
	
	private Integer NumberContext;
	
	private ProjectRequirements PReq;
	
	private ArrayList<Requirements> LReq;
	
	private InputSR inputSR;
	
	public String AppWithChatGPT(String prompt){
		
		ChatGPTRequest request = new ChatGPTRequest("gpt-3.5-turbo", prompt);
        ChatGPTResponse chatGptResponse = template.postForObject(apiURL, request, ChatGPTResponse.class);
		
		return chatGptResponse.getChoices().get(0).getMessage().getContent();
	
	}
	
	public String ConstroiPrompt(String input) {
		
		String fprompt = "Cria requisitos de software tendo em conta a seguinte frase: ";
		
		fprompt = fprompt + input;
		
		fprompt = fprompt + " Apresenta os requisitos em formato bullet points.";
		
		return fprompt;
		
	}
	
	public String ConstroiDetalhePrompt(String input, String cat) {
		
		String fPrompt = "Considerando o contexto da especificação de requisitos de software, onde foi definido o/a " + cat +
				":'" + input + "', apresente uma lista de requisitos funcionais, não-funcionais e regras de negócio mais detalhados.";
		
		return fPrompt;
		
	}
	
	public String Constroi3Prompt(String input) {
		
		String fprompt = "Tendo em conta as seguintes respostas: " + input + ", gere requisitos funcionais, não-funcionais e regras de negócio."
				+ " Identifica a que grupo pertencem. Apresenta o resultado em Português.";
		
		return fprompt;
		
	}
	
	public GeneratedSoftReq UseAI(String inputText) throws Exception {
		
		InputSR isr = new InputSR();
		
		isr.setInput(inputText);
		Integer temp1 = (int) SequenceGeneratorService.generateSequence(InputSR.SEQUENCE_NAME);
		
		isr.setId(temp1.toString());
		
		rInputSR.save(isr);
		
		gsf = new GeneratedSoftReq();
		
		gsf.setiSR(isr);
		gsf.setChatGPTOutput(AppWithChatGPT(ConstroiPrompt(inputText)));
		gsf.setPerplexityOutput(PerplexityAPI.Perplexity(ConstroiPrompt(inputText) + "\""));
		
		return gsf;
		
	}
	
	public GeneratedSoftReq UseChatGPT(String inputText) {
		
		InputSR isr = new InputSR();
		
		isr.setInput(inputText);
		Integer temp1 = (int) SequenceGeneratorService.generateSequence(InputSR.SEQUENCE_NAME);
		
		isr.setId(temp1.toString());
		
		rInputSR.save(isr);
		
		gsf = new GeneratedSoftReq();
		
		gsf.setiSR(isr);
		gsf.setChatGPTOutput(AppWithChatGPT(ConstroiPrompt(inputText)));
		//gsf.setPerplexityOutput(PerplexityAPI.Perplexity(ConstroiPrompt(inputText) + "\""));
		
		return gsf;
		
	}
	
	public List<String> returnQuestions(String LQuestions){
		
		List<String> ListaQ = new ArrayList<>();
		Integer ContadorQ = 0;
		
		String[] tLista = LQuestions.split("\n");
		
		for(String a : tLista) {
			if(a.isBlank() == false && a.contains("?") == true) {
				ListaQ.add(ContadorQ, a);
				if(ContadorQ < 19)
					ContadorQ++;
			}
		}
		
		return ListaQ;
		
	}
	
	public String UseGPTQuestions(String inputText) {
		
		String fPrompt = "Dentro de instantes irei requisitar que faças a descrição de requisitos funcionais, não funcionais e regras de negócio"
				+ " para um software completo de gestão. Tendo em conta a seguinte descrição: " + inputText + ", faz-me 10 perguntas para conhecer"
						+ " de uma maneira mais aprofundada as necessidades do software.";
		
		String fResult = AppWithChatGPT(fPrompt);
		
		return fResult;
		
	}
	
	public void ConstruirListas(String outputGPT, String outputPplx) {
		
		Integer index = 0;
		String[] tListaGPT = outputGPT.split("\n");
		String[] tListaPplx = outputPplx.split("\n");
		String categoria = "";
		ListaReqGPT = new ArrayList<Requirements>();
		ListaReqPplx = new ArrayList<Requirements>();
		
		for(String a : tListaGPT) {
			if(a.toLowerCase().contains("requisitos funcionais")) {
				//System.out.println("String: " + a);
				//System.out.println("Contém Requisitos Funcionais");
				categoria = "Requisitos Funcionais";
			}else {
				if(a.toLowerCase().contains("requisitos não-funcionais")) {
					//System.out.println("String: " + a);
					//System.out.println("Contém Requisitos Não-Funcionais");
					categoria = "Requisitos Não-Funcionais";
				}else {
					if(a.toLowerCase().contains("regras de negócio")) {
						//System.out.println("String: " + a);
						//System.out.println("Contém Regras de Negócio");
						categoria = "Regras de Negócio";
					}else {
						if(a.isBlank() == false) {
							Requirements r = new Requirements();
							r.setOrigem("ChatGPT");
							r.setMsg(a);
							r.setCategoria(categoria);
							ListaReqGPT.add(r);
							LReqGPT.add(index, a);
							//System.out.println("Categoria: " + categoria);
							index++;
							
							Integer CurrIdReq = (int) SequenceGeneratorService.generateSequence(Requirements.SEQUENCE_NAME);
							r.setId(CurrIdReq.toString());
							rRequirements.save(r);
						}
					}
				}
			}
		}
		
		index = 0;
		
		for(String b : tListaPplx) {
			if(b.toLowerCase().contains("requisitos funcionais") || b.toLowerCase().contains("functional requirements")) {
				//System.out.println("String: " + a);
				//System.out.println("Contém Requisitos Funcionais");
				categoria = "Requisitos Funcionais";
			}else {
				if(b.toLowerCase().contains("requisitos não-funcionais") || b.toLowerCase().contains("non-functional requirements")) {
					//System.out.println("String: " + a);
					//System.out.println("Contém Requisitos Não-Funcionais");
					categoria = "Requisitos Não-Funcionais";
				}else {
					if(b.toLowerCase().contains("regras de negócio") || b.toLowerCase().contains("business rules")) {
						//System.out.println("String: " + a);
						//System.out.println("Contém Regras de Negócio");
						categoria = "Regras de Negócio";
					}else {
						if(b.isBlank() == false) {
							Requirements r = new Requirements();
							r.setOrigem("Perplexity");
							r.setMsg(b);
							r.setCategoria(categoria);
							ListaReqPplx.add(r);
							LReqPplx.add(index, b);
							index++;
							
							Integer CurrIdReq = (int) SequenceGeneratorService.generateSequence(Requirements.SEQUENCE_NAME);
							r.setId(CurrIdReq.toString());
							rRequirements.save(r);
						}
					}
				}
			}
		}
		
	}
	
	public void ConstroiListaDetalhada(GeneratedSoftReq newgsf, int req, String ori) {
		
		List<Requirements> TempListReq = new ArrayList<>();
		Integer index = 0;
		String[] tListaGPT = newgsf.getChatGPTOutput().split("\n");
		//String[] tListaPplx = newgsf.getPerplexityOutput().split("\n");
		String categoria = "";
		
		for(int i = 0 ; i <= req ; i++) {
			if(ori.equalsIgnoreCase("ChatGPT")) {
				TempListReq.add(i, ListaReqGPT.get(i));
			}else {
				TempListReq.add(i, ListaReqPplx.get(i));
			}
		}
		
		if(ori.equalsIgnoreCase("ChatGPT")) {
			index = req + 1;
			
			for(String a : tListaGPT) {
				if(a.toLowerCase().contains("requisitos funcionais")) {
					//System.out.println("String: " + a);
					//System.out.println("Contém Requisitos Funcionais");
					categoria = "Requisitos Funcionais";
				}else {
					if(a.toLowerCase().contains("requisitos não-funcionais")) {
						//System.out.println("String: " + a);
						//System.out.println("Contém Requisitos Não-Funcionais");
						categoria = "Requisitos Não-Funcionais";
					}else {
						if(a.toLowerCase().contains("regras de negócio")) {
							//System.out.println("String: " + a);
							//System.out.println("Contém Regras de Negócio");
							categoria = "Regras de Negócio";
						}else {
							if(a.isBlank() == false) {
								Requirements r = new Requirements();
								r.setOrigem("ChatGPT");
								r.setMsg(a);
								r.setCategoria(categoria);
								TempListReq.add(index, r);
								index++;
								
								Integer CurrIdReq = (int) SequenceGeneratorService.generateSequence(Requirements.SEQUENCE_NAME);
								r.setId(CurrIdReq.toString());
								rRequirements.save(r);
							}
						}
					}
				}
			}
			
			//index = ListaReqGPT.size();
			
			/*for(String b : tListaPplx) {
				if(b.toLowerCase().contains("requisitos funcionais")) {
					//System.out.println("String: " + a);
					//System.out.println("Contém Requisitos Funcionais");
					categoria = "Requisitos Funcionais";
				}else {
					if(b.toLowerCase().contains("requisitos não-funcionais")) {
						//System.out.println("String: " + a);
						//System.out.println("Contém Requisitos Não-Funcionais");
						categoria = "Requisitos Não-Funcionais";
					}else {
						if(b.toLowerCase().contains("regras de negócio")) {
							//System.out.println("String: " + a);
							//System.out.println("Contém Regras de Negócio");
							categoria = "Regras de Negócio";
						}else {
							if(b.isBlank() == false) {
								Requirements r = new Requirements();
								r.setOrigem("Perplexity");
								r.setMsg(b);
								r.setCategoria(categoria);
								ListaReqGPT.add(index, r);
								index++;
							}
						}
					}
				}
			}*/
		}else {
			if(ori.equalsIgnoreCase("Perplexity")) {
				index = req + 1;
				
				for(String a : tListaGPT) {
					if(a.toLowerCase().contains("requisitos funcionais")) {
						//System.out.println("String: " + a);
						//System.out.println("Contém Requisitos Funcionais");
						categoria = "Requisitos Funcionais";
					}else {
						if(a.toLowerCase().contains("requisitos não-funcionais")) {
							//System.out.println("String: " + a);
							//System.out.println("Contém Requisitos Não-Funcionais");
							categoria = "Requisitos Não-Funcionais";
						}else {
							if(a.toLowerCase().contains("regras de negócio")) {
								//System.out.println("String: " + a);
								//System.out.println("Contém Regras de Negócio");
								categoria = "Regras de Negócio";
							}else {
								if(a.isBlank() == false) {
									Requirements r = new Requirements();
									r.setOrigem("Perplexity");
									r.setMsg(a);
									r.setCategoria(categoria);
									TempListReq.add(index, r);
									index++;
									
									Integer CurrIdReq = (int) SequenceGeneratorService.generateSequence(Requirements.SEQUENCE_NAME);
									r.setId(CurrIdReq.toString());
									rRequirements.save(r);
								}
							}
						}
					}
				}
				
				//index = ListaReqPplx.size();
				
				/*for(String b : tListaPplx) {
					if(b.toLowerCase().contains("requisitos funcionais")) {
						//System.out.println("String: " + a);
						//System.out.println("Contém Requisitos Funcionais");
						categoria = "Requisitos Funcionais";
					}else {
						if(b.toLowerCase().contains("requisitos não-funcionais")) {
							//System.out.println("String: " + a);
							//System.out.println("Contém Requisitos Não-Funcionais");
							categoria = "Requisitos Não-Funcionais";
						}else {
							if(b.toLowerCase().contains("regras de negócio")) {
								//System.out.println("String: " + a);
								//System.out.println("Contém Regras de Negócio");
								categoria = "Regras de Negócio";
							}else {
								if(b.isBlank() == false) {
									Requirements r = new Requirements();
									r.setOrigem("Perplexity");
									r.setMsg(b);
									r.setCategoria(categoria);
									ListaReqPplx.add(index, r);
									index++;
								}
							}
						}
					}
				}*/
			}
		}
		
		req = req + 1;
		//System.out.println("Req: " + req);
		//System.out.println("Index: " + TempListReq.size());
		
		if(ori.equalsIgnoreCase("ChatGPT")) {
			for(int n = req ; n < ListaReqGPT.size() ; n++) {
				TempListReq.add(index++, ListaReqGPT.get(n));
			}
		}else {
			for(int m = req ; m < ListaReqPplx.size() ; m++) {
				TempListReq.add(index++, ListaReqPplx.get(m));
			}
		}
		
		if(ori.equalsIgnoreCase("ChatGPT")) {
			ListaReqGPT = TempListReq;
		}else {
			ListaReqPplx = TempListReq;
		}
		
		//System.out.println("ChatGPT Output:\n" + newgsf.getChatGPTOutput());
		//System.out.println("Perplexity Output:\n" + newgsf.getPerplexityOutput());
		
	}
	
	//Pergunta ao utilizador o contexto do software
	@GetMapping(path="/context")
	public String GContext(Model model) {
		
		String context = "";
		model.addAttribute("context", context);
		
		return "QuestionContext.html";
		
	}
	
	//Gere e mostra questões geradas pela AI ao utlizador
	//Apresenta pergunta a pergunta
	//Depois de responder a todas as perguntas, mostra os requisitos gerados
	@GetMapping(path="/knowledge")
	public String GKnowledge(Model model, @RequestParam(name="context") String context, @ModelAttribute(name="input") String resp) {
		
		//System.out.println("Descrição: " + context);
		
		PReq = new ProjectRequirements();
		LReq = new ArrayList<>();
		
		//Falta atribuir id
		//Atribuir quando a recolha de requisitos estiver completa
		PReq.setDesc(context);
		
		NumberContext = 0;
		LKnown = new ArrayList<>();
		LReqGPT = new ArrayList<>();
		LReqPplx = new ArrayList<>();
		
		String rQGPT = UseGPTQuestions(context);
		
		LKnown = returnQuestions(rQGPT);
		
		model.addAttribute("NQuestion", NumberContext + 1);
		model.addAttribute("Question", LKnown.get(NumberContext));
		
		LResp = "";
		
		return "InitialContext.html";
		
	}
	
	@PostMapping(path="/knowledge")
	public String PKnowledge(Model model, @ModelAttribute(name="input") String resp) throws Exception {
		
		LResp = LResp + " " + resp;
		
		if(NumberContext < (LKnown.size() - 1)) {
			NumberContext++;
		}else{
			//Só teste para saber se apresenta os requisitos gerados pela AI
			GeneratedSoftReq gsf = new GeneratedSoftReq();
			gsf = UseAI(Constroi3Prompt(PReq.getDesc() + LResp));
			
			Integer CurrIdGenSoftReq = (int) SequenceGeneratorService.generateSequence(GeneratedSoftReq.SEQUENCE_NAME);
			gsf.setId(CurrIdGenSoftReq.toString());
			rGenSoftReq.save(gsf);
			
			ConstruirListas(gsf.getChatGPTOutput(), gsf.getPerplexityOutput());
			
			PReq.setDesc(PReq.getDesc() + LResp);
			
			//Caso tenha chegado ao final das perguntas, apresentar pagina com lista de requisitos
			return "redirect:/response";
		}
		
		model.addAttribute("NQuestion", NumberContext + 1);
		model.addAttribute("Question", LKnown.get(NumberContext));
		
		return "InitialContext.html";
		
	}
	
	//Apresenta os requisitos gerados
	@GetMapping(path="/response")
	public String GResponse(Model model) {
		
		model.addAttribute("ProResGPT", ListaReqGPT);
		model.addAttribute("ProResPplx", ListaReqPplx);
		
		String nomeproj = "";
		model.addAttribute("nomeproj", nomeproj);
		
		return "QuestionResults/RequiresResp.html";
		
	}
	
	//Adiciona requisito ao projeto
	@GetMapping(path="/addreq/{req}")
	public String GAddRequirement(Model model, @PathVariable(name="req") int req, 
			@RequestParam(name="ori") String ori) {
		
		int contaReq = 0;
		
		Requirements requisitos = new Requirements();
		requisitos.setOrigem(ori);
		
		if(ori.equalsIgnoreCase("ChatGPT") == true) {
			requisitos.setMsg(ListaReqGPT.get(req).getMsg());
			requisitos.setOrigem("ChatGPT");
			requisitos.setCategoria(ListaReqGPT.get(req).getCategoria());
			ListaReqGPT.remove(req);
		}else {
			if(ori.equalsIgnoreCase("Perplexity") == true) {
				requisitos.setMsg(ListaReqPplx.get(req).getMsg());
				requisitos.setOrigem("Perplexity");
				requisitos.setCategoria(ListaReqPplx.get(req).getCategoria());
				ListaReqPplx.remove(req);
			}
		}
		
		for(int i = 0 ; i < LReq.size() ; i++) {
			if(LReq.get(i).getCategoria().equalsIgnoreCase(requisitos.getCategoria())) {
				contaReq++;
			}
		}
		
		if(requisitos.getCategoria().equalsIgnoreCase("Requisitos Funcionais")) {
			requisitos.setReferencia("RF" + (contaReq + 1));
		}else {
			if(requisitos.getCategoria().equalsIgnoreCase("Requisitos Não-Funcionais")) {
				requisitos.setReferencia("RNF" + (contaReq + 1));
			}else {
				if(requisitos.getCategoria().equalsIgnoreCase("Regras de Negócio")) {
					requisitos.setReferencia("RN" + (contaReq + 1));
				}
			}
		}
		
		LReq.add(requisitos);
		PReq.setListReq(LReq);
		
		return "redirect:/response";
		
	}
	
	//Detalha o requisito
	@GetMapping(path="/newdetailreq/{req}")
	public String GNewDetailRequirement(Model model, @PathVariable(name="req") int req, 
			@RequestParam(name="ori") String ori) throws Exception {
		
		Requirements reqTemp = new Requirements();
		
		if(ori.equalsIgnoreCase("ChatGPT")){
			reqTemp = ListaReqGPT.get(req);
		}else {
			if(ori.equalsIgnoreCase("Perplexity")) {
				reqTemp = ListaReqPplx.get(req);
			}
		}
		
		String cat = "";
		
		if(reqTemp.getCategoria().equalsIgnoreCase("Requisitos Funcionais")) {
			cat = "RF: ";
		}else {
			if(reqTemp.getCategoria().equalsIgnoreCase("Requisitos Não-Funcionais")) {
				cat = "RNF: ";
			}else {
				if(reqTemp.getCategoria().equalsIgnoreCase("Regras de Negócio")) {
					cat = "RN: ";
				}
			}
		}
		
		GeneratedSoftReq tgsf = UseChatGPT(ConstroiDetalhePrompt(reqTemp.getMsg(), reqTemp.getCategoria()));
		
		//System.out.println(tgsf.getChatGPTOutput());
		
		ConstroiListaDetalhada(tgsf, req, ori);
		
		String nomeproj = "";
		model.addAttribute("nomeproj", nomeproj);
		
		model.addAttribute("TextReq", cat + reqTemp.getMsg());
		model.addAttribute("ProResGPT", ListaReqGPT);
		model.addAttribute("ProResPplx", ListaReqPplx);
		
		return "QuestionResults/RequiresRespDet.html";
		
	}
	
	public int getLastSheetEmpty(String filePath) throws IOException {
		try (FileInputStream fis = new FileInputStream(filePath);
	             Workbook workbook = new XSSFWorkbook(fis)) {
			int numberOfSheets = workbook.getNumberOfSheets();
			Sheet lastSheet = workbook.getSheetAt(numberOfSheets - 1);
			
			Row firstRow = lastSheet.getRow(0);
			
			if(firstRow != null) {
				//Última folha preenchida
				numberOfSheets = numberOfSheets + 1;
			}
			
	        return numberOfSheets;
	    }
	}
	
	public List<Requirements> OrganizaListaSave(List<Requirements> orList){
		
		List<Requirements> tempList = new ArrayList<Requirements>();
		int index = 0; 
		
		for(int i = 0 ; i < orList.size() ; i++) {
			if(orList.get(i).getCategoria().equalsIgnoreCase("Requisitos Funcionais")) {
				tempList.add(index, orList.get(i));
				index = index + 1;
			}
		}
		
		for(int m = 0 ; m < orList.size() ; m++) {
			if(orList.get(m).getCategoria().equalsIgnoreCase("Requisitos Não-Funcionais")) {
				tempList.add(index, orList.get(m));
				index = index + 1;
			}
		}
		
		for(int n = 0 ; n < orList.size() ; n++) {
			if(orList.get(n).getCategoria().equalsIgnoreCase("Regras de Negócio")) {
				tempList.add(index, orList.get(n));
				index = index + 1;
			}
		}
		
		return tempList;
		
	}
	
	//Grava o projeto na base de dados e no Excel
	@GetMapping(path="/save")
	public String GSaveAll(Model model, @RequestParam(name="nomeproj") String nomeproj) throws Exception {
		
		//System.out.println("Nome projeto: " + nomeproj);
		
		int contarf = 0;
		int contarnf = 0;
		int contarn = 0;
		
		List<Requirements> orList = PReq.getListReq();
		
		orList = OrganizaListaSave(orList);
		
		CurrIdPR =  (int) SequenceGeneratorService.generateSequence(ProjectRequirements.SEQUENCE_NAME);
		PReq.setId(CurrIdPR.toString());
		
		FileInputStream fis = new FileInputStream("./Requisitos.xlsx");
		Workbook wb = WorkbookFactory.create(fis);
		
		int np = getLastSheetEmpty("./Requisitos.xlsx");
		
		//System.out.println("Last Page Number: " + np);
		Sheet sh = wb.getSheetAt(0);
		
		if(np != 1) {
			sh = wb.createSheet("Folha" + np);
		}
		
		Row rownome = sh.createRow(0);
		Cell cellnome = rownome.createCell(0);
		cellnome.setCellValue("Nome: " + nomeproj);
		
		Row row = sh.createRow(1);
		Cell cell1 = row.createCell(0);
		cell1.setCellValue("Descrição: " + PReq.getDesc());
		
		Row headerRow = sh.createRow(3);
		Cell headerCell1 = headerRow.createCell(0);
		headerCell1.setCellValue("Categoria");
		
		Cell headerCell2 = headerRow.createCell(1);
		headerCell2.setCellValue("Referência");
		
		Cell headerCell3 = headerRow.createCell(2);
		headerCell3.setCellValue("Requisito");
		
		for(int i = 0 ; i < orList.size() ; i++) {
			Row r = sh.createRow(i + 4);
			Cell c1 = r.createCell(0);
			c1.setCellValue(orList.get(i).getCategoria());
			Cell c2 = r.createCell(1);
			if(orList.get(i).getCategoria().equalsIgnoreCase("Requisitos Funcionais")) {
				contarf = contarf + 1;
				c2.setCellValue("RF" + contarf);
			}else {
				if(orList.get(i).getCategoria().equalsIgnoreCase("Requisitos Não-Funcionais")) {
					contarnf = contarnf + 1;
					c2.setCellValue("RNF" + contarnf);
				}else {
					if(orList.get(i).getCategoria().equalsIgnoreCase("Regras de Negócio")) {
						contarn = contarn + 1;
						c2.setCellValue("RN" + contarn);
					}
				}
			}
			Cell c3 = r.createCell(2);
			c3.setCellValue(orList.get(i).getMsg());
		}
		
		FileOutputStream fos = new FileOutputStream("./Requisitos.xlsx");
		wb.write(fos);
		fos.flush();
		fos.close();
		
		//Grava o projecto na base de dados
		PReq.setNome(nomeproj);
		rProReq.save(PReq);
		
		return "redirect:/showall";
		
	}
	
	//Vista detalhada do projeto selecionado
	@GetMapping(path="/detailview/{proj}")
	public String gDetailView(Model model, @PathVariable(name="proj") String proj) {
		
		ProjectRequirements tpr = new ProjectRequirements();
		
		tpr = rProReq.getProjReqById(proj);
		
		//System.out.println("Project ID: " + proj);
		
		model.addAttribute("NomeProject", tpr.getNome());
		model.addAttribute("NProject", proj);
		model.addAttribute("description", tpr.getDesc());
		
		return "DetailView.html";
		
	}
	
	//Mostra todos os projetos já gravados
	@GetMapping(path="/showall")
	public String GShowAll(Model model) throws Exception {
		
		model.addAttribute("AllResults", rProReq.findAll());
		
		return "ShowResults.html";
		
	}
	
}
