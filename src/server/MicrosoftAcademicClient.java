package server;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.security.cert.CertificateException;
import javax.security.cert.X509Certificate;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

@SuppressWarnings("deprecation")
public class MicrosoftAcademicClient {
	static MicrosoftAcademicClient self;

	public static MicrosoftAcademicClient getInstance() {
		if (self == null) {
			self = new MicrosoftAcademicClient();
		}
		return self;
	}

	private static CloseableHttpClient createSSLClientDefault() {
		try {
			SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
				// 信任所有
				@SuppressWarnings("unused")
				public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
					return true;
				}

				@Override
				public boolean isTrusted(java.security.cert.X509Certificate[] arg0, String arg1)
						throws java.security.cert.CertificateException {
					// TODO Auto-generated method stub
					return false;
				}

			}).build();
			SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext);

			return HttpClients.custom().setSSLSocketFactory(sslsf).build();

		} catch (KeyManagementException e) {

			e.printStackTrace();

		} catch (NoSuchAlgorithmException e) {

			e.printStackTrace();

		} catch (KeyStoreException e) {

			e.printStackTrace();

		}
		return HttpClients.createDefault();

	}
	private MicrosoftAcademicClient() {

	}
	public static String encodeParam(String param) {
		try {
			return java.net.URLEncoder.encode(param, "utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return "";
	}

	public String get(String url) {
		CloseableHttpClient httpclient = createSSLClientDefault();
		HttpGet httpGet = new HttpGet();
		try {
			httpGet.setHeader("Ocp-Apim-Subscription-Key", KEY1);
			httpGet.setURI(new URI(url));
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
		CloseableHttpResponse response1 = null;
		String result = null;
		try {
			try {
				response1 = httpclient.execute(httpGet);
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			System.out.println(response1.getStatusLine());
			HttpEntity entity1 = response1.getEntity();
			try {
				result = EntityUtils.toString(entity1);
				EntityUtils.consume(entity1);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} finally {
			if (response1 != null)
				try {
					response1.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			if(httpclient!=null)
				try {
					httpclient.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		return result;
	}

	public static final String URL_EVALUATE = "https://api.projectoxford.ai/academic/v1.0/evaluate";
	public static final String URL_CALCHISTOGRAM = "https://api.projectoxford.ai/academic/v1.0/calchistogram";
	public static final String URL_INTERPRET = "https://api.projectoxford.ai/academic/v1.0/interpret";
	private static final String KEY1 = "aa006460ea674d9287b2302218969fc8";
	//private static final String KEY2 = "b23b122400e04defa8d330567cb04f7b";

	public String get(String baseUrl, Map<String, String> params) {
		String url = baseUrl + "?";
		for (String key : params.keySet()) {
			String value = encodeParam(params.get(key));
			url += key + "=" + value + "&";
		}
		System.out.println("fetch from:"+url);
		return get(url);
	}

	public static void main(String args[]) {

		Map<String, String> pm = new HashMap<String, String>();
		pm.put("expr", "Composite(AA.AuN=='jaime teevan')");
		pm.put("count", "2");
		pm.put("attributes", "Ti,Y,CC,AA.AuN,AA.AuId");

		String rs1 = MicrosoftAcademicClient.getInstance().get(URL_EVALUATE, pm);
		System.out.println("result:");
		System.out.println(rs1);
	}
}
