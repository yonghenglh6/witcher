package model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import model.Attribute.TYPE;

public class Entity {
	private long Id = -1;
	private List<Long> FFId = null;
	private long JJId = -1;
	private long CCId = -1;
	private List<Long> AuId = null;
	private List<Long> AfId = null;
	private List<Long> RId = null;

	public Entity(JSONObject json) {
		JSONArray jsonArray = json.getJSONArray("entities");
		String key = null;
		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject jsonEntity = jsonArray.getJSONObject(i);
			Iterator iJsonEntity = jsonEntity.keys();
			while (iJsonEntity.hasNext()) {
				key = (String) iJsonEntity.next();
				switch (key) {
				case "Id":
					Id = jsonEntity.getLong(key);
					break;
				case "F":
					JSONArray FFIdJsonArray = jsonEntity.getJSONArray(key);
					FFId = new ArrayList<Long>();
					for (int j = 0; j < FFIdJsonArray.length(); j++) {
						JSONObject FieldJson = FFIdJsonArray.getJSONObject(j);
						FFId.add(FieldJson.getLong("FId"));
					}
					break;
				case "J":
					JSONObject JJIdJson = jsonEntity.getJSONObject(key);
					JJId = JJIdJson.getLong("JId");
					break;
				case "C":
					JSONObject CCIdJson = jsonEntity.getJSONObject(key);
					CCId = CCIdJson.getLong("CId");
					break;
				case "AA":
					JSONArray AAJsonArray = jsonEntity.getJSONArray(key);
					AuId = new ArrayList<Long>();
					AfId = new ArrayList<Long>();
					for (int j = 0; j < AAJsonArray.length(); j++) {
						JSONObject jsonAA = AAJsonArray.getJSONObject(j);
						Iterator iJsonAA = jsonAA.keys();
						while(iJsonAA.hasNext()) {
							switch((String) iJsonAA.next()) {
							case "AuId":
								AuId.add(jsonAA.getLong("AuId"));
								break;
							case "AfId":
								AfId.add(jsonAA.getLong("AfId"));
								break;
							}
						}
						if(AuId.size() != AfId.size())
							AfId.add((long) -1);
					}
					break;
				case "RId":
					JSONArray RIdJsonArray = jsonEntity.getJSONArray(key);
					RId = new ArrayList<Long>();
					for (int j = 0; j < RIdJsonArray.length(); j++) {
						RId.add(RIdJsonArray.getLong(j));
					}
					break;
				}
			}
		}
	}

	public long getSingleEntity(TYPE type) {
		switch (type) {
		case J_JId:
			return JJId;
		case C_CId:
			return CCId;
		case Id:
			return Id;
		}
		return -1;
	}

	public List<Long> getListEntity(TYPE type) {
		switch (type) {
		case F_FId:
			return FFId;
		case AA_AuId:
			return AuId;
		case AA_AfId:
			return AfId;
		case RId:
			return RId;
		}
		return null;
	}
}
