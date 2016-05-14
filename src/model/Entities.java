package model;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class Entities {
	public static List<Entity> getEntityList(JSONObject json) {
		JSONArray jsonArray = json.getJSONArray("entities");
		List<Entity> list = new ArrayList<Entity>();
		for (int i = 0; i < jsonArray.length(); i++) {
			list.add(new Entity(jsonArray.getJSONObject(i)));
		}
		return list;
	}
}
