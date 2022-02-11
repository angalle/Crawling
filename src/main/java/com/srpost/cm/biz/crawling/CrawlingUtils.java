package com.srpost.cm.biz.crawling;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;

import com.srpost.cm.biz.crawling.service.CrawlingServiceImpl;
import com.srpost.cm.biz.crawling.vo.CrawlingVO;

public class CrawlingUtils {
	
	private static final Logger logger = LoggerFactory.getLogger(CrawlingServiceImpl.class);
	
	public static CrawlingVO setVOProperty(CrawlingVO vo, String docSeq, String writer, String title, boolean fileYn, String detilaPath, String docRegDt, String contents, String docType, String siteType, String formParams, String year) {
		if(docSeq != null) {
			vo.setDocSeq(Integer.parseInt(docSeq));
		}		
		vo.setWriter(writer);
		vo.setTitle(title);
		vo.setFileYn(fileYn);
		vo.setDetailUrl(detilaPath);
		vo.setDocRegDt(docRegDt);
		vo.setContents(contents);
		vo.setDocType(docType);
		vo.setSiteType(siteType);
		vo.setFormParams(formParams);
		vo.setDocYear(year);
		return vo;
	}
	
	public static String retryPageReload(WebDriver driver, int pageIndex) {
		WebElement select = driver.findElement(By.cssSelector("select.de-search-select"));
    	List<WebElement> selectOptions = select.findElements(By.cssSelector("option"));
    	if((selectOptions.size()-1) < pageIndex) {
    		return "false";
    	}
    	
    	WebElement clickOption = selectOptions.get(pageIndex);
    	String year = clickOption.getText();
    	clickOption.click();
    	
    	return year;
	}
	
	
	public static boolean isDuplicateSavePoint(CrawlingVO checkDuplicateVo, CrawlingVO vo) {
		if(checkDuplicateVo != null) {
    		if(
				checkDuplicateVo.getDocSeq() == vo.getDocSeq() || 
				(
						checkDuplicateVo.getTitle().equals(vo.getTitle()) && 
						checkDuplicateVo.getDocRegDt().substring(0,10).equals(vo.getDocRegDt())
				)
			) {
        		// 기존에 등록된 곳 까지 insert 시
        		logger.info(vo.getSiteType()+"/ALREADY REGISTER DOCUMENT");
        		logger.info("TITLE::"+checkDuplicateVo.getTitle()+"/DOC_SEQ::"+checkDuplicateVo.getDocSeq()+"/CRETED_DOCUMENT:"+checkDuplicateVo.getDocRegDt());
        		return true;
        	}
    	}
		return false;
	}
	
	public static boolean isDuplicateSavePointByTwoType(CrawlingVO checkDuplicateVo, CrawlingVO vo) {
		if(checkDuplicateVo != null) {
    		if(
    				checkDuplicateVo.getDocYear().equals(vo.getDocYear()) &&
    				checkDuplicateVo.getTitle().equals(vo.getTitle()) && 
    				checkDuplicateVo.getDocType().equals(vo.getDocType())
			) {
        		// 기존에 등록된 곳 까지 insert 시
    			logger.info(vo.getSiteType()+"/ALREADY REGISTER DOCUMENT");
    			logger.info("TITLE::"+checkDuplicateVo.getTitle()+"/BOARD_TYPE::"+checkDuplicateVo.getDocType());
    			return true;
        	}
    	}
		return false;
		
	}
	
	public static boolean isFileCheck(List<WebElement> fileElements) {
		if(fileElements.size() != 0) {
        	return true;
        }
		
		return false;
	}
	
	public static void executeAttributeJavascript(WebDriver driver, String attribute, WebElement clickTarget) {
		JavascriptExecutor executor = (JavascriptExecutor) driver;
        String clickScript = clickTarget.getAttribute("onclick");
        executor.executeScript(clickScript);
	}
	
	public static void executeJavascript(WebDriver driver, String script) {
		JavascriptExecutor executor = (JavascriptExecutor) driver;
        executor.executeScript(script);
	}
	
	public static List<WebElement> getaLinkInTable(WebDriver driver, String css) throws Exception{
		List<WebElement> aLinks = driver.findElements(By.cssSelector(css));
        if (aLinks.size() == 0 ) {
        	throw new Exception("Fail to Call Document");
        }
		return aLinks;
	}

	
	public static void waitingResponse(int seconds){
        try {Thread.sleep(seconds);} catch (InterruptedException e) {}
    }
	
	public static boolean isRetryHttpGetRequest(String urlString) throws IOException{
		int statusCode = 0;
		int retryCnt = 0;
		while(statusCode != 200) {
			URL url = new URL(urlString);
	        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
	        connection.setRequestMethod("GET");
	        connection.connect();
		    logger.info("GET REQUEST::"+urlString);
	        statusCode = connection.getResponseCode();
	        if(retryCnt == 3) {
	        	return false;
	        }
	        retryCnt++;
		}
		return true;
	}
	
	public static boolean retryHttpPostRequest(String urlString, String jsonString) throws IOException{
		int statusCode = 0;
		int retryCnt = 0;
		HttpClient httpClient = HttpClientBuilder.create().build();
		while(statusCode != 200) {
			HttpPost request = new HttpPost(urlString);
		    StringEntity params =new StringEntity(jsonString);
		    request.addHeader("content-type", "application/x-www-form-urlencoded");
		    request.setEntity(params);
		    HttpResponse response = httpClient.execute(request);
		    
		    StatusLine statusLine = response.getStatusLine();
		    statusCode = statusLine.getStatusCode();
		    logger.info("POST REQUEST::"+urlString+"/POSTBODY ::"+jsonString);
	        if(retryCnt == 3) {
	        	logger.warn("PAGE STATUS ERROR URL::"+urlString);
	        	return false;
	        }
	        retryCnt++;
		}
		return true;
	}

	// 페이지 이동시 로드시간을 기다리기 
    public static void pageMove(WebDriver driver, String urlString, int seconds){
        driver.get(urlString);
        waitingResponse(seconds);
    }
    
    public static void pagePostRequestMove(WebDriver driver, String url, int seconds){
        driver.get(url);
        waitingResponse(seconds);
    }
    
	// 뒤로가기 시 로드시간을 기다리기 
    public static void pageBack(WebDriver driver, int seconds){
        driver.navigate().back();
        waitingResponse(seconds);
    }

}
