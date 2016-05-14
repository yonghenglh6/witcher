package server;

import java.util.List;

import org.json.JSONObject;

import model.Attribute.TYPE;

public class SearchWrapper {
	public static List<String> search(TYPE ht, long h, TYPE tt, long t){
		Search search=new Search(ht,h,tt,t);
		return search.getPath();
	}
}
