package server;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MicrosoftAcademicAPI {

	// if some parameters are unnecessary, input "" or -1.
	public JSONObject evaluateMethod(String expr, String model, String attributes, int count, int offset, String orderby) {
		String evaluateURL = "https://api.projectoxford.ai/academic/v1.0/evaluate?expr=" + expr;
		if(!model.equals(""))
			evaluateURL = evaluateURL + "&model=" + model;
		if(!attributes.equals(""))
			evaluateURL = evaluateURL + "&attributes=" + attributes;
		if(count != -1)
			evaluateURL = evaluateURL + "&count=" + count;
		if(offset != -1)
			evaluateURL = evaluateURL + "&offset=" + offset;
		if(!orderby.equals(""))
			evaluateURL = evaluateURL + "&orderby=" + orderby;
		
		String result = MicrosoftAcademicClient.getInstance().get(evaluateURL);
		JSONObject JSONResult = new JSONObject(result);
		return null;
	}

	// if some parameters are unnecessary, input "" or -1.
	public JSONObject calcHistogramMethod(String expr, String model, String attributes, int count, int offset) {
		String calcHistogramURL = "https://api.projectoxford.ai/academic/v1.0/evaluate?";
		return null;
	}
}
