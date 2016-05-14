package server;

import model.Attribute.TYPE;

public class TestSearch {
	public static void oneHopId_ID(){
		long Id1=2157025439L;
		long Id2=2122841972L;
		Search search=new Search(TYPE.Id,Id1,TYPE.Id,Id2);
		search.getPath();
	}
	public static void twoHopId_ID(){
		long Id1=2157025439L;
		long Id2=2117829666L;
		long IdMid=2122841972L;
		Search search=new Search(TYPE.Id,Id1,TYPE.Id,Id2);
		search.getPath();
	}
	
	public static void AuId2Id1hop(){
		long AID=2052648321L;
		long ID=2066636486L;
		Search search=new Search(TYPE.AA_AuId,AID,TYPE.Id,ID);
		search.getPath();
	}
	public static void AuId2Id2hop(){
		
	}
	public static void AuId2Id3hop(){
//		long AID=2052648321L;
//		long ID=2037965136L;
//		long RID=2166559705L;
//		long RID2=2100406636L;
//		SearchWrapper.search(TYPE.AA_AuId, AID, TYPE.Id, RID2);
		
		long AID=2052648321L;
		long AID2=2041650587L;
		long ID=1511277043L;
		long AFID=97018004L;
		SearchWrapper.search(TYPE.AA_AuId, AID, TYPE.Id, ID);
		
		
	}
	public static void AuId2Id(){
		AuId2Id1hop();
		AuId2Id2hop();
		AuId2Id3hop();
	}
	
	public static void main(String[] args){
		//twoHopId_ID();
		//oneHopId_ID();
		AuId2Id3hop();
	}
}
