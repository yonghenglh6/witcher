package model;

public class Attribute {
	public static enum TYPE {
		Id, AA_AuId, AA_AfId, F_FId, J_JId, C_CId, RId
	}

	static String[] TYPE_NAME = new String[] { "Id", "AA.AuId", "AA.AfId", "F.FId", "J.JId", "C.CId", "RId" };

	public static String toTypeName(TYPE t) {
		return TYPE_NAME[t.ordinal()];
	}

	TYPE type;
	long value;

	public static void main(String args[]) {
		System.out.println(Attribute.TYPE.Id.ordinal());
	}

	public Attribute(TYPE type, long value) {
		this.type = type;
		this.value = value;
	}
}
