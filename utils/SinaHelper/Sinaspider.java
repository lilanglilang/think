import java.io.File;
import java.io.IOException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import com.alibaba.fastjson.JSON;
public class Sinaspider {
	private static Logger logger = Logger.getLogger(Sinaspider.class);
	
	/**
	 * 获取信息方法
	 * @param url  
	 * @param cookies
	 * @param fileUrl
	 * @throws IOException
	 */
	public static void getFoundcontent(List<String> cookies,Properties properties,String keyword,String pinyin,String updateTime,String provName) throws Exception{
			ArrayList<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
			Map<String,String> headerParameter = new HashMap<String,String>();
			String url = String.format((String)properties.get("url"), CommonUtil.urlEncodeUTF8(keyword));
			int pageSize=0;
			int back=0;
			for(int i=0;i<cookies.size();i++){
				headerParameter.put("Cookie", cookies.get(i));
				headerParameter.put("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
				Document doc = getDocument(url+"1",headerParameter,Integer.valueOf((String)properties.get("timeout")));
				pageSize = doc.select("div[class=layer_menu_list W_scroll]").select("ul").select("li").size();
				
				if(pageSize!=0){
					break;
				}else if(doc!=null&&!"".equals(doc.html())){
					String title= doc.getElementsByTag("title").text();
					if("新浪通行证".equals(title)){
						String script=doc.select("meta[http-equiv='refresh']").attr("content");
						int start = script.indexOf("=\'")+2;
						int end = script.lastIndexOf("'");
						Document d = getDocument(script.substring(start,end),headerParameter,Integer.valueOf((String)properties.get("timeout")));
						if(d.html()!=null){
							continue;
						}
					}
				}
				logger.error("账号"+i+"异常:"+doc.html());
			}
			
			if(pageSize==0){
				throw new Exception("账号异常！");
			}
			
			
			//-------------------------列表页-----------------------//
			logger.info("获取列表页");
			list=pageList(url,list,headerParameter,properties,cookies,"1",pageSize,pinyin);
			list=getListPage("document",list,updateTime,provName);
			logger.info("获取列表页结束");	
			//-------------------------列表页-----------------------//						
			logger.info("写入文件");
			writeFile(list,properties,pinyin);
			logger.info("写入成功");
	}
	
	/**
	 * 列表页
	 * @param pageSize
	 * @param list
	 * @param headerParameter
	 * @param cookies
	 * @return
	 * @throws Exception 
	 */
	public static ArrayList<Map<String,Object>> getListPage(String parameter,ArrayList<Map<String,Object>> list,String updateTime,String provName) throws Exception{
		ArrayList<Map<String,Object>> newlist = new ArrayList<Map<String,Object>>();
		for(Map<String,Object> m:list){
			Document doc = Jsoup.parse((String) m.get(parameter));
			Elements feed_lists = doc.select("div[class=feed_lists W_texta]");
			for(int j=0;j<feed_lists.size();j++){
				doc = Jsoup.parse(feed_lists.get(j).html());
				Elements userContents = doc.select("div[class=WB_cardwrap S_bg2 clearfix]");
				for(int h=0;h<userContents.size();h++){
					doc = Jsoup.parse(userContents.get(h).html());
					Constans constans = new Constans();
					Map<String,Object> info = constans.map;
					
					int size = doc.select("div[class=feed_from W_textb]").get(0).select("a").size();
					String time = null;
					if(size>0){
						time = doc.select("div[class=feed_from W_textb]").get(0).select("a").get(0).attr("title");
						info.put("发表时间", time);
					}
					
					if(time!=null){
						if(updateTime!=null&&DateUtils.compareDateSina(time,updateTime)==false){
							continue;
						}
					}
					
					String name = doc.select("a[class=name_txt W_fb]").get(0).html();
					logger.info(name);
					String href = doc.select("a[class=name_txt W_fb]").get(0).attr("href");
					String content = doc.select("p[class=comment_txt]").size()>0?doc.select("p[class=comment_txt]").get(0).html():"";
					String Keyword = doc.select("p[class=comment_txt]").size()>0?doc.select("p[class=comment_txt]").get(0).select("em[class=red]").html():"";
					String forward = doc.select("ul[class=feed_action_info feed_action_row4]").select("li").get(1).select("em").html();
					String  comment= doc.select("ul[class=feed_action_info feed_action_row4]").select("li").get(2).select("em").html();
					String like = doc.select("ul[class=feed_action_info feed_action_row4]").select("li").get(3).select("em").html();
					
					if(size>1){
						String phone = doc.select("div[class=feed_from W_textb]").get(0).select("a").get(1).html();
						info.put("手机类型", phone);
					}
					info.put("昵称", name);
					info.put("url", href);
					info.put("关键字", Keyword);
					info.put("内容", content.replaceAll("[\\<][^\\<\\>]+[\\>]", ""));
					info.put("转发",forward.isEmpty()?"0":forward);
					info.put("评论",comment.isEmpty()?"0":comment);
					info.put("点赞",like.isEmpty()?"0":like);
					info.put("采集日期", DateUtils.getCurrentDate());//查看我的时间帮助类				
					newlist.add(info);
					logger.info("解析列表页中...");
				}
			}
			try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
            	logger.error(ExceptionUtil.getExceptionMessage(e));
            }
		}
		return newlist;
	}
	
