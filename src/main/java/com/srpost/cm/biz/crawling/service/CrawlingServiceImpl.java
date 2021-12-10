package com.srpost.cm.biz.crawling.service;

import java.util.List;

import javax.inject.Inject;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.srpost.cm.biz.crawling.Site;
import com.srpost.cm.biz.crawling.dao.CrawlingDAO;
import com.srpost.cm.biz.crawling.vo.CrawlingVO;

@Service
public class CrawlingServiceImpl implements CrawlingService{
	
	private static final Logger logger = LoggerFactory.getLogger(CrawlingServiceImpl.class);
	
	@Inject
    private CrawlingDAO dao;
	
	private static int RETRY_CNT = 3;
	
	@Override
	public boolean executeGN05(Site site) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public boolean executeGN04(Site site) throws Exception {
		ChromeOptions options = new ChromeOptions();
		options.addArguments("headless");		
		WebDriver driver = new ChromeDriver(options);	
		
		CrawlingVO check_duplicate_vo = dao.checkDuplicateCrawling(site.getCode());
		
		String url = site.getValue();
		// 몇번째 row 인지 
        int cursor_index = 0;
        // 현재 페이지
        int page_index = 1;
        // 실패시 재시도 횟수 
        int retry_cnt = 0;
        
        while(true){	
            CrawlingVO vo = new CrawlingVO();
            vo.setSite(site.getCode());
            try{
            	String current_url = url;
            	vo.setSite_url(current_url);
                pageMove(driver, current_url, 2000);

                List<WebElement> a_links = driver.findElements(By.cssSelector("table > tbody > tr"));
                if (a_links.size() == 0 ) {
                	throw new Exception("Fail to Call Document");
                }
                
                logger.info(site.getCode()+"/CURRENT CURSOR/"+cursor_index+"/MAX CURSOR/"+a_links.size()+"/PAGE INDEX/"+page_index);
                
                if(a_links.size() <= cursor_index){
                	page_index++;
                	cursor_index = 0; 
                    continue;
                }
                
                if((a_links.size()-1) <= cursor_index){
                	break;
                }
                
                List<WebElement> tds = driver.findElements(By.cssSelector("table > tbody > tr:nth-child("+(cursor_index+1)+") > td"));
                String board_no = tds.get(0).getText();
                vo.setBoard_no(Integer.parseInt(board_no));
                WebElement click_target = tds.get(1).findElement(By.cssSelector("a"));
                String title = click_target.getText();
                vo.setTitle(title);
                String writer = tds.get(2).getText();
                vo.setWriter(writer);
                List<WebElement> isFileElements = tds.get(3).findElements(By.cssSelector("a"));
                vo.setIsFile(false);
                if(isFileElements.size() != 0) {
                	vo.setIsFile(true);
                }
                
                String created_document = tds.get(4).getText();
                vo.setCreated_document(created_document);
                
                
                JavascriptExecutor executor = (JavascriptExecutor) driver;
                String click_script = click_target.getAttribute("onclick");
                click_script = click_script.replace("return false;", "");
                vo.setDetail_url(click_script);
                System.out.println(click_script);
                executor.executeScript(click_script);
                waitingResponse(2000);
                
                
                WebElement p_article = driver.findElement(By.cssSelector("#editContents"));
                vo.setDocuments(p_article.getAttribute("innerHTML"));
            	
            	
                if(check_duplicate_vo != null) {
            		if(
        				check_duplicate_vo.getTitle().equals(vo.getTitle())  
        				&& check_duplicate_vo.getWriter().equals(vo.getWriter())
        				&& check_duplicate_vo.getCreated_document().substring(0,10).equals(vo.getCreated_document())
    				) {
                		// 기존에 등록된 곳 까지 insert 시
                		logger.info(site.getCode()+"/ALREADY REGISTER DOCUMENT");
                		logger.info("TITLE::"+check_duplicate_vo.getTitle()+"/WRITER::"+check_duplicate_vo.getWriter()+"/CRETED_DOCUMENT:"+check_duplicate_vo.getCreated_document());
                		break;
                	}
            	}
            	
            	
            	vo.setStatus("S");
            	dao.insertCrawling(vo);
            	
            	retry_cnt = 0;
            	cursor_index++;
            }catch(Exception e){
            	// retry 경남은  #### Cusor 3번 게시글이 에러가 남.
            	e.printStackTrace();
            	if(retry_cnt < RETRY_CNT) {
            		logger.warn(site.getCode()+"/CURRENT CURSOR/"+cursor_index+"/PAGE INDEX/"+page_index+"/RETRY CNT/"+retry_cnt);
            		retry_cnt++;
            		continue;
            	}else if(retry_cnt == RETRY_CNT) {
            		vo.setIsFile(false);
                	vo.setStatus("F");
                	dao.insertCrawling(vo);
                	retry_cnt++;
                	// 오류가 났을 시 다음 커서로 이동                
                    cursor_index++;
                   continue;
            	}else {
            		//커서가 이동한뒤에도 반복적 오류가 발생하면 중단
            		logger.error("ABOVE THIS LINE, ERROR OCCURING AND PLEASE READ LOGS");
            		logger.error(site.getCode()+"/CURRENT CURSOR/"+cursor_index+"/PAGE INDEX/"+page_index+"/RETRY CNT/"+retry_cnt);
            		logger.error(site.getCode()+"/SITE URL/"+site.getValue());
            		logger.error("ABOVE THIS LINE, ERROR OCCURING AND PLEASE READ LOGS");
            		break;
            	}
            	
            }            
        }
        
        logger.info(site.getCode()+"/END WRITE DOCUMENT");
		return true;
	}
	
	
	@Override
	public boolean executeGN03(Site site) throws Exception {
		ChromeOptions options = new ChromeOptions();
		options.addArguments("headless");		
		WebDriver driver = new ChromeDriver(options);	
		
		CrawlingVO check_duplicate_vo = dao.checkDuplicateCrawling(site.getCode());
		
		String url = site.getValue();
		// 몇번째 row 인지 
        int cursor_index = 0;
        // 현재 페이지
        int page_index = 1;
        // 실패시 재시도 횟수 
        int retry_cnt = 0;
        
        pageMove(driver, url, 3000);
        while(true){	
            CrawlingVO vo = new CrawlingVO();
            vo.setSite(site.getCode());
            
            try{       	
                vo.setSite_url(url);
                
                List<WebElement> a_links = driver.findElements(By.cssSelector("table > tbody > tr"));
                if (a_links.size() == 0 ) {
                	throw new Exception("Fail to Call Document");
                }
                
                logger.info(site.getCode()+"/CURRENT CURSOR/"+cursor_index+"/MAX CURSOR/"+a_links.size()+"/PAGE INDEX/"+page_index);
                
                if(a_links.size() <= cursor_index){
                	page_index++;
                	cursor_index = 0;
                	JavascriptExecutor executor = (JavascriptExecutor) driver;
                	String javascript_function = "doBbsFPag("+page_index+");";
                    executor.executeScript(javascript_function);
                    waitingResponse(2000);
                    
//                  url = driver.getCurrentUrl();
                    continue;
                }
                
                List<WebElement> tds = driver.findElements(By.cssSelector("table > tbody > tr:nth-child("+(cursor_index+1)+") > td"));
                String board_no = tds.get(0).getText();
                vo.setBoard_no(Integer.parseInt(board_no));
                WebElement click_target = tds.get(1).findElement(By.cssSelector("a"));
                String title = click_target.getText();
                vo.setTitle(title);
                String writer = tds.get(2).getText();
                vo.setWriter(writer);
                List<WebElement> isFileElements = tds.get(3).findElements(By.cssSelector("a"));
                vo.setIsFile(false);
                if(isFileElements.size() != 0) {
                	vo.setIsFile(true);
                }
                
                String created_document = tds.get(4).getText();
                vo.setCreated_document(created_document);
                
                
                JavascriptExecutor executor = (JavascriptExecutor) driver;
                String click_script = click_target.getAttribute("onclick");
                click_script = click_script.replace("return false;", "");                
                executor.executeScript(click_script);
                waitingResponse(2000);
                String detail_path = driver.getCurrentUrl();
                vo.setDetail_url(detail_path);
                
                
                WebElement p_article = driver.findElement(By.cssSelector("#editContents"));
                vo.setDocuments(p_article.getAttribute("innerHTML"));
            	
            	
                if(check_duplicate_vo != null) {
            		if(
        				check_duplicate_vo.getTitle().equals(vo.getTitle())  
        				&& check_duplicate_vo.getWriter().equals(vo.getWriter())
        				&& check_duplicate_vo.getCreated_document().substring(0,10).equals(vo.getCreated_document())
    				) {
                		// 기존에 등록된 곳 까지 insert 시
            			logger.info(site.getCode()+"/ALREADY REGISTER DOCUMENT ");
            			logger.info("TITLE::"+check_duplicate_vo.getTitle()+"/WRITER::"+check_duplicate_vo.getWriter()+"/CRETED_DOCUMENT:"+check_duplicate_vo.getCreated_document());
                		break;
                	}
            	}
            	
            	
            	vo.setStatus("S");
            	dao.insertCrawling(vo);
            	
            	if("1".equals(board_no) ) {
            		// 끝까지 insert 시도 
            		logger.info(site.getCode()+"/END DOCUMENT");
            		break;
            	}
            	
            	// 이 사이트는 목록으로 가는 방법이 목록버튼 눌러서 이동하는 방법이 대기시간이 적어 이 방법으로 진행한다 . 
            	WebElement back_list_page = driver.findElement(By.cssSelector("div.btn_box > a.go_list"));
            	String back_list_page_script = back_list_page.getAttribute("onclick");
            	executor.executeScript(back_list_page_script);
            	waitingResponse(2000);
            	
            	retry_cnt = 0;
            	cursor_index++;
            }catch(Exception e){
            	// retry 경남은  #### Cusor 3번 게시글이 에러가 남.
            	System.out.println(e.getMessage());
            	e.printStackTrace();
            	if(retry_cnt < RETRY_CNT) {
            		logger.warn(site.getCode()+"/CURRENT CURSOR/"+cursor_index+"/PAGE INDEX/"+page_index+"/RETRY CNT/"+retry_cnt);
            		retry_cnt++;
            		continue;
            	}else if(retry_cnt == RETRY_CNT) {
            		vo.setIsFile(false);
                	vo.setStatus("F");
                	dao.insertCrawling(vo);
                	retry_cnt++;
                	// 오류가 났을 시 다음 커서로 이동                
                    cursor_index++;
                    continue;
            	}else {
            		//커서가 이동한뒤에도 반복적 오류가 발생하면 중단
            		logger.error("ABOVE THIS LINE, ERROR OCCURING AND PLEASE READ LOGS");
            		logger.error(site.getCode()+"/CURRENT CURSOR/"+cursor_index+"/PAGE INDEX/"+page_index+"/RETRY CNT/"+retry_cnt);
            		logger.error(site.getCode()+"/SITE URL/"+site.getValue());
            		logger.error("ABOVE THIS LINE, ERROR OCCURING AND PLEASE READ LOGS");
            		break;
            	}            	
            	
                
            }            
        }
        
        logger.info(site.getCode()+"/END WRITE DOCUMENT");
		return true;
	}
	
//	@Scheduled(cron="* * * * * *")
//	private void excuteGN02() throws Exception {
//		this.executeGN02(Site.GN002);
//	}
	
	
	@Override
	public boolean executeGN02(Site site) throws Exception {
		
		ChromeOptions options = new ChromeOptions();
		options.addArguments("headless");		
		WebDriver driver = new ChromeDriver(options);	
		
		CrawlingVO check_duplicate_vo = dao.checkDuplicateCrawling(site.getCode());
		
		String url = site.getValue();
		// 몇번째 row 인지 
        int cursor_index = 0;
        // 현재 페이지
        int page_index = 1;
        // 실패시 재시도 횟수 
        int retry_cnt = 0;
        
        while(true){	
            CrawlingVO vo = new CrawlingVO();
            vo.setSite(site.getCode());
            try{
            	String current_url = url;
            	vo.setSite_url(current_url);
                pageMove(driver, current_url, 2000);

                List<WebElement> a_links = driver.findElements(By.cssSelector(".de-news > table > tbody > tr.table-contents"));
                if (a_links.size() == 0 ) {
                	throw new Exception("Fail to Call Document");
                }
                
                logger.info(site.getCode()+"/CURRENT CURSOR/"+cursor_index+"/MAX CURSOR/"+a_links.size()+"/PAGE INDEX/"+page_index);
                
                if(a_links.size() <= cursor_index){
                	page_index++;
                	cursor_index = 0; 
                    continue;
                }
                
                if((a_links.size()-1) <= cursor_index){
                	break;
                }
                
                List<WebElement> tds = driver.findElements(By.cssSelector(".de-news > table > tbody > tr.table-contents:nth-child("+(cursor_index+1)+") > td"));
                WebElement click_target = tds.get(1);
                String board_type = tds.get(0).getText();
                String title = click_target.getText();
                vo.setBoard_type(board_type);
                vo.setTitle(title);
                
                JavascriptExecutor executor = (JavascriptExecutor) driver;
                String click_script = click_target.getAttribute("onclick");
                System.out.println(click_script);
                executor.executeScript(click_script);
                waitingResponse(2000);
                
//                String detail_path = driver.getCurrentUrl();
//                System.out.println(detail_path);
                vo.setDetail_url(click_script);
                
                WebElement p_article = driver.findElement(By.cssSelector("div.de-open-container > div.detail-contents"));
                vo.setDocuments(p_article.getAttribute("innerHTML"));
            	
            	
            	if(check_duplicate_vo != null) {
            		if(
            				check_duplicate_vo.getTitle().equals(vo.getTitle())  
            				&& check_duplicate_vo.getBoard_type().equals(vo.getBoard_type())
    				) {
                		// 기존에 등록된 곳 까지 insert 시
                		// 기존에 등록된 곳 까지 insert 시
            			logger.info(site.getCode()+"/ALREADY REGISTER DOCUMENT ");
            			logger.info("TITLE::"+check_duplicate_vo.getTitle()+"/BOARD_TYPE::"+check_duplicate_vo.getBoard_type());
                		break;
                	}
            	}
            	
            	
            	vo.setIsFile(false);
            	vo.setStatus("S");
            	dao.insertCrawling(vo);
            	
            	retry_cnt = 0;
            	cursor_index++;
            }catch(Exception e){
            	// retry 경남은  #### Cusor 3번 게시글이 에러가 남.
            	e.printStackTrace();
            	if(retry_cnt < RETRY_CNT) {
            		logger.warn(site.getCode()+"/CURRENT CURSOR/"+cursor_index+"/PAGE INDEX/"+page_index+"/RETRY CNT/"+retry_cnt);
            		retry_cnt++;
            		continue;
            	}else if(retry_cnt == RETRY_CNT) {
            		vo.setIsFile(false);
                	vo.setStatus("F");
                	dao.insertCrawling(vo);
                	retry_cnt++;
                	// 오류가 났을 시 다음 커서로 이동                
                    cursor_index++;
                    continue;
            	}else {
            		//커서가 이동한뒤에도 반복적 오류가 발생하면 중단
            		logger.error("ABOVE THIS LINE, ERROR OCCURING AND PLEASE READ LOGS");
            		logger.error(site.getCode()+"/CURRENT CURSOR/"+cursor_index+"/PAGE INDEX/"+page_index+"/RETRY CNT/"+retry_cnt);
            		logger.error(site.getCode()+"/SITE URL/"+site.getValue());
            		logger.error("ABOVE THIS LINE, ERROR OCCURING AND PLEASE READ LOGS");
            		break;
            	}       
            	
                
            }            
        }
        
        logger.info(site.getCode()+"/END WRITE DOCUMENT");
		return true;
	}
	
	
	@Override
	public boolean executeGN01(Site site) throws Exception {
		ChromeOptions options = new ChromeOptions();
		options.addArguments("headless");		
		WebDriver driver = new ChromeDriver(options);	
		
		CrawlingVO check_duplicate_vo = dao.checkDuplicateCrawling(site.getCode());
		
		String url = site.getValue();
		// 몇번째 row 인지 
        int cursor_index = 0;
        // 현재 페이지
        int page_index = 1;
        // 실패시 재시도 횟수 
        int retry_cnt = 0;
        
        while(true){	
            CrawlingVO vo = new CrawlingVO();
            vo.setSite(site.getCode());
            try{
            	String current_url = url + "&page="+ page_index;
            	vo.setSite_url(current_url);
                pageMove(driver, current_url, 2000);

                List<WebElement> a_links = driver.findElements(By.cssSelector("#list_body > tr > td > a"));
                if (a_links.size() == 0 ) {
                	throw new Exception("Fail to Call Document");
                }
                
                logger.info(site.getCode()+"/CURRENT CURSOR/"+cursor_index+"/MAX CURSOR/"+a_links.size()+"/PAGE INDEX/"+page_index);
                
                if(a_links.size() <= cursor_index){
                	page_index++;
                	cursor_index = 0; 
                    continue;
                }
                
                List<WebElement> tds = driver.findElements(By.cssSelector("#list_body > tr:nth-child("+(cursor_index+1)+") > td"));
                String board_no = tds.get(0).getText();
                vo.setBoard_no(Integer.parseInt(board_no));
                String writer = tds.get(2).getText();
                vo.setWriter(writer);
                List<WebElement> is_files = tds.get(4).findElements(By.cssSelector("span"));
                vo.setIsFile(false);
                if(is_files.size() != 0) {
            		vo.setIsFile(true);
                }
                String created_document = tds.get(5).getText();
                vo.setCreated_document(created_document);
                
                WebElement a = a_links.get(cursor_index);
                String title = a.getText();
                vo.setTitle(title);
                a.click();
                waitingResponse(2000);

                String detail_path = driver.getCurrentUrl();
                vo.setDetail_url(detail_path);
                pageMove(driver, detail_path, 2000);

                WebElement p_article = driver.findElement(By.cssSelector("div.brd_viewer > div.vw_article"));
                vo.setDocuments(p_article.getAttribute("innerHTML"));
                
            	vo.setBoard_type(null);
            	
            	
            	
            	if(check_duplicate_vo != null) {
            		if(
        				check_duplicate_vo.getTitle().equals(vo.getTitle())  
        				&& check_duplicate_vo.getWriter().equals(vo.getWriter())
        				&& check_duplicate_vo.getCreated_document().substring(0,10).equals(vo.getCreated_document())
    				) {
                		// 기존에 등록된 곳 까지 insert 시
                		logger.info(site.getCode()+"/ALREADY REGISTER DOCUMENT");
                		logger.info("TITLE::"+check_duplicate_vo.getTitle()+"/WRITER::"+check_duplicate_vo.getWriter()+"/CRETED_DOCUMENT:"+check_duplicate_vo.getCreated_document());
                		break;
                	}
            	}
            	
            	
            	vo.setStatus("S");
            	dao.insertCrawling(vo);
            	
            	if("1".equals(board_no) ) {
            		// 끝까지 insert 시도 
            		logger.info(site.getCode()+"/END DOCUMENT");
            		break;
            	}
            	
            	retry_cnt = 0;
            	cursor_index++;
            }catch(Exception e){
            	// retry 경남은  #### Cusor 3번 게시글이 에러가 남.
            	e.printStackTrace();
            	if(retry_cnt < RETRY_CNT) {
            		logger.warn(site.getCode()+"/CURRENT CURSOR/"+cursor_index+"/PAGE INDEX/"+page_index+"/RETRY CNT/"+retry_cnt);
            		retry_cnt++;
            		continue;
            	}else if(retry_cnt == RETRY_CNT) {
            		vo.setStatus("F");
                	dao.insertCrawling(vo);
                	retry_cnt++;
                    // 오류가 났을 시 다음 커서로 이동                
                    cursor_index++;
                    continue;
            	}else {
            		//커서가 이동한뒤에도 반복적 오류가 발생하면 중단
            		logger.error("ABOVE THIS LINE, ERROR OCCURING AND PLEASE READ LOGS");
            		logger.error(site.getCode()+"/CURRENT CURSOR/"+cursor_index+"/PAGE INDEX/"+page_index+"/RETRY CNT/"+retry_cnt);
            		logger.error(site.getCode()+"/SITE URL/"+site.getValue());
            		logger.error("ABOVE THIS LINE, ERROR OCCURING AND PLEASE READ LOGS");
            		break;
            	}              	
            	

            }            
        }
        
        logger.info(site.getCode()+"/END WRITE DOCUMENT");
		return true;
	}

	
	public void waitingResponse(int seconds){
        try {Thread.sleep(seconds);} catch (InterruptedException e) {}
    }

	// 페이지 이동시 로드시간을 기다리기 
    public void pageMove(WebDriver driver, String url, int seconds){
        driver.get(url);
        waitingResponse(seconds);
    }
    
	// 뒤로가기 시 로드시간을 기다리기 
    public void pageBack(WebDriver driver, int seconds){
        driver.navigate().back();
        waitingResponse(seconds);
    }
    
}
