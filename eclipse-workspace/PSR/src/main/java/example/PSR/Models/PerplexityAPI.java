package example.PSR.Models;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.tomcat.util.json.JSONParser;
import org.apache.tomcat.util.json.ParseException;
import org.bson.json.JsonObject;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import example.PSR.ChatGPT.ChatGPTResponse;
import example.PSR.Perplexity.PerplexityResponse;

public class PerplexityAPI {

	public static String Perplexity(String question) throws IOException, ParseException {
		
		String url = "https://api.perplexity.ai/chat/completions";
        HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();

        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Authorization", "Bearer YOUR_API_KEY_PERPLEXITY");
        
        String model = "mistral-7b-instruct";
        String prompt = "[{\"role\": \"user\", \"content\": \"" + question + "}]";
        int maxTokens = 500;
        
        con.setDoOutput(true);
        String body = "{\"model\": \"" + model + "\", \"messages\": " + prompt + ", \"max_tokens\": " + maxTokens + "}";
        
        OutputStreamWriter writer = new OutputStreamWriter(con.getOutputStream());
        writer.write(body);
        writer.flush();
        
        //Visualizar pedido
        //System.out.println(body);

        //Read the response
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
          response.append(inputLine);
        }
        in.close();
        
        //Converte uma string num objeto do tipo JSON
        ObjectMapper om = new ObjectMapper();
        om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        PerplexityResponse pr = om.readValue(response.toString(), PerplexityResponse.class);
        
        return pr.getChoices().get(0).getMessage().getContent();
        
	}
	
}
