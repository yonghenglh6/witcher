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
		stopWatch.stopAndStart("getId2IdPath===========================================");
		startResult();

		// 1次查询
		Map<String, String> paras1 = new HashMap<String, String>();
		paras1.put("attributes", "Id,F.FId,J.JId,C.CId,AA.AuId,AA.AfId,RId");
		paras1.put("count", Integer.MAX_VALUE + "");
		JSONObject json1 = MicrosoftAcademicAPI.evaluateMethod(Or(Or("Id=" + ID1, "Id=" + ID2), "RId=" + ID2), paras1);

		stopWatch.stopAndStart("1次查询");

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
		stopWatch.stopAndStart("1次整理结果");
		// 所求不是指定的类型
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
		stopWatch.stopAndStart("2次查询并整理结果");
		// [Id,RId,]
		if (etId1.getRId() != null && etId1.getRId().indexOf(etId2) != -1) {
			addResult("[" + ID1 + "," + ID2 + "]");
		}
		stopWatch.stopAndStart("[Id,RId,]");
		// [Id,RId,RId,]
		if (etId1.getRId() != null) {
			ArrayList<Long> comm = getEntityIds(etRefID2);
			comm.retainAll(etId1.getRId());
			for (long trid : comm) {
				addResult("[" + ID1 + "," + trid + "," + ID2 + "]");
			}
		}
		stopWatch.stopAndStart(" [Id,RId,RId,]");
		// [Id,F_FId,Id,]
		if (etId1.getFId() != null && etId2.getFId() != null) {
			for (long tcomid : getCommItemWithNoChange(etId1.getFId(), etId2.getFId())) {
				addResult("[" + ID1 + "," + tcomid + "," + ID2 + "]");
			}
		}
		stopWatch.stopAndStart("[Id,F_FId,Id,]");
		// [Id,C_CId,Id,]
		if (etId1.getCCId() != -1 && etId2.getCCId() != -1) {
			if (etId1.getCCId() == etId2.getCCId()) {
				addResult("[" + ID1 + "," + etId1.getCCId() + "," + ID2 + "]");
			}
		}
		stopWatch.stopAndStart("[Id,C_CId,Id,]");
		// [Id,J_JId,Id,]

		if (etId1.getJJId() != -1 && etId2.getJJId() != -1) {
			if (etId1.getJJId() == etId2.getJJId()) {
				addResult("[" + ID1 + "," + etId1.getJJId() + "," + ID2 + "]");
			}
		}
		stopWatch.stopAndStart(" [Id,J_JId,Id,]");
		// [Id,AA_AuId,Id,]
		if (etId1.getAuId() != null && etId2.getAuId() != null) {
			for (long tcomid : getCommItemWithNoChange(etId1.getAuId(), etId2.getAuId())) {
				addResult("[" + ID1 + "," + tcomid + "," + ID2 + "]");
			}
		}
		stopWatch.stopAndStart("[Id,AA_AuId,Id,]");
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
		stopWatch.stopAndStart("[Id,RId,RId,RId,]");
		// [Id,RId,F_FId,Id,]
		for (Entity et : etRefByID1) {
			for (long tcomid : getCommItemAndOverrideFirst(et.getFId(), etId2.getFId())) {
				addResult("[" + ID1 + "," + et.getId() + "," + tcomid + "," + ID2 + "]");
			}
		}
		stopWatch.stopAndStart("[Id,RId,F_FId,Id,]");
		// [Id,RId,C_CId,Id,]
		for (Entity et : etRefByID1) {
			if (et.getCCId() != -1 && et.getCCId() == etId2.getCCId()) {
				addResult("[" + ID1 + "," + et.getId() + "," + et.getCCId() + "," + ID2 + "]");
			}
		}
		stopWatch.stopAndStart("[Id,RId,C_CId,Id,]");
		// [Id,RId,J_JId,Id,]
		for (Entity et : etRefByID1) {
			if (et.getJJId() != -1 && et.getJJId() == etId2.getJJId()) {
				addResult("[" + ID1 + "," + et.getId() + "," + et.getJJId() + "," + ID2 + "]");
			}
		}
		stopWatch.stopAndStart("[Id,RId,J_JId,Id,]");
		// [Id,RId,AA_AuId,Id,]
		for (Entity et : etRefByID1) {
			for (long tcomaid : getCommItemAndOverrideFirst(et.getAuId(), etId2.getAuId())) {
				addResult("[" + ID1 + "," + et.getId() + "," + tcomaid + "," + ID2 + "]");
			}
		}
		stopWatch.stopAndStart("[Id,RId,AA_AuId,Id,]");
		// [Id,F_FId,Id,RId,]
		// [Id,C_CId,Id,RId,]
		// [Id,J_JId,Id,RId,]
		// [Id,AA_AuId,Id,RId,]
		for (Entity et : etRefID2) {
			for (long tcom : getCommItemAndOverrideFirst(et.getAuId(), etId1.getAuId())) {
				addResult("[" + ID1 + "," + tcom + "," + et.getId() + "," + ID2 + "]");
			}
			for (long tcom : getCommItemAndOverrideFirst(et.getFId(), etId1.getFId())) {
				addResult("[" + ID1 + "," + tcom + "," + et.getId() + "," + ID2 + "]");
			}
			if (et.getCCId() != -1 && et.getCCId() == etId1.getCCId()) {
				addResult("[" + ID1 + "," + et.getCCId() + "," + et.getId() + "," + ID2 + "]");
			}
			if (et.getJJId() != -1 && et.getJJId() == etId1.getJJId()) {
				addResult("[" + ID1 + "," + et.getJJId() + "," + et.getId() + "," + ID2 + "]");
			}
		}
		stopWatch.stopAndStart("[Id,F_FId...,Id,RId,]");
		return endAndGetResult();
	}

	private List<String> getId2AuIdPath() {
		long AID = tail;
		long ID = head;
		startResult();

		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		stopWatch.stopAndStart("getId2AuIdPath===========================================");
		// 第一次查询
		Map<String, String> paras = new HashMap<String, String>();
		paras.put("attributes", "Id,F.FId,J.JId,C.CId,AA.AuId,AA.AfId,RId");
		paras.put("count", Integer.MAX_VALUE + "");
		JSONObject json = MicrosoftAcademicAPI.evaluateMethod(Or(Composite("AA.AuId=" + AID), "Id=" + ID), paras);
		// json = MicrosoftAcademicAPI.evaluateMethod(Or(Composite("AA.AuId=" +
		// AID), "Id=" + ID), paras);
		stopWatch.stopAndStart("查询");
		List<Entity> entitys = Entities.getEntityList(json);

		stopWatch.stopAndStart("分析结果");
		Entity etId = null;
		List<Entity> PagersWithSpecAuid = new ArrayList<Entity>();

		Set<Long> AFID = new HashSet<Long>();
		for (Entity et : entitys) {
			if (et.getId() == ID) {
				etId = et;
			}
			if (et.getAuId() != null && et.getAuId().contains(AID)) {
				PagersWithSpecAuid.add(et);
				AFID.add(et.getAfId().get(et.getAuId().indexOf(AID)));
			}
		}

		stopWatch.stopAndStart("分类返回数据");

		// 所求不是指定的类型
		if (etId == null || PagersWithSpecAuid.size() == 0)
			return new ArrayList<String>();

		List<Entity> etRefById = new ArrayList<Entity>();

		// List<Entity> etWithSameAuthor = new ArrayList<Entity>();
		// 第二次查询
		// 2次查询
		List<Long> et1rids = etId.getRId();
		String evalateStatement = "";
		if (et1rids != null && et1rids.size() > 0) {
			evalateStatement = "Id=" + et1rids.get(0);
			for (int i = 1; i < et1rids.size(); i++) {
				evalateStatement = Or(evalateStatement, "Id=" + et1rids.get(i));
			}
		}
		List<Long> et1auids = etId.getAuId();
		if (et1auids != null && et1auids.size() > 0) {
			String tEvalueStateMent = "AA.AuId=" + et1auids.get(0);
			;
			for (int i = 1; i < et1auids.size(); i++) {
				tEvalueStateMent = Or(tEvalueStateMent, "AA.AuId=" + et1auids.get(i));
			}
			tEvalueStateMent = Composite(tEvalueStateMent);
			if (evalateStatement.equals("")) {
				evalateStatement = tEvalueStateMent;
			} else {
				evalateStatement = Or(evalateStatement, tEvalueStateMent);
			}
		}
		stopWatch.stopAndStart("2次查询");
		Map<Long, Set<Long>> AFIDMap = new HashMap<Long, Set<Long>>();
		if (!evalateStatement.equals("")) {
			Map<String, String> paras2 = new HashMap<String, String>();
			paras2.put("attributes", "Id,F.FId,J.JId,C.CId,AA.AuId,AA.AfId");
			paras2.put("count", Integer.MAX_VALUE + "");
			JSONObject json2 = MicrosoftAcademicAPI.evaluateMethod(evalateStatement, paras2);
			ArrayList<Entity> entities2 = (ArrayList<Entity>) Entities.getEntityList(json2);
			for (Entity et : entities2) {
				if (et1rids.contains(et.getId())) {
					etRefById.add(et);
				}
				if (et.getAuId() != null) {
					for (int i = 0; i < et.getAuId().size(); i++) {
						long ttauid = et.getAuId().get(i);
						long ttafid = et.getAfId().get(i);
						if (et1auids.contains(ttauid)) {
							if (!AFIDMap.containsKey(ttauid)) {
								AFIDMap.put(ttauid, new HashSet<Long>());
							}
							AFIDMap.get(ttauid).add(ttafid);
						}
					}
				}
			}
		}

		stopWatch.stop("2次分类");

		// [Id,AA_AuId,]
		if (PagersWithSpecAuid.contains(etId)) {
			addResult("[" + ID + "," + AID + "]");
		}
		stopWatch.stopAndStart("[Id,AA_AuId,]");
		// [Id,RId,AA_AuId,]
		if (etId.getRId() != null) {
			List<Long> headids = getEntityIds(PagersWithSpecAuid);
			for (Entity et : PagersWithSpecAuid) {
				for(long trid:getCommItemAndOverrideFirst(headids, etId.getRId())){
					addResult("[" + ID + "," + trid + "," + AID + "]");
				}
			}
		}
		stopWatch.stopAndStart("[Id,RId,AA_AuId,]");
		// [Id,RId,RId,AA_AuId,]
		// 这儿需要二次查询，到底查不查。另外有两种查询顺序，要根据直方图决定如何查。
		List<Long> headids = getEntityIds(PagersWithSpecAuid);
		for (Entity et : etRefById) {
			if (et.getRId() != null) {
				for (long trid : getCommItemAndOverrideFirst(et.getRId(), headids)) {
					addResult("[" + ID + "," + et.getId() + "," + trid + "," + AID + "]");
				}
			}
		}
		stopWatch.stopAndStart("[Id,RId,RId,AA_AuId,]");
		// [Id,AA_AuId,AA_AfId,AA_AuId,]
		if (AFIDMap.size() == 0) {
			if (etId.getAuId() != null) {
				for (int i = 0; i < etId.getAuId().size(); i++) {
					if (AFID.contains(etId.getAfId().get(i))) {
						addResult(
								"[" + ID + "," + etId.getAuId().get(i) + "," + etId.getAfId().get(i) + "," + AID + "]");
					}
				}
			}
		} else {
			for (long taid : AFIDMap.keySet()) {
				AFIDMap.get(taid).retainAll(AFID);
				for (long tafid : AFIDMap.get(taid)) {
					addResult("[" + ID + "," + taid + "," + tafid + "," + AID + "]");
				}
			}
		}
		stopWatch.stopAndStart("[Id,AA_AuId,AA_AfId,AA_AuId,]");
		// [Id,F_FId,Id,AA_AuId,]
		// [Id,C_CId,Id,AA_AuId,]
		// [Id,J_JId,Id,AA_AuId,]
		// [Id,AA_AuId,Id,AA_AuId,]
		for (Entity et : PagersWithSpecAuid) {
			for (long tcomm : getCommItemAndOverrideFirst(et.getFId(), etId.getFId())) {
				addResult("[" + ID + "," + tcomm + "," + et.getId() + "," + AID + "]");
			}
			for (long tcomm : getCommItemAndOverrideFirst(et.getAuId(), etId.getAuId())) {
				addResult("[" + ID + "," + tcomm + "," + et.getId() + "," + AID + "]");
			}
			if (et.getCCId() != -1 && et.getCCId() == etId.getCCId()) {
				addResult("[" + ID + "," + et.getCCId() + "," + et.getId() + "," + AID + "]");
			}
			if (et.getJJId() != -1 && et.getJJId() == etId.getJJId()) {
				addResult("[" + ID + "," + et.getJJId() + "," + et.getId() + "," + AID + "]");
			}
		}
		stopWatch.stopAndStart("[Id,F_FId...,Id,AA_AuId,]");
		return endAndGetResult();
	}

	private List<String> getAuId2IdPath() {
		long AID = head;
		long ID = tail;
		startResult();
		StopWatch stopWatch = new StopWatch();

		Map<String, String> paras = new HashMap<String, String>();
		paras.put("attributes", "Id,F.FId,J.JId,C.CId,AA.AuId,AA.AfId,RId");
		paras.put("count", Integer.MAX_VALUE + "");
		stopWatch.start();
		stopWatch.stopAndStart("getAuId2IdPath===========================================");
		JSONObject json = MicrosoftAcademicAPI
				.evaluateMethod(Or(Or(Composite("AA.AuId=" + AID), "Id=" + ID), "RId=" + ID), paras);
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
		// 所求不是指定的类型
		if (PaperDest == null || PagersWithSpecAuid.size() == 0)
			return new ArrayList<String>();

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
		System.out.println(item);
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
		stopWatch.stopAndStart("getAuId2AuIdPath===========================================");
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

		// 所求不是指定的类型
		if (PagersWithAuid1.size() == 0 || PagersWithAuid2.size() == 0)
			return new ArrayList<String>();

		// [AA_AuId,AA_AfId,AA_AuId,]
		@SuppressWarnings("unchecked")
		Set<Long> comAFID = (Set<Long>) AFID1.clone();
		comAFID.retainAll(AFID2);
		for (long s : comAFID) {
			addResult("[" + AID1 + "," + s + "," + AID2 + "]");
			// System.out.println("[" + AID1 + "," + s + "," + AID2 + "]");
		}
		stopWatch.stopAndStart("[AA_AuId,AA_AfId,AA_AuId,]");
		// [AA_AuId,Id,AA_AuId,]
		List<Entity> comPaper = (List<Entity>) PagersWithAuid1.clone();
		comPaper.retainAll(PagersWithAuid2);
		for (Entity et : comPaper) {
			addResult("[" + AID1 + "," + et.getId() + "," + AID2 + "]");
			// System.out.println("[" + AID1 + "," + et.getId() + "," + AID2 +
			// "]");
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
					// System.out.println("[" + AID1 + "," + et.getId() + "," +
					// trid + "," + AID2 + "]");
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
