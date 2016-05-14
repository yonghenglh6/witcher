package dealer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import model.Attribute.TYPE;
import server.SearchWrapper;

public class Dealer extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		Map<String, String[]> pmaps = req.getParameterMap();
		String out = "";
		for (String key : pmaps.keySet()) {
			String[] values = pmaps.get(key);
			out += key + ":" + values.length + "[";
			for (String value : values) {
				out += value + ",";
			}
			out += "].\n";
			FileWriter fw = new FileWriter("d:/input.txt", true);
			fw.append(out);
			fw.flush();
			fw.close();
		}

		try {
			String id1str = pmaps.get("id1")[0];
			String id2str = pmaps.get("id2")[0];
			long id1 = Long.valueOf(id1str);
			long id2 = Long.valueOf(id2str);
			List<String> list1 = SearchWrapper.search(TYPE.Id, id1, TYPE.Id, id2);
			List<String> list2 = SearchWrapper.search(TYPE.AA_AuId, id1, TYPE.Id, id2);
			List<String> list3 = SearchWrapper.search(TYPE.Id, id1, TYPE.AA_AuId, id2);
			List<String> list4 = SearchWrapper.search(TYPE.AA_AuId, id1, TYPE.AA_AuId, id2);
			if(list2!=null)
			list1.addAll(list2);
			if(list3!=null)
			list1.addAll(list3);
			if(list4!=null)
			list1.addAll(list4);
			String rs = "[";
			boolean first = true;
			for (String tts : list1) {
				if (first) {
					first = false;
					rs+=tts;
				} else {
					rs+=","+tts;
				}
			}
			rs+="]";
			resp.getOutputStream().print(rs);
		} catch (Exception e) {
			e.printStackTrace();
		}
		resp.getOutputStream().print("[]");
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.getOutputStream().print("{Die Now.Post}");
	}
}
