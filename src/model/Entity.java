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
		String key = null;
		Iterator iJsonEntity = json.keys();
		while (iJsonEntity.hasNext()) {
			key = (String) iJsonEntity.next();
			switch (key) {
			case "Id":
				Id = json.getLong(key);
				break;
			case "F":
				JSONArray FFIdJsonArray = json.getJSONArray(key);
				FFId = new ArrayList<Long>();
				for (int j = 0; j < FFIdJsonArray.length(); j++) {
					JSONObject FieldJson = FFIdJsonArray.getJSONObject(j);
					FFId.add(FieldJson.getLong("FId"));
				}
				break;
			case "J":
				JSONObject JJIdJson = json.getJSONObject(key);
				JJId = JJIdJson.getLong("JId");
				break;
			case "C":
				JSONObject CCIdJson = json.getJSONObject(key);
				CCId = CCIdJson.getLong("CId");
				break;
			case "AA":
				JSONArray AAJsonArray = json.getJSONArray(key);
				AuId = new ArrayList<Long>();
				AfId = new ArrayList<Long>();
				for (int j = 0; j < AAJsonArray.length(); j++) {
					JSONObject jsonAA = AAJsonArray.getJSONObject(j);
					Iterator iJsonAA = jsonAA.keys();
					while (iJsonAA.hasNext()) {
						switch ((String) iJsonAA.next()) {
						case "AuId":
							AuId.add(jsonAA.getLong("AuId"));
							break;
						case "AfId":
							AfId.add(jsonAA.getLong("AfId"));
							break;
						}
					}
					if (AuId.size() != AfId.size())
						AfId.add((long) -1);
				}
				break;
			case "RId":
				JSONArray RIdJsonArray = json.getJSONArray(key);
				RId = new ArrayList<Long>();
				for (int j = 0; j < RIdJsonArray.length(); j++) {
					RId.add(RIdJsonArray.getLong(j));
				}
				break;
			}
		}
	}
	
	public long getId() {
		return Id;
	}
	
	public long getJJId() {
		return JJId;
	}
	
	public long getCCId() {
		return CCId;
	}
	
	public List<Long> getFId() {
		return FFId;
	}
	
	public List<Long> getAuId() {
		return AuId;
	}
	
	public List<Long> getAfId() {
		return AfId;
	}
	
	public List<Long> getRId() {
		return RId;
	}
		private String buildOrExpression(String name, List<Long> l, Boolean isComposite){
		String res = "";
		if (isComposite)
			for (int i =0;i<l.size();i++)
				res = "Or("+res+",Composite("+name+"="+l.get(i)+"))";
		else
			for (int i =0;i<l.size();i++)
				res = "Or("+res+","+name+"="+l.get(i)+")";
		return res;
	}

	public String getRIdOrExpression(){ return buildOrExpression("RId", RId, false); }
	public String getFIdOrExpression(){ return buildOrExpression("F.FId", FFId, true); }
	public String getAuIdOrExpression(){ return buildOrExpression("AA.AuId", AuId, true); }
}
