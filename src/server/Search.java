package server;

import java.lang.reflect.Array;
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

	public List<String> getPath() {
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

	private List<String> getId2IdPath() {
		long ID1 = head;
		long ID2 = tail;

		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		startResult();

		// 1次查询
		Map<String, String> paras1 = new HashMap<String, String>();
		paras1.put("attributes", "Id,F.FId,J.JId,C.CId,AA.AuId,AA.AfId,RId");
		paras1.put("count", Integer.MAX_VALUE + "");
		JSONObject json1 = MicrosoftAcademicAPI.evaluateMethod(Or(Or("Id=" + ID1, "Id=" + ID2), "RId=" + ID2), paras1);

		stopWatch.stopAndStart("query");

		List<Entity> entitys = Entities.getEntityList(json1);
		ArrayList<Entity> etRefID2 = new ArrayList<Entity>();
		ArrayList<Entity> etRefByID1 = new ArrayList<Entity>();
		Entity etId1 = null, etId2 = null;
		for (Entity et : entitys) {
			if (et.getId() == ID1) {
				etId1 = et;
			}
			if (et.getId() == ID2) {
				etId2 = et;
			}
			if (et.getRId() != null && et.getRId().indexOf(ID2) != -1) {
				etRefID2.add(et);
			}
		}

		if (etId1 == null || etId2 == null)
			return new ArrayList<String>();

		// 2次查询
		List<Long> et1rids = etId1.getRId();
		if (et1rids != null && et1rids.size() > 0) {
			String evalateStatement = "Id=" + et1rids.get(0);
			for (int i = 1; i < etId1.getRId().size(); i++) {
				evalateStatement = Or(evalateStatement, "Id=" + et1rids.get(i));
			}

			Map<String, String> paras2 = new HashMap<String, String>();
			paras2.put("attributes", "Id,F.FId,J.JId,C.CId,AA.AuId,AA.AfId");
			paras2.put("count", Integer.MAX_VALUE + "");
			JSONObject json2 = MicrosoftAcademicAPI.evaluateMethod(evalateStatement, paras2);
			etRefByID1 = (ArrayList<Entity>) Entities.getEntityList(json2);
		}

		// [Id,RId,]
		if (etId1.getRId() != null && etId1.getRId().indexOf(etId2) != -1) {
			addResult("[" + ID1 + "," + ID2 + "]");
		}
		// [Id,RId,RId,]
		if (etId1.getRId() != null) {
			ArrayList<Long> comm = getEntityIds(etRefID2);
			comm.retainAll(etId1.getRId());
			for (long trid : comm) {
				addResult("[" + ID1 + "," + trid + "," + ID2 + "]");
			}
		}

		// [Id,F_FId,Id,]
		if (etId1.getFId() != null && etId2.getFId() != null) {
			for (long tcomid : getCommItemWithNoChange(etId1.getFId(), etId2.getFId())) {
				addResult("[" + ID1 + "," + tcomid + "," + ID2 + "]");
			}
		}

		// [Id,C_CId,Id,]
		if (etId1.getCCId() != -1 && etId2.getCCId() != -1) {
			if (etId1.getCCId() == etId2.getCCId()) {
				addResult("[" + ID1 + "," + etId1.getCCId() + "," + ID2 + "]");
			}
		}

		// [Id,J_JId,Id,]

		if (etId1.getJJId() != -1 && etId2.getJJId() != -1) {
			if (etId1.getJJId() == etId2.getJJId()) {
				addResult("[" + ID1 + "," + etId1.getJJId() + "," + ID2 + "]");
			}
		}
		// [Id,AA_AuId,Id,]
		if (etId1.getAuId() != null && etId2.getAuId() != null) {
			for (long tcomid : getCommItemWithNoChange(etId1.getAuId(), etId2.getAuId())) {
				addResult("[" + ID1 + "," + tcomid + "," + ID2 + "]");
			}
		}

		// [Id,RId,RId,RId,]
		ArrayList<Long> tidrefByid2 = getEntityIds(etRefID2);
		for (Entity et : etRefByID1) {
			List<Long> trids = et.getRId();
			if (trids != null) {
				trids.retainAll(tidrefByid2);
				for (long tcomrid : trids) {
					addResult("[" + ID1 + "," + et.getId() + "," + tcomrid + "," + ID2 + "]");
				}
			}
		}
		// [Id,RId,F_FId,Id,]
		for (Entity et : etRefByID1) {
			for (long tcomid : getCommItemAndOverrideFirst(et.getFId(), etId2.getFId())) {
				addResult("[" + ID1 + "," + et.getId() + "," + tcomid + "," + ID2 + "]");
			}
		}

		// [Id,RId,C_CId,Id,]
		for (Entity et : etRefByID1) {
			if (et.getCCId() != -1 && et.getCCId() == etId2.getCCId()) {
				addResult("[" + ID1 + "," + et.getId() + "," + et.getCCId() + "," + ID2 + "]");
			}
		}

		// [Id,RId,J_JId,Id,]
		for (Entity et : etRefByID1) {
			if (et.getJJId() != -1 && et.getJJId() == etId2.getJJId()) {
				addResult("[" + ID1 + "," + et.getId() + "," + et.getJJId() + "," + ID2 + "]");
			}
		}

		// [Id,RId,AA_AuId,Id,]
		for (Entity et : etRefByID1) {
			for (long tcomaid : getCommItemAndOverrideFirst(et.getAuId(), etId2.getAuId())) {
				addResult("[" + ID1 + "," + et.getId() + "," + tcomaid + "," + ID2 + "]");
			}
		}

		// [Id,F_FId,Id,RId,]
		// [Id,C_CId,Id,RId,]
		// [Id,J_JId,Id,RId,]
		// [Id,AA_AuId,Id,RId,]
		for (Entity et : etRefID2) {
			List<Long> jihe1_1 = et.getAuId();
			List<Long> jihe2_1 = et.getFId();
			long zhi1_1 = et.getCCId();
			long zhi2_1 = et.getJJId();

			List<Long> jihe1_2 = etId1.getAuId();
			List<Long> jihe2_2 = etId1.getFId();
			long zhi1_2 = etId1.getCCId();
			long zhi2_2 = etId1.getJJId();

			for (long tcom : getCommItemAndOverrideFirst(jihe1_1, jihe1_2)) {
				addResult("[" + ID1 + "," + tcom + "," + et.getId() + "," + ID2 + "]");
			}
			for (long tcom : getCommItemAndOverrideFirst(jihe2_1, jihe2_2)) {
				addResult("[" + ID1 + "," + tcom + "," + et.getId() + "," + ID2 + "]");
			}
			if (zhi1_1 != -1 && zhi1_2 != -1 && zhi1_1 == zhi1_2) {
				addResult("[" + ID1 + "," + zhi1_1 + "," + et.getId() + "," + ID2 + "]");
			}
			if (zhi2_1 != -1 && zhi2_2 != -1 && zhi2_1 == zhi2_2) {
				addResult("[" + ID1 + "," + zhi2_1 + "," + et.getId() + "," + ID2 + "]");
			}
		}
		return endAndGetResult();
	}

	private List<String> getId2AuIdPath() {
		long AID = tail;
		long ID = head;
		startResult();
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

		return null;
	}

	private List<String> getAuId2IdPath() {
		long AID = head;
		long ID = tail;
		startResult();
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
					if (et.getAfId().get(tjid) != -1)
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
				addResult("[" + AID + "," + ID + "]");
				// System.out.println("[" + AID + "," + ID + "]");
			}
		}
		stopWatch.stopAndStart("[AA_AuId,Id,]");
		// [AA_AuId,Id,RId,]
		for (Entity et : PagersWithSpecAuid) {
			if (et.getRId() != null && et.getRId().indexOf(ID) != -1) {
				addResult("[" + AID + "," + et.getId() + "," + ID + "]");
				// System.out.println("[" + AID + "," + et.getId() + "," + ID +
				// "]");
			}
		}
		stopWatch.stopAndStart(" [AA_AuId,Id,RId,]");
		// [AA_AuId,AA_AfId,AA_AuId,Id,]
		if (PaperDest.getAfId() != null) {
			for (int in = 0; in < PaperDest.getAfId().size(); in++) {
				if (AFID.contains(PaperDest.getAfId().get(in))) {
					addResult("[" + AID + "," + PaperDest.getAfId().get(in) + "," + PaperDest.getAuId().get(in) + ","
							+ ID + "]");
					// System.out.println("[" + AID + "," +
					// PaperDest.getAfId().get(in) + "," +
					// PaperDest.getAuId().get(in)
					// + "," + ID + "]");
				}
			}
		}
		stopWatch.stopAndStart(" [AA_AuId,AA_AfId,AA_AuId,Id,]");
		// [AA_AuId,Id,RId,RId,]
		for (Entity et : PagersWithSpecAuid) {
			if (et.getRId() != null) {
				for (Entity et2 : PagersRefSpecId) {
					if (et.getRId().indexOf(et2.getId()) != -1) {
						addResult("[" + AID + "," + et.getId() + "," + et2.getId() + "," + ID + "]");
						// System.out.println("[" + AID + "," + et.getId() + ","
						// + et2.getId() + "," + ID + "]");
					}
				}
			}
		}
		stopWatch.stopAndStart(" [AA_AuId,Id,RId,RId,]");
		// [AA_AuId,Id,F_FId,Id,]
		if (PaperDest != null) {
			for (Entity et : PagersWithSpecAuid) {
				if (et.getFId() != null && PaperDest.getFId() != null) {
					et.getFId().retainAll(PaperDest.getFId());
					for (long tfid : et.getFId()) {
						addResult("[" + AID + "," + et.getId() + "," + tfid + "," + ID + "]");
						// System.out.println("[" + AID + "," + et.getId() + ","
						// + tfid + "," + ID + "]");
					}
				}
				if (et.getAuId() != null && PaperDest.getAuId() != null) {
					et.getAuId().retainAll(PaperDest.getAuId());
					for (long taid : et.getAuId()) {
						addResult("[" + AID + "," + et.getId() + "," + taid + "," + ID + "]");
						// System.out.println("[" + AID + "," + et.getId() + ","
						// + taid + "," + ID + "]");
					}
				}
				if (et.getCCId() != -1 && PaperDest.getCCId() != -1) {
					if (et.getCCId() == PaperDest.getCCId() && PaperDest.getCCId() != -1) {
						addResult("[" + AID + "," + et.getId() + "," + et.getCCId() + "," + ID + "]");
						// System.out.println("[" + AID + "," + et.getId() + ","
						// + et.getCCId() + "," + ID + "]");
					}
				}
				if (et.getJJId() != -1 && PaperDest.getJJId() != -1) {
					if (et.getJJId() == PaperDest.getJJId() && PaperDest.getJJId() != -1) {
						addResult("[" + AID + "," + et.getId() + "," + et.getJJId() + "," + ID + "]");
						// System.out.println("[" + AID + "," + et.getId() + ","
						// + et.getJJId() + "," + ID + "]");
					}
				}
			}
		}
		stopWatch.stopAndStart("[AA_AuId,Id,(FId,AuId,CCId,JJId),Id,]");
		// JSONObject rs=new JSONObject();
		return endAndGetResult();
	}

	public void addResult(String item) {
		result.add(item);
	}

	public List<String> endAndGetResult() {
		return result;
	}

	public void startResult() {
		result.clear();
	}

	private List<Long> getCommItemAndOverrideFirst(List<Long> first, List<Long> second) {
		if (first == null || second == null) {
			return new ArrayList<Long>();
		}
		first.retainAll(second);
		return first;
	}

	private List<Long> getCommItemWithNoChange(List<Long> first, List<Long> second) {
		if (first == null || second == null) {
			return new ArrayList<Long>();
		}
		@SuppressWarnings("unchecked")
		List<Long> comm = (List<Long>) ((ArrayList<Long>) first).clone();
		comm.retainAll(second);
		return comm;
	}

	List<String> result = new ArrayList<String>();

	private List<String> getAuId2AuIdPath() {
		long AID1 = head;
		long AID2 = tail;
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		startResult();

		Map<String, String> paras = new HashMap<String, String>();
		paras.put("attributes", "Id,F.FId,J.JId,C.CId,AA.AuId,AA.AfId,RId");
		paras.put("count", Integer.MAX_VALUE + "");

		JSONObject json = MicrosoftAcademicAPI.evaluateMethod(Composite(Or("AA.AuId=" + AID1, "AA.AuId=" + AID2)),
				paras);
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
					if (et.getAfId().get(tjid1) != -1)
						AFID1.add(et.getAfId().get(tjid1));
				}
				int tjid2 = et.getAuId().indexOf(AID2);
				if (tjid2 != -1) {
					PagersWithAuid2.add(et);
					if (et.getAfId().get(tjid2) != -1)
						AFID2.add(et.getAfId().get(tjid2));
				}
			}
		}
		stopWatch.stopAndStart("整理结果");
		// [AA_AuId,AA_AfId,AA_AuId,]
		@SuppressWarnings("unchecked")
		Set<Long> comAFID = (Set<Long>) AFID1.clone();
		comAFID.retainAll(AFID2);
		for (long s : comAFID) {
			addResult("[" + AID1 + "," + s + "," + AID2 + "]");
			System.out.println("[" + AID1 + "," + s + "," + AID2 + "]");
		}
		stopWatch.stopAndStart("[AA_AuId,AA_AfId,AA_AuId,]");
		// [AA_AuId,Id,AA_AuId,]
		List<Entity> comPaper = (List<Entity>) PagersWithAuid1.clone();
		comPaper.retainAll(PagersWithAuid2);
		for (Entity et : comPaper) {
			addResult("[" + AID1 + "," + et.getId() + "," + AID2 + "]");
			System.out.println("[" + AID1 + "," + et.getId() + "," + AID2 + "]");
		}
		stopWatch.stopAndStart("[AA_AuId,Id,AA_AuId,]");
		// [AA_AuId,Id,RId,AA_AuId,]
		ArrayList<Long> idsWithAuid2 = getEntityIds(PagersWithAuid2);
		for (Entity et : PagersWithAuid1) {
			List<Long> tRIDs = et.getRId();
			if (tRIDs != null) {
				tRIDs.retainAll(idsWithAuid2);
				for (long trid : tRIDs) {
					addResult("[" + AID1 + "," + et.getId() + "," + trid + "," + AID2 + "]");
					System.out.println("[" + AID1 + "," + et.getId() + "," + trid + "," + AID2 + "]");
				}
			}
		}
		stopWatch.stopAndStart("[AA_AuId,Id,RId,AA_AuId,]");
		// JSONObject rs=new JSONObject();
		return endAndGetResult();
	}

	public ArrayList<Long> getEntityIds(List<Entity> ets) {
		ArrayList<Long> tRID = new ArrayList<Long>();
		for (Entity et : ets) {
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
