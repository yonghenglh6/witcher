package server;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import model.Attribute.TYPE;

public class Search {
	public static enum Category {
		Id2Id, Id2AuId, AuId2Id, AuId2AuId
	}
	private int head;
	private int tail;
	private Category category;
	
	public Search(TYPE ht, int h, TYPE tt, int t) {
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
		JSONObject jsonRes;
		MicrosoftAcademicAPI msAPI = new MicrosoftAcademicAPI();
		Map<String, String> paras = new HashMap<String, String>();
		paras.put("attributes", "Id,F.FId,J.JId,C.CId,AA.AuId,AA.AfId,RId");
		jsonRes = msAPI.evaluateMethod("Or(Id=" + head + ",Id=" + tail + ")", paras);
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
