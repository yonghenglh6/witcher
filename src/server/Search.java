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
		JSONObject jsonRes;
		Map<String, String> paras = new HashMap<String, String>();

		String reqStr, resStr;
		List<String> resStrList = new ArrayList<>();
		List<String> reqStrList = new ArrayList<>();

		// REQ1
		paras.put("attributes", "Id,F.FId,J.JId,C.CId,AA.AuId,RId");
		jsonRes = MicrosoftAcademicAPI.evaluateMethod("Id=" + head, paras);

		Entity headE = Entities.getEntityList(jsonRes).get(0);

		// REQ2
		paras.clear();paras.put("attributes", "F.FId,Id,RId");
		reqStr = "Or("+headE.getRIdOrExpression()+",RId="+tail+")";
		reqStr = "Or(Composite(F.FId="+headE.getFIdOrExpression()+"),"+reqStr+")";
		reqStr = "Or(RId="+tail+","+reqStr+")";
		reqStr = "Or("+headE.getAuIdOrExpression()+"),"+reqStr+")";
		jsonRes = MicrosoftAcademicAPI.evaluateMethod(reqStr, paras);
		Stream<Entity> reqRes2 = Entities.getEntityList(jsonRes).stream();

		// CAL1
		Stream<Long> Right = reqRes2.filter(e -> e.getRId().contains(tail)).map(e -> e.getId());
		Stream<Entity> IdLeft = reqRes2.filter(e -> headE.getRId().contains(e.getId()));
		// TODO check
		Stream<Entity> FFLeft = reqRes2.filter(e -> headE.getFId().retainAll(e.getFId()));
		FFLeft.forEach(e -> e.getFId().retainAll(headE.getFId()));
		Stream<Entity> AuLeft = reqRes2.filter(e -> headE.getAuId().retainAll(e.getAuId()));
		AuLeft.forEach(e -> e.getAuId().retainAll(headE.getAuId()));

		// 1HOP
		if (headE.getRId().contains(tail))
			resStrList.add("["+head+","+tail+"]");
		// 2HOP
		Right.filter(l -> headE.getRId().contains(l)).forEach(
				l -> resStrList.add("["+head+","+l+","+tail+"]")
		);
		// 3HOP : Id->Id->Id->AuId
		IdLeft.forEach(
				el -> Right.filter(er -> el.getRId().contains(er)).forEach(
						er -> resStrList.add("[" + head + "," + el.getId() + "," + er + "]")
				)
		);
		// 3HOP : Id->FFId->Id->AuId
		FFLeft.forEach(
				el -> Right.filter(er -> el.getFId().contains(er)).forEach(
						er -> el.getFId().forEach(
								lv -> resStrList.add("[" + head + "," + lv + "," + er + "," + tail + "]")
						)
				)
		);
		// 3HOP : Id->AuId->Id->AuId
		AuLeft.forEach(
				el -> Right.filter(er -> el.getAuId().contains(er)).forEach(
						er -> el.getAuId().forEach(
								lv -> resStrList.add("[" + head + "," + lv + "," + er + "," + tail + "]")
						)
				)
		);

		// REQ3
		paras.clear();paras.put("attributes", "Id,RId");
		if (headE.getCCId() != -1)
			reqStrList.add("And(Composite(C.CId="+headE.getCCId()+"),Composite(AA.AuId="+tail+"))");
		else if (headE.getJJId() != -1)
			reqStrList.add("And(Composite(J.JId="+headE.getJJId()+"),Composite(AA.AuId="+tail+"))");
		AuLeft.forEach(
				e -> e.getAfId().forEach(
						af -> reqStrList.add("Composite(And(AA.AfId="+af+",AA.AuId="+tail+"))")
				)
		);
		reqStr = "";
		for (int i=0;i<reqStrList.size();i++)
			reqStr = "Or("+reqStr+","+reqStrList.get(i)+")";
		paras.clear();paras.put("attributes", "F.FId,Id,RId");
		jsonRes = MicrosoftAcademicAPI.evaluateMethod(reqStr, paras);
		Stream<Entity> reqRes3 = Entities.getEntityList(jsonRes).stream();

		Stream<Entity> AfLeft = reqRes3.filter(e -> e.getCCId() != headE.getJJId() && e.getJJId() != headE.getJJId());
		List<Long> AfList = new ArrayList<>();AuLeft.forEach(e -> AfList.addAll(e.getAfId()));
        AfLeft.forEach(
                eAf -> {
                    eAf.getAuId().retainAll(headE.getAuId());
                    eAf.getAfId().retainAll(AfList);
                }
        );

		// HOP3 : Id->CCId/JJId->Id->AuId
		if (headE.getCCId() != -1)
			reqRes3.filter(e -> e.getCCId() == headE.getCCId()).forEach(
					e -> resStrList.add("["+head+","+headE.getCCId()+","+e.getId()+","+tail+"]")
			);
		else if (headE.getJJId() != -1)
			reqRes3.filter(e -> e.getCCId() == headE.getJJId()).forEach(
					e -> resStrList.add("[" + head + "," + headE.getJJId() + "," + e.getId() + "," + tail + "]")
			);

		// HOP3 : Id->AuId->AfId->AuId
		AuLeft.forEach(
				e -> e.getAuId().forEach(
						lAu -> e.getAfId().forEach(
								lAf -> resStrList.add("["+head+","+lAu+","+lAf+","+tail+"]")
						)
				)
		);

		resStr = "[" + String.join(",", resStrList) + "]";

		// TODO: TO JSON
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

		return null;
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
		Search search = new Search(TYPE.Id, 2157025439L, TYPE.Id, 2233354937L);
		search.getPath();
	}
}
