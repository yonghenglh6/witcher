package server;

import org.apache.http.client.HttpClient;

public class MicrosoftAcademicClient {
	static MicrosoftAcademicClient self;
	public static MicrosoftAcademicClient getInstance(){
		if(self==null){
			self=new MicrosoftAcademicClient();
		}
		return self;
	}
	private MicrosoftAcademicClient(){
		
	}
	
	public String get(String url){
		return null;
	}
}
