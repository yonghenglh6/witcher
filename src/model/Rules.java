package model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import model.Attribute.TYPE;

public class Rules {
	public List<TYPE> getTypeOnePath(TYPE t) {
		List<TYPE> result = new ArrayList<TYPE>();
		if (t == TYPE.Id || t == TYPE.RId) {
			result.add(TYPE.RId);
			result.add(TYPE.F_FId);
			result.add(TYPE.C_CId);
			result.add(TYPE.J_JId);
			result.add(TYPE.AA_AuId);
		}
		if (t == TYPE.F_FId) {
			result.add(TYPE.Id);
		}
		if (t == TYPE.C_CId) {
			result.add(TYPE.Id);
		}
		if (t == TYPE.J_JId) {
			result.add(TYPE.Id);
		}
		if (t == TYPE.AA_AuId) {
			result.add(TYPE.AA_AfId);
			result.add(TYPE.Id);
		}
		if (t == TYPE.AA_AfId) {
			result.add(TYPE.AA_AuId);
		}
		return result;
	}
	
	TYPE[][] ID_ID_1=new TYPE[][]{{TYPE.Id,TYPE.RId}};
	TYPE[][] ID_ID_2=new TYPE[][]{{TYPE.Id,TYPE.RId,}};
	
	public List<List<TYPE>> getPaths(List<List<TYPE>> from, TYPE dest, int hop) {
		List<List<TYPE>> rs=new ArrayList<List<TYPE>>();
		if(hop==0){
			Iterator<List<TYPE>> it=from.iterator();
			while(it.hasNext()){
				List<TYPE> list=it.next();
				TYPE last=list.get(list.size()-1);
				if(last==dest||(dest==TYPE.Id&&last==TYPE.RId)){
					System.out.print("[");
					for(TYPE mk:list){
						System.out.print(mk+",");
					}
					System.out.println("]");
					rs.add(list);
				}
			}
			return rs;
		}
		Iterator<List<TYPE>> it=from.iterator();
		while(it.hasNext()){
			List<TYPE> list=it.next();
			TYPE last=list.get(list.size()-1);
			List<TYPE> nexts=getTypeOnePath(last);
			for(TYPE next:nexts){
				List<TYPE> apath=new ArrayList<TYPE>();
				apath.addAll(list);
				apath.add(next);
				rs.add(apath);
			}
		}
		return getPaths(rs,dest,hop-1);
	}
	
	public List<List<TYPE>> getPath(TYPE oType,TYPE eType,int hop){
		List<List<TYPE>> begin=new ArrayList<List<TYPE>>();
		List<TYPE> ori=new ArrayList<TYPE>();
		ori.add(oType);
		begin.add(ori);
		return getPaths(begin, eType, hop);
	}
	
	public static void main(String[] args){
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		Rules rules=new Rules();
		List<List<TYPE>> begin=new ArrayList<List<TYPE>>();
		List<TYPE> ori=new ArrayList<TYPE>();
		ori.add(TYPE.Id);
		begin.add(ori);
		List<List<TYPE>> kk=rules.getPaths(begin, TYPE.Id, 3);
		stopWatch.stop("generatePath");
		
	}
}
