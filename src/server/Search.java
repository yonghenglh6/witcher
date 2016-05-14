package server;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import model.Attribute.TYPE;
import model.Entities;
import model.Entity;
import model.StopWatch;

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
		StopWatch stopWatch = new StopWatch();
		JSONObject json;
		JSONArray paths = new JSONArray();
		Map<String, String> paras = new HashMap<String, String>();
		paras.put("attributes", "Id,F.FId,J.JId,C.CId,AA.AuId,AA.AfId,RId");
		paras.put("count", Integer.MAX_VALUE + "");
		stopWatch.start();
		json = MicrosoftAcademicAPI.evaluateMethod(Or(Or("Id=" + head, "Id=" + tail), "RId=" + tail), paras);
		stopWatch.stop("query");
		stopWatch.start();
		List<Entity> Id2AsReference = Entities.getEntityList(json);
		Entity Id1 = null, Id2 = null;
		for(int i = 0, count = 0; i < Id2AsReference.size() && count < 2; i++) {
			if(Id2AsReference.get(i).getId() == head) {
				Id1 = Id2AsReference.get(i);
				Id2AsReference.remove(i);
				--i;
				++count;
			} else if(Id2AsReference.get(i).getId() == tail) {
				Id2 = Id2AsReference.get(i);
				Id2AsReference.remove(i);
				--i;
				++count;
			}
		}
		
		List<Long> RId1 = Id1.getRId();
		// 1-hop
		if(-1 != RId1.indexOf(Id2.getId())) {
			System.out.println("[" + head + "," + tail + "]");
		}
		// 2-hop	Id->Id->Id
		for(int i = 0; i < Id2AsReference.size(); i++) {
			Entity e = Id2AsReference.get(i);
			if(-1 != RId1.indexOf(e.getId())) {
				System.out.println("[" + head + ","+ e.getId() + "," + tail + "]");
			}
		}

		// 2-hop	Id->CId->Id
		if(Id1.getCCId() == Id2.getCCId() && -1 != Id1.getCCId()) {
			System.out.println("[" + head + ","+ Id1.getCCId() + "," + tail + "]");
		}

		// 2-hop	Id->JId->Id
		if(Id1.getJJId() == Id2.getJJId() && -1 != Id1.getJJId()) {
			System.out.println("[" + head + ","+ Id1.getJJId() + "," + tail + "]");
		}

		// 2-hop	Id->AuId->Id
		List<Long> AuId1 = Id1.getAuId();
		List<Long> AuId2 = Id2.getAuId();
		for(int i = 0; i < AuId1.size(); i++) {
			if(-1 != AuId2.indexOf(AuId1.get(i))) {
				System.out.println("[" + head + ","+ AuId1.get(i) + "," + tail + "]");
			}
		}

		// 2-hop	Id->FId->Id
		List<Long> FId1 = Id1.getFId();
		List<Long> FId2 = Id2.getFId();
		for(int i = 0; i < FId1.size(); i++) {
			if(-1 != FId2.indexOf(FId1.get(i))) {
				System.out.println("[" + head + ","+ FId1.get(i) + "," + tail + "]");
			}
		}
		stopWatch.stop("1&2-hop");
		
		// 3-hop
		String expr = "";
		if(RId1.size() > 0)
			expr = "Id=" + RId1.get(0);
		for(int i = 1; i < RId1.size(); i++) {
			expr = Or(expr, "Id=" + RId1.get(i));
		}
		System.out.println(expr);
		json = MicrosoftAcademicAPI.evaluateMethod(expr, paras);
		List<Entity> referenceOfId1 = Entities.getEntityList(json);
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
	
	public String Or(String a, String b) {
		return "Or(" + a + "," + b + ")";
	}
	
	public String And(String a, String b) {
		return "And(" + a + "," + b + ")";
	}
}
