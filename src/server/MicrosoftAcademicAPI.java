package server;

import java.util.Map;

//import org.json.JSONArray;
//import org.json.JSONException;
import org.json.JSONObject;

public class MicrosoftAcademicAPI {

	public static JSONObject evaluateMethod(String expr, Map<String, String> params) {
		params.put("expr", expr);
		return baseMethod(MicrosoftAcademicClient.URL_EVALUATE, params);
	}

	public static JSONObject calcHistogramMethod(String expr, Map<String, String> params) {
		params.put("expr", expr);
		return baseMethod(MicrosoftAcademicClient.URL_CALCHISTOGRAM, params);
	}

	private static JSONObject baseMethod(String baseURL, Map<String, String> params) {
		String strResult = MicrosoftAcademicClient.getInstance().get(baseURL, params);
		JSONObject JSONResult = new JSONObject(strResult);
		return JSONResult;
	}

//	public static void main(String args[]) {
//		Map<String, String> pm = new HashMap<String, String>();
//		pm.put("expr", "Composite(AA.AuN=='jaime teevan')");
//		pm.put("attributes", "Ti,Y,CC,AA.AuN,AA.AuId");
//		Map<String, String> result = baseMethod(MicrosoftAcademicClient.URL_EVALUATE, pm);
//		for (String key : result.keySet()) {
//			System.out.println(key + ": " + result.get(key));
//		}
//	}
}
