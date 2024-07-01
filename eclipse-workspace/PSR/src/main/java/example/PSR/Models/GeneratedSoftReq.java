package example.PSR.Models;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;

@Document(collection = "GenSoftReq")
public class GeneratedSoftReq {
	
	@Transient
	public static final String SEQUENCE_NAME = "softreq_sequence";
	
	@Id
	private String id;
	
	public InputSR iSR;
	
	private String ChatGPTOutput;
	
	private String PerplexityOutput;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public InputSR getiSR() {
		return iSR;
	}

	public void setiSR(InputSR iSR) {
		this.iSR = iSR;
	}

	public String getChatGPTOutput() {
		return ChatGPTOutput;
	}

	public void setChatGPTOutput(String chatGPTOutput) {
		ChatGPTOutput = chatGPTOutput;
	}

	public String getPerplexityOutput() {
		return PerplexityOutput;
	}

	public void setPerplexityOutput(String perplexityOutput) {
		PerplexityOutput = perplexityOutput;
	}

}
