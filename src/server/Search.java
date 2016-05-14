package server;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import model.Attribute.TYPE;
import model.Entities;
import model.Entity;

public class Search {
	public static enum Category {
		Id2Id, Id2AuId, AuId2Id, AuId2AuId
	}
	private long head;
	private long tail;
	private Category category;
	
	public Search(TYPE ht, long h, TYPE tt, long t) {
		if(ht == TYPE.Id && tt == TYPE.Id) {
			category = Category.Id2Id;
		} else if(ht == TYPE.Id && tt == TYPE.AA_AuId) {
			category = Category.Id2AuId;
		} else if(ht == TYPE.AA_AuId && tt == TYPE.Id) {
			category = Category.AuId2Id;
		} else if(ht == TYPE.AA_AuId && tt == TYPE.AA_AuId) {
			category = Category.AuId2AuId;
		}
		head = h;
		tail = t;
	}
	
	public JSONObject getPath() {
		switch(category) {
		case Id2Id:
			return getId2IdPath();
		case Id2AuId:
			return getId2AuIdPath();
		case AuId2Id:
			return getAuId2IdPath();
		case AuId2AuId:
			return getAuId2AuIdPath();
		}
		return null;
	}
	
	private JSONObject getId2IdPath() {
		JSONObject json;
		JSONArray paths = new JSONArray();
		Map<String, String> paras = new HashMap<String, String>();
		paras.put("attributes", "Id,F.FId,J.JId,C.CId,AA.AuId,AA.AfId,RId");
		json = MicrosoftAcademicAPI.evaluateMethod("Or(Or(Id=" + head + ",Id=" + tail + "),RId=" + tail +")", paras);
		List<Entity> firstQuery = Entities.getEntityList(json);
		Entity Id1 = null, Id2 = null;
		for(int i = 0, count = 0; i < firstQuery.size() && count < 2; i++) {
			if(firstQuery.get(i).getId() == head) {
				Id1 = firstQuery.get(i);
				firstQuery.remove(i);
				--i;
				++count;
			} else if(firstQuery.get(i).getId() == tail) {
				Id2 = firstQuery.get(i);
				firstQuery.remove(i);
				--i;
				++count;
			}
		}
		List<Long> RId1 = Id1.getRId();
		if(-1 != RId1.indexOf(Id2.getId())) {
			System.out.println("[" + head + "," + tail + "]");
		}
		for(int i = 0; i < RId1.size(); i++) {
			
		}
		return null;
	}
	
	private JSONObject getId2AuIdPath() {
		
		return null;
	}
	
	private JSONObject getAuId2IdPath() {
		
		return null;
	}
	
	private JSONObject getAuId2AuIdPath() {
		
		return null;
	}
}
