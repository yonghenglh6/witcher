package model;

import java.util.List;

import model.Attribute.TYPE;

public class LSearch {
	Rules rules;
	public LSearch(){
		rules=new Rules();
	}
	public void search(Attribute a,Attribute b){
		TYPE aType=a.type;
		TYPE bType=b.type;
		//1hop
		List<List<TYPE>> onepaths=rules.getPath(aType, bType, 1);
		
		
		//2hop
		List<List<TYPE>> twopaths=rules.getPath(aType, bType, 2);
		
		//3hop
		List<List<TYPE>> threepaths=rules.getPath(aType, bType, 3);
	}
	

	public static void main(String atgs[]){
		LSearch s=new LSearch();
		s.search(new Attribute(TYPE.Id,2157025439L), new Attribute(TYPE.AA_AuId,2117829666L));
	}
}