	/**
	 * 详情页
	 * @param parameter
	 * @param list
	 * @return
	 * @throws IOException
	 */
	public static ArrayList<Map<String,Object>> getDetailPage(String parameter,ArrayList<Map<String,Object>> list)throws Exception{
		ArrayList<Map<String,Object>> newlist = new ArrayList<Map<String,Object>>();
		for(Map<String,Object> m:list){
			logger.info("========"+m.get("昵称")+"========第"+m.get("fym")+"页");
			Document d = Jsoup.parse((String) m.get(parameter));
			Elements script = d.select("script");
			for(int j=0;j<script.size();j++){
				String html = script.get(j).html();
				if(html.contains("\"domid\":\"Pl_Official_PersonalInfo__62\"")){			
					@SuppressWarnings("unchecked")
					Map<String,String> map =(Map<String, String>) JSON.parse(html.substring(html.indexOf("(")+1,html.lastIndexOf(")")));
					if(map.get("html")==null||"".equals(map.get("html"))){
						logger.info("获取基础信息出错");
					}else{
						Document doc = Jsoup.parse(map.get("html"));
						Elements es = doc.select("div[class=WB_cardwrap S_bg2]");
						for(Element e:es){
							doc = Jsoup.parse(e.html());
							String title = doc.select("h2[class=main_title W_fb W_f14]").html();
							switch(title) { 
								case "基本信息": {
									Elements s = doc.select("li");
									for(Element element:s){
										m.put(element.select("span[class=pt_title S_txt2]").html().split("：")[0], 
												element.select("span[class=pt_detail]").html().replaceAll("[\\<][^\\<\\>]+[\\>]", ""));
									}
								}
								break; 
								case "联系信息": {
									Elements s = doc.select("li");
									for(Element element:s){
										m.put(element.select("span[class=pt_title S_txt2]").html().split("：")[0], 
												element.select("span[class=pt_detail]").html().replaceAll("[\\<][^\\<\\>]+[\\>]", ""));
									}
								}
								break; 
								case "工作信息":{
									Elements s = doc.select("li");
									String dwmc = "";
									for(Element element:s){
										doc=Jsoup.parse(element.html());
										for(Element el:doc.select("span[class=pt_detail]")){
											dwmc += "^"+el.select("a").html();
										}
									}
									m.put("单位名称",dwmc.substring(1,dwmc.length()));
								} 
								break; 
								case "教育信息":{
									Elements s = doc.select("li");
									String mc = "";
									for(Element element:s){
										doc=Jsoup.parse(element.html());
										for(Element el:doc.select("span[class=pt_detail]")){
											for(Element els:el.select("a")){
												mc += "^"+els.html();
											}
										}
									}
									m.put("学校名称",mc.substring(1,mc.length()));
								} 
								break; 
								case "标签信息":{
									Elements s = doc.select("li");
									for(Element element:s){
										m.put(element.select("span[class=pt_title S_txt2]").html().split("：")[0], 
												element.select("span[class=pt_detail]").html().replaceAll("[\\<][^\\<\\>]+[\\>]", "").replaceAll("     ", "^"));
									}
								} 
								break; 
								case "简介":{
									String content= doc.select("div[class=WB_innerwrap]").select("p[class=p_txt]").html();
									map.put("简介", content);
								} 
								break; 
								case "基本讯息":{
									Elements s = doc.select("li");
									String mc = "";
									for(Element element:s){
										doc=Jsoup.parse(element.html());
										for(Element el:doc.select("span[class=pt_detail]")){
											mc += "^"+el.select("a").html();
										}
										m.put(element.select("span[class=pt_title S_txt2]").html().split("：")[0],mc.substring(1,mc.length()));
									}
								} 
								break; 
							} 
						}
					}
				}
				if(html.contains("\"domid\":\"Pl_Core_T3Simpletext__30\"")){
					@SuppressWarnings("unchecked")
					Map<String,String> map =(Map<String, String>) JSON.parse(html.substring(html.indexOf("(")+1,html.lastIndexOf(")")));
					if(map.get("html")==null||"".equals(map.get("html"))){
						logger.info("获取简介信息出错");
					}else{
						Document doc = Jsoup.parse(map.get("html"));
						String content= doc.select("div[class=WB_innerwrap]").select("p[class=p_txt]").html();
						m.put("简介", content);
					}
				}
				if(html.contains("\"domid\":\"Pl_Core_UserInfo__27\"")){
					@SuppressWarnings("unchecked")
					Map<String,String> map =(Map<String, String>) JSON.parse(html.substring(html.indexOf("(")+1,html.lastIndexOf(")")));
					if(map.get("html")==null||"".equals(map.get("html"))){
						logger.info("获取行业类别出错");
					}else{
						Document doc = Jsoup.parse(map.get("html"));
						Elements es = doc.select("div[class=WB_innerwrap]");
						if(es.size()>0){
							String hylb = es.get(0).select("span[class=item_text W_fl]").text();
							if(html.contains("行业类别")){
								m.put("行业类别", hylb.split(" ")[1]);
							}	
						}
					}
				}
			}
			m.remove(parameter);
			/**=================================**/
			m.remove("fym");
			/**=================================**/
			newlist.add(m);
			logger.info("解析详情页中...");
		}
		return newlist;
	}
	/**
	 * 中间页
	 * @param parameter
	 * @param list
	 * @return
	 * @throws IOException
	 */
	public static ArrayList<Map<String,Object>> getMiddelePage(String parameter,ArrayList<Map<String,Object>> list) throws Exception{
		ArrayList<Map<String,Object>> newlist = new ArrayList<Map<String,Object>>();
		for(Map<String,Object> m:list){
			logger.info("========"+m.get("昵称")+"========第"+m.get("fym")+"页");
			//Document d = Jsoup.parse(FileTools.readData(new FileInputStream(new File((String) m.get(parameter))), "UTF-8"));
			Document d = Jsoup.parse((String) m.get(parameter));
			Elements script = d.select("script");
			for(int j=0;j<script.size();j++){
				String html = script.get(j).html();
				if(html.contains("PCD_person_info")){			
					@SuppressWarnings("unchecked")
					Map<String,String> map =(Map<String, String>) JSON.parse(html.substring(html.indexOf("(")+1,html.lastIndexOf(")")));
					if(map.get("html")==null||"".equals(map.get("html"))){
						logger.info("获取详情页地址出错");
					}else{
						Document doc = Jsoup.parse(map.get("html"));
						String href = doc.select("a[class=WB_cardmore S_txt1 S_line1 clearfix]").attr("href");
						if(href.contains("http://weibo.com")){
							m.put("more", href);
						}else{
							m.put("more", "http://weibo.com"+href);
						}
					}
				}
				if(html.contains("\"domid\":\"Pl_Core_T8CustomTriColumn__3\"")){
					@SuppressWarnings("unchecked")
					Map<String,String> map =(Map<String, String>) JSON.parse(html.substring(html.indexOf("(")+1,html.lastIndexOf(")")));
					if(map.get("html")==null||"".equals(map.get("html"))){
						logger.info("获取关注、粉丝数、微博数出错");
					}else{
						Document doc = Jsoup.parse(map.get("html"));
						m.put("总关注数", doc.select("table[class=tb_counter]").select("td[class=S_line1]").get(0).select("strong").html());
						m.put("总粉丝数",doc.select("table[class=tb_counter]").select("td[class=S_line1]").get(1).select("strong").html());
						m.put("总微博数",doc.select("table[class=tb_counter]").select("td[class=S_line1]").get(2).select("strong").html());
					}
				}
			}
			logger.info("========"+m.get("more")+"========");
			logger.info("解析中间页中...");
			m.remove(parameter);
			newlist.add(m);
		}
		return newlist;
	}
	
