package tools;
import org.apache.http.Consts;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import sun.misc.resources.Messages_zh_CN;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * @author: zpf
 **/
public class MRequest {

    private static final Logger logger = Logger.getLogger("MRquest.class");

    public static HttpResponse query(String method, String url, Map<String,String> header, HttpEntity entity, int tries)  {
        HttpClient httpclient = new DefaultHttpClient();
        httpclient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT,2000);
        httpclient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT,2000);

        if(entity instanceof UrlEncodedFormEntity){
            header.put(MConstants.HEADER_CONTENT_TYPE, "application/x-www-form-urlencoded");
        }

        if(url.indexOf("http://music.163.com") >= 0){
            header.put("Referer", "http://music.163.com");
        }

        String ua = header.get("User-Agent");
        if(ua == null){
            header.put("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:46.0) Gecko/20100101 Firefox/46.0");
        }

        List<Header>  headerList = new ArrayList<>();
        for(Map.Entry<String,String> entry : header.entrySet()){
            Header h = new BasicHeader(entry.getKey(), entry.getValue());
            headerList.add(h);
        }

        Header[] headers = new Header[headerList.size()];
        headerList.toArray(headers);

        HttpResponse result = null;

        int time = 0;
        while(true) {
            try {
                if (method.equalsIgnoreCase("POST")) {
                    HttpPost httppost = new HttpPost(url);
                    httppost.setHeaders(headers);
                    httppost.setEntity(entity);
                    result = httpclient.execute(httppost);
                    break;
                }
                if (method.equalsIgnoreCase("GET")) {
                    HttpGet httpget = new HttpGet(url);
                    httpget.setHeaders(headers);
                    result = httpclient.execute(httpget);
                    break;
                }
            } catch (Exception ex) {
                if(time < tries) {
                    time++;
                    continue;
                }else{
                    logger.info("请求失败URL#=> " + url);
                    break;
                }
            }
        }
        return result;
    }

    public static UrlEncodedFormEntity getUrlEncodedFormEntity(Map<String,String> fieldValue){

        List<BasicNameValuePair> formparams = new ArrayList<>();
        for(Map.Entry<String,String> entry : fieldValue.entrySet()){
            formparams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }

        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, Consts.UTF_8);
        return entity;

    }

}
