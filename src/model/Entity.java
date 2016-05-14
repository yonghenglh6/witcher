package model;

import java.util.List;

import org.json.JSONObject;

import model.Attribute.TYPE;

public class Entity {
	private JSONObject json;
	private int Id;
	private List<Integer> FFId;
	private List<Integer> JJId;
	private List<Integer> CCId;
	private List<Integer> AuId;
	private List<Integer> AfId;
	private List<Integer> RId;
	
	public Entity(JSONObject j) {
		json = j;
	}
	
	public int getId() {
		return Id;
	}
	
	public List<Integer> getEntity(TYPE type) {
		switch(type) {
		case F_FId:
			return FFId;
		case J_JId:
			return JJId;
		case C_CId:
			return CCId;
		case AA_AuId:
			return AuId;
		case AA_AfId:
			return AfId;
		case RId:
			return RId;
		}
		return null;
	}
}
