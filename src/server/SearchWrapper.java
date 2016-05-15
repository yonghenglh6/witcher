package server;

import java.util.List;

import org.json.JSONObject;

import model.Attribute.TYPE;

public class SearchWrapper {
	public static List<String> search(long h,  long t){
		Search search=new Search(h,t);
		return search.getPath();
	}
}