	/**
	 * 下载页面文档
	 * @param url
	 * @param headerParameter
	 * @param timeout
	 * @return
	 * @throws IOException
	 */
	public static Document getDocument(String url,Map<String,String> headerParameter,int timeout) throws IOException{
		Connection connection = Jsoup.connect(url);
		if(timeout!=0){
			connection.timeout(timeout);
		}

		for (Map.Entry<String, String> entry : headerParameter.entrySet()) {  
		    connection.header(entry.getKey(),entry.getValue());
		}  
		return connection.get();
	}
	
	/**
	 * 写入文件
	 * @param url
	 * @param headerParameter
	 * @param timeout
	 * @return
	 * @throws IOException
	 */
	public static void writeFile(ArrayList<Map<String,Object>> list,Properties properties,String pinyin) throws Exception{
		StringBuffer stringbuffer = new StringBuffer();
		for(Map<String,Object> m:list){
			stringbuffer.append(properties.get("header") + "\r\n");
			for (Map.Entry<String, Object> entry : m.entrySet()) {  
				if("简介".equals(entry.getKey())){
					stringbuffer.append(properties.get("titleStart")+entry.getKey()+properties.get("titleEnd")+ "\r\n");
				}else{
					stringbuffer.append(properties.get("titleStart")+entry.getKey()+properties.get("titleEnd")+ CharacterFilter.filter(String.valueOf(entry.getValue())) + "\r\n");
				}
			}  
		}
        Format format = new SimpleDateFormat("yyyyMMddHHmmss");
        String fileDir = String.format((String) properties.get("fileDir"), pinyin+format.format(new Date()));
        FileTools.createNewFile(fileDir,format.format(new Date())+".txt",stringbuffer.toString());
	}
	/**
	 * 指定微博写入文件
	 * @param url
	 * @param headerParameter
	 * @param timeout
	 * @return
	 * @throws IOException
	 */
	public static void writeFileOneBlog(Map<String,Object> m,Properties properties,String pinyin) throws Exception{
		StringBuffer stringbuffer = new StringBuffer();
			stringbuffer.append(properties.get("header") + "\r\n");
			for (Map.Entry<String, Object> entry : m.entrySet()) {  
				if("简介".equals(entry.getKey())){
					stringbuffer.append(properties.get("titleStart")+entry.getKey()+properties.get("titleEnd")+ "\r\n");
				}else{
					stringbuffer.append(properties.get("titleStart")+entry.getKey()+properties.get("titleEnd")+ CharacterFilter.filter(String.valueOf(entry.getValue())) + "\r\n");
				}
			}  
        Format format = new SimpleDateFormat("yyyy-MM-dd");
        String fileDir = String.format((String) properties.get("OneBlogfileDir")+format.format(new Date())+File.separator, format.format(new Date()));
        FileTools.createNewFile(fileDir,pinyin+".txt",stringbuffer.toString());
	}
	/**
	 * 下载页面文档集合
	 * @param url
	 * @param headerParameter
	 * @param timeout
	 * @return
	 * @throws Exception 
	 * @throws IOException
	 */
	public static ArrayList<Map<String,Object>> pageList(String url,ArrayList<Map<String,Object>> list,Map<String,String> headerParameter,Properties properties,List<String> cookies,String urlType,int pageSize,String pinyin) throws Exception{
		ArrayList<Map<String,Object>> newlist = new ArrayList<Map<String,Object>>();
		int back=0;
		for(int i=0;i<pageSize;i++){	
			try {
				headerParameter.put("Cookie", cookies.get(i%cookies.size()));
				if("1".equals(urlType)){
					Map<String,Object> m= new HashMap<String,Object>();
					Document doc = getDocument(url+i,headerParameter,Integer.valueOf((String)properties.get("timeout")));	
					m.put("document",doc.html());
					/**=================================**/
					m.put("fym", i);
					/**=================================**/
					newlist.add(m);
				}
				if("2".equals(urlType)){
					Map<String,Object> m=list.get(i);
					if(m.get(url)==null){
						continue;
					}
					Document doc = getDocument((String) m.get(url),headerParameter,Integer.valueOf((String)properties.get("timeout")));        
					m.put("document", doc.html());
					m.remove(url);
					newlist.add(m);
				}
				
				back=0;
			}catch(Exception e){
				System.out.println("back"+back);
				if(back<5){
					back++;
	            	i--;
	            	continue;
				}else{
					continue;
				}
            }finally{ 
                    Thread.sleep(2000);
            }
			logger.info("========获取第"+i+"条数据========");
		}
		return newlist;
	}
	public static void main(String[] args) throws IOException {
		String html ="<html>"+
		 "<head>"+ 
		  "<title>新浪通行证</title>"+ 
		  "<meta http-equiv=\"refresh\" content=\"0; url='http://login.sina.com.cn/crossdomain2.php?action=login&amp;entry=security&amp;r=https%3A%2F%2Fpassport.weibo.com%2Fwbsso%2Flogin%3Fssosavestate%3D1510019386%26url%3Dhttp%253A%252F%252Fweibo.com%26ticket%3DST-MjA5NjY5NTU3NQ%3D%3D-1478483386-xd-BA4A01DE3E29C676B83A6298604704CB%26retcode%3D0'\">"+ 
		  "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=GBK\">"+ 
		 "</head>"+ 
		 "<body bgcolor=\"#ffffff\" text=\"#000000\" link=\"#0000cc\" vlink=\"#551a8b\" alink=\"#ff0000\">"+ 
		  "<script type=\"text/javascript\" language=\"javascript\">"+
				"location.replace(\"http://login.sina.com.cn/crossdomain2.php?action=login&entry=security&r=https%3A%2F%2Fpassport.weibo.com%2Fwbsso%2Flogin%3Fssosavestate%3D1510019386%26url%3Dhttp%253A%252F%252Fweibo.com%26ticket%3DST-MjA5NjY5NTU3NQ%3D%3D-1478483386-xd-BA4A01DE3E29C676B83A6298604704CB%26retcode%3D0\");"+
				"</script>"+  
		 "</body>"+
		"</html>";
		Document doc = Jsoup.parse(html);
		String title= doc.getElementsByTag("title").text();
		if("新浪通行证".equals(title)){
			String script=doc.select("meta[http-equiv='refresh']").attr("content");
			int start = script.indexOf("=\'")+2;
			int end = script.lastIndexOf("'");
			System.out.println(script.substring(start,end));
			Map<String,String> headerParameter = new HashMap<String,String>();
			Document d = getDocument(script.substring(start,end),headerParameter,8000);
			System.out.println(d.html());
		}
	}

