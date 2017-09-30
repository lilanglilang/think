package com.inspur.sinaspider;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;

public class SinaLoginUtil {
	/**
	 * 登录获取后的cookies
	 * 
	 * @param username
	 * @param password
	 * @return cookies
	 */
	@SuppressWarnings("deprecation")
	public static String requestAccessTicket(String username, String password)
			throws MalformedURLException, IOException {
		username = Base64.encodeBase64String(username.replace("@", "%40")
				.getBytes());
		HttpURLConnection conn = (HttpURLConnection) new URL(
				"https://login.sina.com.cn/sso/login.php?client=ssologin.js(v1.4.15)")
				.openConnection();
		conn.setDoInput(true);
		conn.setDoOutput(true);
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Referer",
				"http://login.sina.com.cn/signup/signin.php?entry=sso");
		conn.setRequestProperty(
				"User-Agent",
				"Mozilla/5.0 (Macintosh; Intel Mac OS X 10.10; rv:34.0) Gecko/20100101 Firefox/34.0");
		conn.setRequestProperty("Content-Type",
				"application/x-www-form-urlencoded");
		DataOutputStream out = new DataOutputStream(conn.getOutputStream());
		out.writeBytes(String
				.format("entry=sso&gateway=1&from=null&savestate=30&useticket=0&pagerefer=&vsnf=1&su=%s&service=sso&sp=%s&sr=1280*800&encoding=UTF-8&cdult=3&domain=sina.com.cn&prelt=0&returntype=TEXT",
						URLEncoder.encode(username), password));
		out.flush();
		out.close();
		BufferedReader read = new BufferedReader(new InputStreamReader(
				conn.getInputStream(), "gbk"));
		String line = null;
		StringBuilder ret = new StringBuilder();
		while ((line = read.readLine()) != null) {
			ret.append(line).append("\n");
		}
		String res = null;
		try {
			res = ret.substring(ret.indexOf("https:"),
					ret.indexOf(",\"https:") - 1).replace("\\", "");
		} catch (Exception e) {
			res = "false";
		}
		return res;
	}
	
	
	  /**
	  * 把cookies放进请求里
	  * 默认传进来的cookie为空
	  * List<String> cookieList = new ArrayList<String>();
	  * properties = PropertiesUtil.getProperties("sina.properties");
	  *users=SinaLoginUtil.userContainer(properties.getProperty("username"), properties.getProperty("password"));
	  * users=SinaLoginUtil.userContainer(properties.getProperty("username"), properties.getProperty("password"));
	  *			for (Map.Entry<String, String> entry : users.entrySet()) {  
	  *				    String user = SinaLoginUtil.requestAccessTicket(entry.getKey(), entry.getValue());
	  *				    Thread.sleep(1000);
	  *				    String cookies = SinaLoginUtil.sendGetRequest(user, null);			
	  *				    cookieList.add(cookies);			    
	  *				    logger.info(entry.getKey()+"微博账号获取中...");
	  *				}  
	  *
	  *
	  * 
	 * @param url
	 * @param cookies
	 */
	public static String sendGetRequest(String url, String cookies)
			throws MalformedURLException, IOException {
		HttpURLConnection conn = (HttpURLConnection) new URL(url)
				.openConnection();
		conn.setRequestProperty("Cookie", cookies);
		conn.setRequestProperty("Referer",
				"http://login.sina.com.cn/signup/signin.php?entry=sso");
		conn.setRequestProperty(
				"User-Agent",
				"Mozilla/5.0 (Macintosh; Intel Mac OS X 10.10; rv:34.0) Gecko/20100101 Firefox/34.0");
		conn.setRequestProperty("Content-Type",
				"application/x-www-form-urlencoded");
		BufferedReader read = new BufferedReader(new InputStreamReader(
				conn.getInputStream(), "gbk"));
		String line = null;
		StringBuilder ret = new StringBuilder();
		while ((line = read.readLine()) != null) {
			ret.append(line).append("\n");
		}
		StringBuilder ck = new StringBuilder();
		try {
			for (String s : conn.getHeaderFields().get("Set-Cookie")) {
				ck.append(s.split(";")[0]).append(";");
			}

		} catch (Exception e) {
		}
		return ck.toString();
	}
	
	public static Map<String,String> userContainer(String names,String passwords){
		Map<String,String> container = new HashMap<String,String>();
		String[] nameArry = names.split(",");
		String[] passArry = passwords.split(",");
		for(int i=0;i<nameArry.length;i++){
			container.put(nameArry[i], passArry[i]);
		}
		return container;
	}
}
