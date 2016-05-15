package dealer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
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
			out += "]";

		}
		FileWriter fw = new FileWriter("d:/input.txt", true);
		fw.append(out + "\n======");
		fw.flush();
		fw.close();

		try {
//			String id1str = "57898110", id2str = "2014261844";//auid2auid 27
//			String id1str = "2332023333", id2str = "57898110";//id2auid   6
//			String id1str = "2332023333", id2str = "2310280492";//id2id   1
			String id1str = "2147152072", id2str = "189831743";//id2id    9
//			String id1str = "2251253715", id2str = "2180737804";//auid2id 14
			if (pmaps.get("id1") != null)
				id1str = pmaps.get("id1")[0];
			if (pmaps.get("id2") != null)
				id2str = pmaps.get("id2")[0];
			long id1 = Long.valueOf(id1str);
			long id2 = Long.valueOf(id2str);
			List<String> mrs = SearchWrapper.search(id1,  id2);
			
			String rs = "[";
			boolean first = true;
			for (String tts : mrs) {
				if (first) {
					first = false;
					rs += tts;
				} else {
					rs += "," + tts;
				}
			}
			rs += "]";
			resp.getOutputStream().print(rs);
		} catch (Exception e) {
			e.printStackTrace();
			resp.getOutputStream().print("[]");
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.getOutputStream().print("{Die Now.Post}");
	}
}
