package server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
		if (ht == TYPE.Id && tt == TYPE.Id) {
			category = Category.Id2Id;
		} else if (ht == TYPE.Id && tt == TYPE.AA_AuId) {
			category = Category.Id2AuId;
		} else if (ht == TYPE.AA_AuId && tt == TYPE.Id) {
			category = Category.AuId2Id;
		} else if (ht == TYPE.AA_AuId && tt == TYPE.AA_AuId) {
			category = Category.AuId2AuId;
		}
		head = h;
		tail = t;
	}

	public JSONObject getPath() {
		switch (category) {
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
		for (int i = 0, count = 0; i < Id2AsReference.size() && count < 2; i++) {
			if (Id2AsReference.get(i).getId() == head) {
				Id1 = Id2AsReference.get(i);
				Id2AsReference.remove(i);
				--i;
				++count;
			} else if (Id2AsReference.get(i).getId() == tail) {
				Id2 = Id2AsReference.get(i);
				Id2AsReference.remove(i);
				--i;
				++count;
			}
		}

		List<Long> RId1 = Id1.getRId();
		// 1-hop
		if (-1 != RId1.indexOf(Id2.getId())) {
			System.out.println("[" + head + "," + tail + "]");
		}
		// 2-hop Id->Id->Id
		for (int i = 0; i < Id2AsReference.size(); i++) {
			Entity e = Id2AsReference.get(i);
			if (-1 != RId1.indexOf(e.getId())) {
				System.out.println("[" + head + "," + e.getId() + "," + tail + "]");
			}
		}

		// 2-hop Id->CId->Id
		if (Id1.getCCId() == Id2.getCCId() && -1 != Id1.getCCId()) {
			System.out.println("[" + head + "," + Id1.getCCId() + "," + tail + "]");
		}

		// 2-hop Id->JId->Id
		if (Id1.getJJId() == Id2.getJJId() && -1 != Id1.getJJId()) {
			System.out.println("[" + head + "," + Id1.getJJId() + "," + tail + "]");
		}

		// 2-hop Id->AuId->Id
		List<Long> AuId1 = Id1.getAuId();
		List<Long> AuId2 = Id2.getAuId();
		if (AuId2 != null && AuId2 != null) {
			for (int i = 0; i < AuId1.size(); i++) {
				if (-1 != AuId2.indexOf(AuId1.get(i))) {
					System.out.println("[" + head + "," + AuId1.get(i) + "," + tail + "]");
				}
			}
		}

		// 2-hop Id->FId->Id
		List<Long> FId1 = Id1.getFId();
		List<Long> FId2 = Id2.getFId();
		if (FId1 != null && FId2 != null) {
			for (int i = 0; i < FId1.size(); i++) {
				if (-1 != FId2.indexOf(FId1.get(i))) {
					System.out.println("[" + head + "," + FId1.get(i) + "," + tail + "]");
				}
			}
		}
		stopWatch.stop("1&2-hop");

		// 3-hop
		String expr = "";
		if (RId1.size() > 0)
			expr = "Id=" + RId1.get(0);
		for (int i = 1; i < RId1.size(); i++) {
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
		long AID = head;
		long ID = tail;
		StopWatch stopWatch = new StopWatch();
		JSONObject json;
		JSONArray paths = new JSONArray();
		Map<String, String> paras = new HashMap<String, String>();
		paras.put("attributes", "Id,F.FId,J.JId,C.CId,AA.AuId,AA.AfId,RId");
		paras.put("count", Integer.MAX_VALUE + "");
		stopWatch.start();
		json = MicrosoftAcademicAPI.evaluateMethod(Or(Or(Composite("AA.AuId=" + AID), "Id=" + ID), "RId=" + ID), paras);
		// json = MicrosoftAcademicAPI.evaluateMethod(Or(Composite("AA.AuId=" +
		// AID), "Id=" + ID), paras);
		stopWatch.stop("查询");
		stopWatch.start();
		// System.out.println(json.toString());
		List<Entity> entitys = Entities.getEntityList(json);

		stopWatch.stop("分析结果");
		stopWatch.start();
		Entity PaperDest = null;
		List<Entity> PagersWithSpecAuid = new ArrayList<Entity>();
		List<Entity> PagersRefSpecId = new ArrayList<Entity>();
		Set<Long> AFID = new HashSet<Long>();
		for (Entity et : entitys) {
			if (et.getId() == ID) {
				PaperDest = et;
			}

			if (et.getAuId() != null) {
				int tjid = et.getAuId().indexOf(AID);
				if (tjid != -1) {
					PagersWithSpecAuid.add(et);
					if(et.getAfId().get(tjid)!=-1)
						AFID.add(et.getAfId().get(tjid));
				}
			}
			if (et.getRId() != null && et.getRId().indexOf(ID) != -1) {
				PagersRefSpecId.add(et);
			}
		}

		stopWatch.stop("分类返回数据");
		stopWatch.start();
		// [AA_AuId,Id,]
		for (Entity et : PagersWithSpecAuid) {
			if (et.getId() == ID && et.getId() != -1) {
				System.out.println("[" + AID + "," + ID + "]");
			}
		}
		stopWatch.stopAndStart("[AA_AuId,Id,]");
		// [AA_AuId,Id,RId,]
		for (Entity et : PagersWithSpecAuid) {
			if (et.getRId() != null && et.getRId().indexOf(ID) != -1) {
				System.out.println("[" + AID + "," + et.getId() + "," + ID + "]");
			}
		}
		stopWatch.stopAndStart(" [AA_AuId,Id,RId,]");
		// [AA_AuId,AA_AfId,AA_AuId,Id,]
		if (PaperDest.getAfId() != null) {
			for (int in = 0; in < PaperDest.getAfId().size(); in++) {
				if (AFID.contains(PaperDest.getAfId().get(in))) {
					System.out.println("[" + AID + "," + PaperDest.getAfId().get(in) + "," + PaperDest.getAuId().get(in) + "," + ID + "]");
				}
			}
		}
		stopWatch.stopAndStart(" [AA_AuId,AA_AfId,AA_AuId,Id,]");
		// [AA_AuId,Id,RId,RId,]
		for (Entity et : PagersWithSpecAuid) {
			if (et.getRId() != null) {
				for (Entity et2 : PagersRefSpecId) {
					if (et.getRId().indexOf(et2.getId()) != -1) {
						System.out.println("[" + AID + "," + et.getId() + "," + et2.getId() + "," + ID + "]");
					}
				}
			}
		}
		stopWatch.stopAndStart(" [AA_AuId,Id,RId,RId,]");
		// [AA_AuId,Id,F_FId,Id,]
		for (Entity et : PagersWithSpecAuid) {
			if (et.getFId() != null) {
				et.getFId().retainAll(PaperDest.getFId());
				for (long tfid : et.getFId()) {
					System.out.println("[" + AID + "," + et.getId() + "," + tfid + "," + ID + "]");
				}
			}
			if (et.getAuId() != null) {
				et.getAuId().retainAll(PaperDest.getAuId());
				for (long taid : et.getAuId()) {
					System.out.println("[" + AID + "," + et.getId() + "," + taid + "," + ID + "]");
				}
			}
			if (et.getCCId() != -1) {
				if (et.getCCId() == PaperDest.getCCId() && PaperDest.getCCId() != -1) {
					System.out.println("[" + AID + "," + et.getId() + "," + et.getCCId() + "," + ID + "]");
				}
			}
			if (et.getJJId() != -1) {
				if (et.getJJId() == PaperDest.getJJId() && PaperDest.getJJId() != -1) {
					System.out.println("[" + AID + "," + et.getId() + "," + et.getJJId() + "," + ID + "]");
				}
			}
		}
		stopWatch.stopAndStart("[AA_AuId,Id,(FId,AuId,CCId,JJId),Id,]");
		return null;
	}

	private JSONObject getAuId2AuIdPath() {
		long AID1 = head;
		long AID2 = tail;
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		JSONArray result=new JSONArray();
		 
		
		Map<String, String> paras = new HashMap<String, String>();
		paras.put("attributes", "Id,F.FId,J.JId,C.CId,AA.AuId,AA.AfId,RId");
		paras.put("count", Integer.MAX_VALUE + "");
		
		JSONObject json = MicrosoftAcademicAPI.evaluateMethod(Composite(Or("AA.AuId=" + AID1,"AA.AuId=" + AID2)), paras);
		stopWatch.stopAndStart("查询");
		List<Entity> entitys = Entities.getEntityList(json);
		stopWatch.stopAndStart("分析结果");
		

		ArrayList<Entity> PagersWithAuid1 = new ArrayList<Entity>();
		ArrayList<Entity> PagersWithAuid2 = new ArrayList<Entity>();
		HashSet<Long> AFID1 = new HashSet<Long>();
		HashSet<Long> AFID2 = new HashSet<Long>();
		
		for (Entity et : entitys) {
			if (et.getAuId() != null) {
				int tjid1 = et.getAuId().indexOf(AID1);
				if (tjid1 != -1) {
					PagersWithAuid1.add(et);
					if(et.getAfId().get(tjid1)!=-1)
						AFID1.add(et.getAfId().get(tjid1));
				}
				int tjid2 = et.getAuId().indexOf(AID2);
				if (tjid2 != -1) {
					PagersWithAuid2.add(et);
					if(et.getAfId().get(tjid2)!=-1)
						AFID2.add(et.getAfId().get(tjid2));
				}
			}
		}
		stopWatch.stopAndStart("整理结果");
		//[AA_AuId,AA_AfId,AA_AuId,]
		@SuppressWarnings("unchecked")
		Set<Long> comAFID=(Set<Long>) AFID1.clone();
		comAFID.retainAll(AFID2);
		for(long s:comAFID){
			System.out.println("[" + AID1 + "," + s + "," + AID2 + "]");
		}
		stopWatch.stopAndStart("[AA_AuId,AA_AfId,AA_AuId,]");
		//[AA_AuId,Id,AA_AuId,]
		List<Entity> comPaper=(List<Entity>) PagersWithAuid1.clone();
		comPaper.retainAll(PagersWithAuid2);
		for(Entity et:comPaper){
			System.out.println("[" + AID1 + "," + et.getId() + "," + AID2 + "]");
		}
		stopWatch.stopAndStart("[AA_AuId,Id,AA_AuId,]");
		//[AA_AuId,Id,RId,AA_AuId,]
		ArrayList<Long> idsWithAuid2=getEntityIds(PagersWithAuid2);
		for(Entity et:PagersWithAuid1){
			List<Long> tRIDs=et.getRId();
			if(tRIDs!=null){
				tRIDs.retainAll(idsWithAuid2);
				for(long trid:tRIDs){
					System.out.println("[" + AID1 + "," + et.getId() + ","+trid +","+ AID2 + "]");
				}
			}
		}
		stopWatch.stopAndStart("[AA_AuId,Id,RId,AA_AuId,]");
		
		return null;
	}
	
	
	public ArrayList<Long> getEntityIds(List<Entity> ets){
		ArrayList<Long> tRID=new ArrayList<Long>();
		for(Entity et:ets){
			tRID.add(et.getId());
		}
		return tRID;
	}
	
	
	public String Or(String a, String b) {
		return "Or(" + a + "," + b + ")";
	}

	public String And(String a, String b) {
		return "And(" + a + "," + b + ")";
	}

	public String Composite(String a) {
		return "Composite(" + a + ")";
	}

	public static void main(String args[]) {

	}
}
