import Test.sinaspider.FileTools;
import Test.utils.ChineseToEnglish;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Time: 2017/10/14.
 */
public class LyUtils {
    private  static Logger logger=Logger.getLogger("LyUtils.class");
    /**
     * 访问的网站响应时间有点慢，所以设置的时间比较大
     */
    static  RequestConfig config = RequestConfig.custom()
            .setConnectTimeout(6000*2)
            .setConnectionRequestTimeout(6000*2)
            .setSocketTimeout(6000*2)
            .setStaleConnectionCheckEnabled(true)
            .build();
    /**
     * 获取网站的cookie
     * [getCookie description]
     * @param  url [网站地址]
     * @param  map [请求参数]
     * @return     [description]
     */
    public static String getCookie(String url,Map<String,String> map){
        CloseableHttpClient httpClient=HttpClients.createDefault();
        HttpPost httpPost = null;
        String result = null;
        List<BasicNameValuePair> list=new ArrayList<>();
        try {
            CookieStore cookieStore=new BasicCookieStore();
            HttpClients.custom().setDefaultCookieStore(cookieStore).build();
            httpPost=new HttpPost(url);
            if(map!=null||!map.isEmpty()){
                for(Map.Entry<String,String>entity:map.entrySet()){
                       list.add(new BasicNameValuePair(entity.getKey(),entity.getValue()));
                }
            }else {
                logger.info("请求参数为空，请重新设置参数！");
                return  null;
            }
            httpPost.setConfig(config);
            httpPost.setEntity(new UrlEncodedFormEntity(list,"utf-8"));
            CloseableHttpResponse response= httpClient.execute(httpPost);
            // JSESSIONID
            String cookie = response.getFirstHeader("Set-Cookie")
                    .getValue();
            if(!"".equals(cookie)){
                cookie=cookie.substring("JSESSIONID=".length(),cookie.indexOf(";"));
            }else{
                logger.info("该站点没有启用cookie！");
                return null;
            }
            return "JSESSIONID="+cookie;
        }catch (Exception e){
            logger.info("获取cookie失败！");
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 方法不启用留作备用
     * @param url
     * @return
     */
    public static String getCookies(String url){
        HttpClient httpClient=new HttpClient();
        PostMethod postMethod=new PostMethod(url);
        Cookie[] cookies=null;
        // 设置 HttpClient 接收 Cookie,用与浏览器一样的策略
        try{
            httpClient.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
            httpClient.getParams().setSoTimeout(5000);
            int  statusCode=httpClient.executeMethod(postMethod);
            cookies=httpClient.getState().getCookies();
            if(statusCode==302){
                logger.info("模拟登陆成功");
                return  cookies[0].getName()+"="+cookies[0].getValue();
            }
        }catch (Exception e){
           logger.info("模拟登陆失败");
        }
        return null;
    }
    /**
     * 下载页面文档
     * @param url
     * @param headerParameter
     * @param timeout
     * @return
     * @throws IOException
     */
    public static Document getDocument(String url, Map<String,String> headerParameter, int timeout) throws IOException, InterruptedException {
        Connection connection = Jsoup.connect(url);
        Thread.sleep(3000);
        if(timeout!=0){
            connection.timeout(timeout);
        }
        for (Map.Entry<String, String> entry : headerParameter.entrySet()) {
            connection.header(entry.getKey(),entry.getValue());
        }
        return connection.get();
    }
    /**
     * 模拟请求
     *
     * @param url   资源地址
     * @param map   参数列表
     * @param encoding  编码
     * @return
     * @throws ParseException
     * @throws IOException
     */
    public static String send(String url, Map<String,String> map,String encoding,String cookies) throws ParseException, IOException{
        String body = "";
        //创建httpclient对象
        CloseableHttpClient client = HttpClients.createDefault();
        //创建post方式请求对象
        HttpPost httpPost = new HttpPost(url);
        //装填参数
        httpPost.setConfig(config);
        List<BasicNameValuePair> nvps = new ArrayList<>();
        if(map!=null){
            for (Map.Entry<String, String> entry : map.entrySet()) {
                nvps.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
            }
        }
        //设置参数到请求对象中
        httpPost.setEntity(new UrlEncodedFormEntity(nvps, encoding));
        //设置header信息
        //指定报文头【Content-type】、【User-Agent】
        httpPost.setHeader("Accept","image/gif, image/jpeg, image/pjpeg, application/x-ms-application, application/xaml+xml, application/x-ms-xbap, */*");
        httpPost.setHeader("Accept-Encoding","gzip, deflate");
        httpPost.setHeader("Accept-Language","zh-CN");
        httpPost.setHeader("Cache-Control","no-cache");
        httpPost.setHeader("Connection","Keep-Alive");
        httpPost.setHeader("Content-Type","application/x-www-form-urlencoded");
        httpPost.setHeader("Referer","http://218.56.58.69:8881/Hotel_Tourism/pages/StaCl/unlockingStatistics.do");
        httpPost.setHeader("Cookie", cookies);
        httpPost.setHeader("Host", "218.56.58.69:8881");
        httpPost.setHeader("User-Agent","Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 10.0; WOW64; Trident/7.0; .NET4.0C; .NET4.0E; .NET CLR 2.0.50727; .NET CLR 3.0.30729; .NET CLR 3.5.30729; InfoPath.3)");
        //执行请求操作，并拿到结果
        CloseableHttpResponse response = client.execute(httpPost);
        //获取结果实体
        HttpEntity entity = response.getEntity();
        if (entity != null) {
            //按指定编码转换结果实体为String类型
            body = EntityUtils.toString(entity, encoding);
        }
        EntityUtils.consume(entity);
        //释放链接
        response.close();
        return body;
    }

    public static Map getrequestParms(Properties properties) {
        String username=properties.getProperty("username");
        String password=properties.getProperty("password");
        Map map=new HashedMap();
        if("".equals(username)||username==null){
            logger.info("用户名尚未设置！");
            return null;
        }
        if("".equals(password)||password==null){
            logger.info("用户密码尚未设置！");
            return null;
        }
        map.put("j_username",username);
        map.put("j_password",password);
        return map;
    }
    public static void  writeToFile(String dataType,Map<Integer,Map<String,Object>> map,String date,String city) throws Exception {
        if(!(map.size()>0)){
         return ;
        }
        StringBuffer buffer=new StringBuffer();
        for(int j=0;j<map.size();j++){
            buffer.append("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&"+"\r\n");
            for(Map.Entry<String,Object> entry:map.get(j).entrySet()){
                buffer.append("【"+entry.getKey()+"】"+entry.getValue()+"\r\n");
            }
        }
        SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd");
        city=ChineseToEnglish.getPingYin(city);
        String filedir="E:/BDQX/LyData/"+dataType+"/"+ city+"/"+map.get(0).get("采集日期")+"/";
        String fileName=city+"_"+date+".txt";
        FileTools.createNewFile(filedir,fileName,buffer.toString());
    }
    public static  List<String> getBeenByDays(String start_time,String end_time){
        SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd");
        Date sdate=null;
        Date eDate=null;
        try {
            sdate=format.parse(start_time);
            eDate=format.parse(end_time);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Calendar c = Calendar.getInstance();
        List<String> list=new ArrayList<String>();
        while (sdate.getTime()<=eDate.getTime()) {
            list.add(format.format(sdate));
            c.setTime(sdate);
            c.add(Calendar.DATE, 1); // 日期加1天
            sdate = c.getTime();
        }
        return list;
    }
    /*得到uuid*/
    public static String getUUid(){
        return UUID.randomUUID().toString().replaceAll("-", "");
    }
    /**
     *
     * @return 数据格式:2014-12-19
     */
    public static String getCurDate(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return dateFormat.format(new Date());
    }
}