	/**
	 * 获取制定微博信息
	 * @param url  
	 * @param cookies
	 * @param fileUrl
	 * @throws IOException
	 */
	static boolean   hasHtml=false;//判断是否含有指定 的微博正文内容
	static boolean   flag=false;//判断抓取数据时间是否超过要求范围
	public static void getOneBlog(List<String> cookies,Properties properties,String keyword,String pinyin,String updateTime,String url,String provName) throws Exception{		    		  
		    Map<String,String> headerParameter = new HashMap<String,String>();
			Constans constans = new Constans();
			Map<String,Object> m = constans.map;
			for(int i=0;i<cookies.size();i++){
				headerParameter.put("Cookie", cookies.get(i));
				//headerParameter.put("Content-Type", "application/xml");
				headerParameter.put("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
			}
			logger.info("获取指定微博");
			m.put("昵称", keyword);			
			//https://weibo.com/208820678?is_search=0&visible=0&is_all=1&is_tag=0&profile_ftype=1&page=1#feedtop
			for(int i=1;;i++){
				hasHtml=false;
				flag=false;				
				if(!url.startsWith("http://")){
					continue;
				}				
				m.put("url",url+"?is_search=0&visible=0&is_all=1&is_tag=0&profile_ftype=1&page="+i+"#feedtop");				
				Document d = getDocument(url+"?is_search=0&visible=0&is_all=1&is_tag=0&profile_ftype=1&page="+i+"#feedtop",headerParameter,Integer.valueOf((String)properties.get("timeout")));
				//System.out.println(d.toString());
				logger.info(url+"?is_search=0&visible=0&is_all=1&is_tag=0&profile_ftype=1&page="+i+"#feedtop");
				
				Elements script = d.select("script");
				logger.info(script);
				for(int j=0;j<script.size();j++){
					String html = script.get(j).html();
					if(html.contains("\"domid\":\"Pl_Core_T8CustomTriColumn__3\"")){
						@SuppressWarnings("unchecked")
						Map<String,String> map =(Map<String, String>) JSON.parse(html.substring(html.indexOf("(")+1,html.lastIndexOf(")")));
						if(map.get("html")==null||"".equals(map.get("html"))){
						
							break;
						}else{
							Document doc = Jsoup.parse(map.get("html"));
							m.put("总关注数", doc.select("table[class=tb_counter]").select("td[class=S_line1]").get(0).select("strong").html());
							m.put("总粉丝数",doc.select("table[class=tb_counter]").select("td[class=S_line1]").get(1).select("strong").html());
							m.put("总微博数",doc.select("table[class=tb_counter]").select("td[class=S_line1]").get(2).select("strong").html());
						}
					}
					if(html.contains("WB_cardwrap WB_feed_type S_bg2 WB_feed_like")){
						hasHtml=true;
						@SuppressWarnings("unchecked")
						Map<String,String> map =(Map<String, String>) JSON.parse(html.substring(html.indexOf("(")+1,html.lastIndexOf(")")));
						if(map.get("html")==null||"".equals(map.get("html"))){												
							break;
						}else{
							Document doc = Jsoup.parse(map.get("html"));						
							Elements  elements=doc.select("div[class=WB_cardwrap WB_feed_type S_bg2 WB_feed_like ]");
							if(elements.size()!=0){
								flag=insertData(elements,m,provName,properties,pinyin,updateTime);	
								if(flag){
									break;
								}
							}																	
						}
					}							
				}
				if(flag){
					break;
				}
				if(hasHtml){
					continue;
				}else{
					break;
				}
			}			
	}
	private static boolean insertData(Elements elements,Map m,String provName,Properties properties,String pinyin,String dateupdate) throws Exception{
		int size=elements.size();	
		m.put("采集日期", DateUtils.getCurrentDate());
		m.put("省份","广东"+provName);
		if(provName.split("#").length>0){
			m.put("城市","".equals(provName.split("#")[0])?"":provName.split("#")[0]);	
		}			
		if(provName.split("#").length>1){
			m.put("区县","".equals(provName.split("#")[1])?"":provName.split("#")[1]);	
		}		
		for(int i=0;i<size;i++){
			//获取发布内容的手机
	        String comefrom="".equals(elements.select("div[class=WB_cardwrap WB_feed_type S_bg2 WB_feed_like]").get(i).select("div[class=WB_feed_detail clearfix]").select("div[class=WB_detail]").select("div[class=WB_detail]").select("div[class=WB_from S_txt2]").select("a").size()>1)?elements.select("div[class=WB_cardwrap WB_feed_type S_bg2 WB_feed_like]").get(i).select("div[class=WB_feed_detail clearfix]").select("div[class=WB_detail]").select("div[class=WB_detail]").select("div[class=WB_from S_txt2]").select("a").get(1).html():"";
	        //发布时间
	        String date="".equals(elements.select("div[class=WB_cardwrap WB_feed_type S_bg2 WB_feed_like]").get(i).select("div[class=WB_feed_detail clearfix]").select("div[class=WB_detail]").select("div[class=WB_detail]").select("div[class=WB_from S_txt2]").select("a").get(0).attr("title"))?"":elements.select("div[class=WB_cardwrap WB_feed_type S_bg2 WB_feed_like]").get(i).select("div[class=WB_feed_detail clearfix]").select("div[class=WB_detail]").select("div[class=WB_detail]").select("div[class=WB_from S_txt2]").select("a").get(0).attr("title");
	        String content="".equals(elements.select("div[class=WB_cardwrap WB_feed_type S_bg2 WB_feed_like]").get(i).select("div[class=WB_feed_detail clearfix]").select("div[class=WB_detail]").select("div[class=WB_detail]").select("div[class=WB_text W_f14]").html())?"":elements.select("div[class=WB_cardwrap WB_feed_type S_bg2 WB_feed_like]").get(i).select("div[class=WB_feed_detail clearfix]").select("div[class=WB_detail]").select("div[class=WB_detail]").select("div[class=WB_text W_f14]").html();
	        String getAway="".equals(elements.select("div[class=WB_cardwrap WB_feed_type S_bg2 WB_feed_like]").get(i).select("div[class=WB_feed_handle]").select("ul[class=WB_row_line WB_row_r4 clearfix S_line2]").select("li").get(1).select("span[class=line S_line1]").select("span").select("em").get(1).html())?"":elements.select("div[class=WB_cardwrap WB_feed_type S_bg2 WB_feed_like]").get(i).select("div[class=WB_feed_handle]").select("ul[class=WB_row_line WB_row_r4 clearfix S_line2]").select("li").get(1).select("span[class=line S_line1]").select("span").select("em").get(1).html();
	        String comment="".equals(elements.select("div[class=WB_cardwrap WB_feed_type S_bg2 WB_feed_like]").get(i).select("div[class=WB_feed_handle]").select("ul[class=WB_row_line WB_row_r4 clearfix S_line2]").select("li").get(2).select("span[class=line S_line1]").select("span").select("em").get(1).html())?"":elements.select("div[class=WB_cardwrap WB_feed_type S_bg2 WB_feed_like]").get(i).select("div[class=WB_feed_handle]").select("ul[class=WB_row_line WB_row_r4 clearfix S_line2]").select("li").get(2).select("span[class=line S_line1]").select("span").select("em").get(1).html();
	        String like="".equals(elements.select("div[class=WB_cardwrap WB_feed_type S_bg2 WB_feed_like]").get(i).select("div[class=WB_feed_handle]").select("ul[class=WB_row_line WB_row_r4 clearfix S_line2]").select("li").get(3).select("span[class=line S_line1]").select("span").select("em").get(1).html())?"":elements.select("div[class=WB_cardwrap WB_feed_type S_bg2 WB_feed_like]").get(i).select("div[class=WB_feed_handle]").select("ul[class=WB_row_line WB_row_r4 clearfix S_line2]").select("li").get(3).select("span[class=line S_line1]").select("span").select("em").get(1).html();
	        String keyword="".equals(elements.select("div[class=WB_cardwrap WB_feed_type S_bg2 WB_feed_like]").get(i).select("div[class=WB_feed_detail clearfix]").select("div[class=WB_detail]").select("div[class=WB_detail]").select("div[class=WB_text W_f14]").select("a").attr("title"))?"":elements.select("div[class=WB_cardwrap WB_feed_type S_bg2 WB_feed_like]").get(i).select("div[class=WB_feed_detail clearfix]").select("div[class=WB_detail]").select("div[class=WB_detail]").select("div[class=WB_text W_f14]").select("a").attr("title");
	        //微博对应的链接	      
	        String url="".equals(elements.select("div[class=WB_cardwrap WB_feed_type S_bg2 WB_feed_like]").get(i).select("div[class=WB_feed_detail clearfix]").select("div[class=WB_detail]").select("div[class=WB_detail]").select("div[class=WB_text W_f14]").select("a").attr("href"))?"":elements.select("div[class=WB_cardwrap WB_feed_type S_bg2 WB_feed_like]").get(i).select("div[class=WB_feed_detail clearfix]").select("div[class=WB_detail]").select("div[class=WB_detail]").select("div[class=WB_text W_f14]").select("a").attr("href");
	        m.put("手机类型", comefrom);
	        m.put("发表时间",date);
	        m.put("转发",getAway);
	        //m.put("内容",content.replaceAll("[^\u4e00-\u9fa5]", "|"));	        
	        m.put("内容",getContent(content));
	        m.put("评论",comment);
	        m.put("点赞", like);
	        m.put("关键字",keyword);
	        m.put("友情链接",url);	       	
			if(date.compareTo(dateupdate)<0?true:false){
				flag=true;
				logger.info("获取指定微博结束");					
				return flag;
			}			
			logger.info("写入文件");
			writeFileOneBlog(m,properties,pinyin);
			m.put("手机类型", "");
	        m.put("发表时间","");
	        m.put("转发","");
	        m.put("内容","");
	        m.put("评论","");
	        m.put("点赞", "");
	        m.put("关键字","");
	        m.put("友情链接","");
			logger.info("写入成功");	
		}				
		return flag;
	}
	private static String getContent(String contentString){		
		    contentString=contentString.replaceAll("<a .*>", "");  
		    contentString=contentString.replaceAll("</a>", "");  
		    contentString=contentString.replaceAll("<img .*/>", "");  
		    contentString=contentString.replaceAll("<img(.*?)>", "");  
		    System.out.println(contentString); 
		    return contentString;
	}
	/**
	 * 获取三年前的时间函数
	 * @param date
	 * @return
	 */
	private static String getThreeYearAgo(){
		SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:MM");		
		Calendar calendar=Calendar.getInstance();
		calendar.add(calendar.YEAR, -3);
		String date=format.format(calendar.getTime());		
		return date;
	}
}
