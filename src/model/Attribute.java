package model;

public class Attribute {
	public static enum TYPE {
		Id, Ti, Y, D, CC, AA_AuN, AA_AuId, AA_AfN, AA_AfId, F_FN, F_FId, J_JN, J_JId, C_CN, C_CId, RId, W, E,
	}
	static String[] TYPE_NAME = new String[] { "Id", "Ti", "Y", "D", "CC", "AA.AuN", "AA.AuId", "AA.AfN", "AA.AfId", "F.FN",
			"F.FId", "J.JN", "J.JId", "C.CN", "C.CId", "RId", "W", "E" };

	public static String toTypeName(TYPE t){
		return TYPE_NAME[t.ordinal()];
	}
	
	
	TYPE type;
	String value;
	
	
	public static void main(String args[]) {
		System.out.println(Attribute.TYPE.Id.ordinal());
	}
}
