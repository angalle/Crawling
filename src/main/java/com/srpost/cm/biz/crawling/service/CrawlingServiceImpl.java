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
import org.springframework.stereotype.Service;

import com.srpost.cm.biz.crawling.Site;
import com.srpost.cm.biz.crawling.dao.CrawlingDAO;
import com.srpost.cm.biz.crawling.vo.CrawlingVO;

@Service
public class CrawlingServiceImpl implements CrawlingService{
	
	private static final Logger logger = LoggerFactory.getLogger(CrawlingServiceImpl.class);
	
	@Inject
    private CrawlingDAO dao;
	//재시도 횟수 
	private static int RETRY_CNT = 5;
	
	@Override
	public boolean executeGN05(Site site) throws Exception {
		ChromeOptions options = new ChromeOptions();
		options.addArguments("headless");		
		WebDriver driver = new ChromeDriver(options);	
		
		CrawlingVO checkDuplicateVo = dao.checkDuplicateCrawling(site.getCode());
		String url = site.getValue();
		// 몇번째 row 인지 
        int cursorIndex = 0;
        // 현재 페이지
        int pageIndex = 1;
        // 실패시 재시도 횟수 
        int retryCnt = 0;
        
        String current_url = url;
        pageMove(driver, current_url, 2000);
        while(true){	
            CrawlingVO vo = new CrawlingVO();
            vo.setSiteType(site.getCode());
            try{
            	vo.setSiteUrl(current_url);   

                List<WebElement> aLinks = driver.findElements(By.cssSelector("table > tbody > tr"));
                if (aLinks.size() == 0 ) {
                	throw new Exception("Fail to Call Document");
                }
                
                logger.info(site.getCode()+"/CURRENT CURSOR/"+cursorIndex+"/MAX CURSOR/"+aLinks.size()+"/PAGE INDEX/"+pageIndex);
                
                if(aLinks.size() <= cursorIndex){
                	pageIndex++;
                	cursorIndex = 0;
                	
                	JavascriptExecutor executor = (JavascriptExecutor) driver;
                	
                	// 이 사이트는 목록으로 가는 방법이 목록버튼 눌러서 이동하는 방법이 대기시간이 적어 이 방법으로 진행한다 . 
                	executor.executeScript("goPaging("+pageIndex+")");
                	waitingResponse(2000);
                    continue;
                }
                
                
                List<WebElement> tds = driver.findElements(By.cssSelector("table > tbody > tr:nth-child("+(cursorIndex+1)+") > td"));
                String docSeq = tds.get(0).getText();
                vo.setDocSeq(Integer.parseInt(docSeq));
                WebElement clickTarget = tds.get(1).findElement(By.cssSelector("a"));
                String title = clickTarget.getText();
                vo.setTitle(title);
                
                String docRegDt = tds.get(2).getText().replace(".", "-");
                vo.setDocRegDt(docRegDt);
                
                List<WebElement> isFile = tds.get(3).findElements(By.cssSelector("img"));
                vo.setFileYn(false);
                if(isFile.size() != 0 ) {
                	vo.setFileYn(true);
                }
                
                clickTarget.click();
                waitingResponse(2000);

                String detailUrl = driver.getCurrentUrl();
                vo.setDetailUrl(detailUrl);
                
                
                WebElement pArticle = driver.findElement(By.cssSelector("textarea"));
                vo.setContents(pArticle.getAttribute("innerHTML"));
                
                if(checkDuplicateVo != null) {
            		if(
            				checkDuplicateVo.getTitle().equals(vo.getTitle())  
        				&& checkDuplicateVo.getDocSeq() == vo.getDocSeq()
        				&& checkDuplicateVo.getDocRegDt().substring(0, 10).equals(vo.getDocRegDt())
    				) {
                		// 기존에 등록된 곳 까지 insert 시
                		logger.info(site.getCode()+"/ALREADY REGISTER DOCUMENT");
                		logger.info("TITLE::"+checkDuplicateVo.getTitle()+"/WRITER::"+checkDuplicateVo.getWriter()+"/CRETED_DOCUMENT:"+checkDuplicateVo.getDocRegDt());
                		break;
                	}
            	}
            	
            	
            	vo.setSuccessYn("Y");
            	dao.insertCrawling(vo);
            	
            	if("1".equals(docSeq) ) {
            		// 끝까지 insert 시도 
            		logger.info(site.getCode()+"/END DOCUMENT");
            		break;
            	}
            	
            	// 이 사이트는 목록으로 가는 방법이 목록버튼 눌러서 이동하는 방법이 대기시간이 적어 이 방법으로 진행한다 .
            	WebElement backListPage = driver.findElement(By.cssSelector("div.contain-div > dl > dd.mar_top_15.txt_alignR > a"));
            	backListPage.click();
            	waitingResponse(2000);
            	
            	retryCnt = 0;
            	cursorIndex++;
            }catch(Exception e){
            	// retry 경남은  #### Cusor 3번 게시글이 에러가 남.
            	e.printStackTrace();
            	if(retryCnt < RETRY_CNT) {
            		logger.warn(site.getCode()+"/CURRENT CURSOR/"+cursorIndex+"/PAGE INDEX/"+pageIndex+"/RETRY CNT/"+retryCnt);
            		retryCnt++;
            		continue;
            	}else if(retryCnt == RETRY_CNT) {
            		vo.setFileYn(false);
                	vo.setSuccessYn("N");
                	dao.insertCrawling(vo);
                	retryCnt++;
                	// 오류가 났을 시 다음 커서로 이동                
                    cursorIndex++;
                   continue;
            	}else {
            		//커서가 이동한뒤에도 반복적 오류가 발생하면 중단
            		logger.error("ABOVE THIS LINE, ERROR OCCURING AND PLEASE READ LOGS");
            		logger.error(site.getCode()+"/CURRENT CURSOR/"+cursorIndex+"/PAGE INDEX/"+pageIndex+"/RETRY CNT/"+retryCnt);
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
	public boolean executeGN04(Site site) throws Exception {
		ChromeOptions options = new ChromeOptions();
		options.addArguments("headless");		
		WebDriver driver = new ChromeDriver(options);	
		
		CrawlingVO checkDuplicateVo = dao.checkDuplicateCrawling(site.getCode());
		
		String url = site.getValue();
		// 몇번째 row 인지 
        int cursorIndex = 0;
        // 현재 페이지
        int pageIndex = 1;
        // 실패시 재시도 횟수 
        int retryCnt = 0;
        
        while(true){	
            CrawlingVO vo = new CrawlingVO();
            vo.setSiteType(site.getCode());
            try{
            	String current_url = url;
            	vo.setSiteUrl(current_url);
                pageMove(driver, current_url, 2000);

                List<WebElement> aLinks = driver.findElements(By.cssSelector("table > tbody > tr"));
                if (aLinks.size() == 0 ) {
                	throw new Exception("Fail to Call Document");
                }
                
                logger.info(site.getCode()+"/CURRENT CURSOR/"+cursorIndex+"/MAX CURSOR/"+aLinks.size()+"/PAGE INDEX/"+pageIndex);
                
                if(aLinks.size() <= cursorIndex){
                	pageIndex++;
                	cursorIndex = 0; 
                    continue;
                }
                
                
                List<WebElement> tds = driver.findElements(By.cssSelector("table > tbody > tr:nth-child("+(cursorIndex+1)+") > td"));
                String docSeq = tds.get(0).getText();
                vo.setDocSeq(Integer.parseInt(docSeq));
                WebElement clickTarget = tds.get(1).findElement(By.cssSelector("a"));
                String title = clickTarget.getText();
                vo.setTitle(title);
                String writer = tds.get(2).getText();
                vo.setWriter(writer);
                
                String docRegDt = tds.get(4).getText();
                vo.setDocRegDt(docRegDt);
                
                clickTarget.click();
                waitingResponse(2000);

                String detailPath = driver.getCurrentUrl();
                vo.setDetailUrl(detailPath);
                
                
                WebElement pArticle = driver.findElement(By.cssSelector("#bo_v_atc"));
                vo.setContents(pArticle.getAttribute("innerHTML"));
                
                List<WebElement> files = driver.findElements(By.cssSelector("#bo_v_file"));
                vo.setFileYn(false);
                if(files.size() != 0) {
                	vo.setFileYn(true);
                }
                
            	
                if(checkDuplicateVo != null) {
            		if(
        				checkDuplicateVo.getTitle().equals(vo.getTitle())  
        				&& checkDuplicateVo.getWriter().equals(vo.getWriter())
        				&& checkDuplicateVo.getDocRegDt().substring(0,10).equals(vo.getDocRegDt())
    				) {
                		// 기존에 등록된 곳 까지 insert 시
                		logger.info(site.getCode()+"/ALREADY REGISTER DOCUMENT");
                		logger.info("TITLE::"+checkDuplicateVo.getTitle()+"/WRITER::"+checkDuplicateVo.getWriter()+"/CRETED_DOCUMENT:"+checkDuplicateVo.getDocRegDt());
                		break;
                	}
            	}
            	
            	
            	vo.setSuccessYn("Y");
            	dao.insertCrawling(vo);
            	
            	if("1".equals(docSeq) ) {
            		// 끝까지 insert 시도 
            		logger.info(site.getCode()+"/END DOCUMENT");
            		break;
            	}
            	
            	retryCnt = 0;
            	cursorIndex++;
            }catch(Exception e){
            	// retry 경남은  #### Cusor 3번 게시글이 에러가 남.
            	e.printStackTrace();
            	if(retryCnt < RETRY_CNT) {
            		logger.warn(site.getCode()+"/CURRENT CURSOR/"+cursorIndex+"/PAGE INDEX/"+pageIndex+"/RETRY CNT/"+retryCnt);
            		retryCnt++;
            		continue;
            	}else if(retryCnt == RETRY_CNT) {
            		vo.setFileYn(false);
                	vo.setSuccessYn("N");
                	dao.insertCrawling(vo);
                	retryCnt++;
                	// 오류가 났을 시 다음 커서로 이동                
                    cursorIndex++;
                   continue;
            	}else {
            		//커서가 이동한뒤에도 반복적 오류가 발생하면 중단
            		logger.error("ABOVE THIS LINE, ERROR OCCURING AND PLEASE READ LOGS");
            		logger.error(site.getCode()+"/CURRENT CURSOR/"+cursorIndex+"/PAGE INDEX/"+pageIndex+"/RETRY CNT/"+retryCnt);
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
		
		CrawlingVO checkDuplicateVo = dao.checkDuplicateCrawling(site.getCode());
		
		String url = site.getValue();
		// 몇번째 row 인지 
        int cursorIndex = 0;
        // 현재 페이지
        int pageIndex = 1;
        // 실패시 재시도 횟수 
        int retryCnt = 0;
        
        pageMove(driver, url, 3000);
        while(true){	
            CrawlingVO vo = new CrawlingVO();
            vo.setSiteType(site.getCode());
            
            try{       	
                vo.setSiteUrl(url);
                
                List<WebElement> aLinks = driver.findElements(By.cssSelector("table > tbody > tr"));
                if (aLinks.size() == 0 ) {
                	throw new Exception("Fail to Call Document");
                }
                
                logger.info(site.getCode()+"/CURRENT CURSOR/"+cursorIndex+"/MAX CURSOR/"+aLinks.size()+"/PAGE INDEX/"+pageIndex);
                
                if(aLinks.size() <= cursorIndex){
                	pageIndex++;
                	cursorIndex = 0;
                	JavascriptExecutor executor = (JavascriptExecutor) driver;
                	String javascript_function = "doBbsFPag("+pageIndex+");";
                    executor.executeScript(javascript_function);
                    waitingResponse(2000);
                    
//                  url = driver.getCurrentUrl();
                    continue;
                }
                
                List<WebElement> tds = driver.findElements(By.cssSelector("table > tbody > tr:nth-child("+(cursorIndex+1)+") > td"));
                String docSeq = tds.get(0).getText();
                vo.setDocSeq(Integer.parseInt(docSeq));
                WebElement clickTarget = tds.get(1).findElement(By.cssSelector("a"));
                String title = clickTarget.getText();
                vo.setTitle(title);
                String writer = tds.get(2).getText();
                vo.setWriter(writer);
                List<WebElement> isFileElements = tds.get(3).findElements(By.cssSelector("a"));
                vo.setFileYn(false);
                if(isFileElements.size() != 0) {
                	vo.setFileYn(true);
                }
                
                String docRegDt = tds.get(4).getText();
                vo.setDocRegDt(docRegDt);
                
                
                JavascriptExecutor executor = (JavascriptExecutor) driver;
                String clickScript = clickTarget.getAttribute("onclick");
                clickScript = clickScript.replace("return false;", "");                
                executor.executeScript(clickScript);
                waitingResponse(2000);
                String detailPath = driver.getCurrentUrl();
                vo.setDetailUrl(detailPath);
                
                
                WebElement pArticle = driver.findElement(By.cssSelector("#editContents"));
                vo.setContents(pArticle.getAttribute("innerHTML"));
            	
            	
                if(checkDuplicateVo != null) {
            		if(
        				checkDuplicateVo.getTitle().equals(vo.getTitle())  
        				&& checkDuplicateVo.getWriter().equals(vo.getWriter())
        				&& checkDuplicateVo.getDocRegDt().substring(0,10).equals(vo.getDocRegDt())
    				) {
                		// 기존에 등록된 곳 까지 insert 시
            			logger.info(site.getCode()+"/ALREADY REGISTER DOCUMENT ");
            			logger.info("TITLE::"+checkDuplicateVo.getTitle()+"/WRITER::"+checkDuplicateVo.getWriter()+"/CRETED_DOCUMENT:"+checkDuplicateVo.getDocRegDt());
                		break;
                	}
            	}
            	
            	
            	vo.setSuccessYn("Y");
            	dao.insertCrawling(vo);
            	
            	if("1".equals(docSeq) ) {
            		// 끝까지 insert 시도 
            		logger.info(site.getCode()+"/END DOCUMENT");
            		break;
            	}
            	
            	// 이 사이트는 목록으로 가는 방법이 목록버튼 눌러서 이동하는 방법이 대기시간이 적어 이 방법으로 진행한다 . 
            	WebElement backListPage = driver.findElement(By.cssSelector("div.btn_box > a.go_list"));
            	String backListPageScript = backListPage.getAttribute("onclick");
            	executor.executeScript(backListPageScript);
            	waitingResponse(2000);
            	
            	retryCnt = 0;
            	cursorIndex++;
            }catch(Exception e){
            	// retry 경남은  #### Cusor 3번 게시글이 에러가 남.
            	System.out.println(e.getMessage());
            	e.printStackTrace();
            	if(retryCnt < RETRY_CNT) {
            		logger.warn(site.getCode()+"/CURRENT CURSOR/"+cursorIndex+"/PAGE INDEX/"+pageIndex+"/RETRY CNT/"+retryCnt);
            		retryCnt++;
            		continue;
            	}else if(retryCnt == RETRY_CNT) {
            		vo.setFileYn(false);
                	vo.setSuccessYn("N");
                	dao.insertCrawling(vo);
                	retryCnt++;
                	// 오류가 났을 시 다음 커서로 이동                
                    cursorIndex++;
                    continue;
            	}else {
            		//커서가 이동한뒤에도 반복적 오류가 발생하면 중단
            		logger.error("ABOVE THIS LINE, ERROR OCCURING AND PLEASE READ LOGS");
            		logger.error(site.getCode()+"/CURRENT CURSOR/"+cursorIndex+"/PAGE INDEX/"+pageIndex+"/RETRY CNT/"+retryCnt);
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
	public boolean executeGN02(Site site) throws Exception {
		
		ChromeOptions options = new ChromeOptions();
		options.addArguments("headless");		
		WebDriver driver = new ChromeDriver(options);	
		
		CrawlingVO checkDuplicateVo = dao.checkDuplicateCrawling(site.getCode());
		
		String url = site.getValue();
		// 몇번째 row 인지 
        int cursorIndex = 0;
        // 현재 페이지
        int pageIndex = 1;
        // 실패시 재시도 횟수 
        int retryCnt = 0;
        
        while(true){	
            CrawlingVO vo = new CrawlingVO();
            vo.setSiteType(site.getCode());
            try{
            	String current_url = url;
            	vo.setSiteUrl(current_url);
                pageMove(driver, current_url, 2000);

                List<WebElement> aLinks = driver.findElements(By.cssSelector(".de-news > table > tbody > tr.table-contents"));
                if (aLinks.size() == 0 ) {
                	throw new Exception("Fail to Call Document");
                }
                
                logger.info(site.getCode()+"/CURRENT CURSOR/"+cursorIndex+"/MAX CURSOR/"+aLinks.size()+"/PAGE INDEX/"+pageIndex);
                
                if(aLinks.size() <= cursorIndex){
                	pageIndex++;
                	cursorIndex = 0; 
                    continue;
                }
                
                if((aLinks.size()-1) <= cursorIndex){
                	break;
                }
                
                List<WebElement> tds = driver.findElements(By.cssSelector(".de-news > table > tbody > tr.table-contents:nth-child("+(cursorIndex+1)+") > td"));
                WebElement clickTarget = tds.get(1);
                String board_type = tds.get(0).getText();
                String title = clickTarget.getText();
                vo.setDocType(board_type);
                vo.setTitle(title);
                
                JavascriptExecutor executor = (JavascriptExecutor) driver;
                String clickScript = clickTarget.getAttribute("onclick");
                executor.executeScript(clickScript);
                waitingResponse(2000);
                
                vo.setDetailUrl(clickScript);
                
                WebElement pArticle = driver.findElement(By.cssSelector("div.de-open-container > div.detail-contents"));
                vo.setContents(pArticle.getAttribute("innerHTML"));
            	
            	
            	if(checkDuplicateVo != null) {
            		if(
            				checkDuplicateVo.getTitle().equals(vo.getTitle())  
            				&& checkDuplicateVo.getDocType().equals(vo.getDocType())
    				) {
                		// 기존에 등록된 곳 까지 insert 시
            			logger.info(site.getCode()+"/ALREADY REGISTER DOCUMENT ");
            			logger.info("TITLE::"+checkDuplicateVo.getTitle()+"/BOARD_TYPE::"+checkDuplicateVo.getDocType());
                		break;
                	}
            	}
            	
            	
            	vo.setFileYn(false);
            	vo.setSuccessYn("Y");
            	dao.insertCrawling(vo);
            	
            	retryCnt = 0;
            	cursorIndex++;
            }catch(Exception e){
            	// retry 경남은  #### Cusor 3번 게시글이 에러가 남.
            	e.printStackTrace();
            	if(retryCnt < RETRY_CNT) {
            		logger.warn(site.getCode()+"/CURRENT CURSOR/"+cursorIndex+"/PAGE INDEX/"+pageIndex+"/RETRY CNT/"+retryCnt);
            		retryCnt++;
            		continue;
            	}else if(retryCnt == RETRY_CNT) {
            		vo.setFileYn(false);
                	vo.setSuccessYn("N");
                	dao.insertCrawling(vo);
                	retryCnt++;
                	// 오류가 났을 시 다음 커서로 이동                
                    cursorIndex++;
                    continue;
            	}else {
            		//커서가 이동한뒤에도 반복적 오류가 발생하면 중단
            		logger.error("ABOVE THIS LINE, ERROR OCCURING AND PLEASE READ LOGS");
            		logger.error(site.getCode()+"/CURRENT CURSOR/"+cursorIndex+"/PAGE INDEX/"+pageIndex+"/RETRY CNT/"+retryCnt);
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
		
		CrawlingVO checkDuplicateVo = dao.checkDuplicateCrawling(site.getCode());
		
		String url = site.getValue();
		// 몇번째 row 인지 
        int cursorIndex = 0;
        // 현재 페이지
        int pageIndex = 1;
        // 실패시 재시도 횟수 
        int retryCnt = 0;
        
        while(true){	
            CrawlingVO vo = new CrawlingVO();
            vo.setSiteType(site.getCode());
            
            try{
            	String current_url = url + "&page="+ pageIndex;
            	vo.setSiteUrl(current_url);
                pageMove(driver, current_url, 2500);

                List<WebElement> aLinks = driver.findElements(By.cssSelector("#list_body > tr > td > a"));
                if (aLinks.size() == 0 ) {
                	throw new Exception("Fail to Call Document");
                }
                
                logger.info(site.getCode()+"/CURRENT CURSOR/"+cursorIndex+"/MAX CURSOR/"+aLinks.size()+"/PAGE INDEX/"+pageIndex);
                
                if(aLinks.size() <= cursorIndex){
                	pageIndex++;
                	cursorIndex = 0; 
                    continue;
                }
                
                List<WebElement> tds = driver.findElements(By.cssSelector("#list_body > tr:nth-child("+(cursorIndex+1)+") > td"));
                String docSeq = tds.get(0).getText();
                vo.setDocSeq(Integer.parseInt(docSeq));
                
                String writer = tds.get(2).getText();
                vo.setWriter(writer);
                List<WebElement> fileYN = tds.get(4).findElements(By.cssSelector("span"));
                vo.setFileYn(false);
                if(fileYN.size() != 0) {
            		vo.setFileYn(true);
                }
                String docRegDt = tds.get(5).getText();
                vo.setDocRegDt(docRegDt);
                
                
                WebElement a = aLinks.get(cursorIndex);
                String title = a.getText();
                vo.setTitle(title);
                a.click();
                waitingResponse(2500);

                String detailPath = driver.getCurrentUrl();
                vo.setDetailUrl(detailPath);

                WebElement pArticle = driver.findElement(By.cssSelector("div.brd_viewer > div.vw_article"));
                vo.setContents(pArticle.getAttribute("innerHTML"));
                
            	vo.setDocType(null);
            	
            	if(checkDuplicateVo != null) {
            		if(
        				checkDuplicateVo.getTitle().equals(vo.getTitle())  
        				&& checkDuplicateVo.getWriter().equals(vo.getWriter())
        				&& checkDuplicateVo.getDocRegDt().substring(0,10).equals(vo.getDocRegDt())
    				) {
                		// 기존에 등록된 곳 까지 insert 시
                		logger.info(site.getCode()+"/ALREADY REGISTER DOCUMENT");
                		logger.info("TITLE::"+checkDuplicateVo.getTitle()+"/WRITER::"+checkDuplicateVo.getWriter()+"/CRETED_DOCUMENT:"+checkDuplicateVo.getDocRegDt());
                		break;
                	}
            	}
            	
            	
            	vo.setSuccessYn("Y");
            	dao.insertCrawling(vo);
            	
            	if("1".equals(docSeq) ) {
            		// 끝까지 insert 시도 
            		logger.info(site.getCode()+"/END DOCUMENT");
            		break;
            	}
            	
            	retryCnt = 0;
            	cursorIndex++;
            }catch(Exception e){
            	// retry 경남은  #### Cusor 3번 게시글이 에러가 남.
            	e.printStackTrace();
            	if(retryCnt < RETRY_CNT) {
            		logger.warn(site.getCode()+"/CURRENT CURSOR/"+cursorIndex+"/PAGE INDEX/"+pageIndex+"/RETRY CNT/"+retryCnt);
            		retryCnt++;
            		continue;
            	}else if(retryCnt == RETRY_CNT) {
            		vo.setSuccessYn("N");
            		
                	dao.insertCrawling(vo);
                	retryCnt++;
                    // 오류가 났을 시 다음 커서로 이동                
                    cursorIndex++;
                    continue;
            	}else {
            		//커서가 이동한뒤에도 반복적 오류가 발생하면 중단
            		logger.error("ABOVE THIS LINE, ERROR OCCURING AND PLEASE READ LOGS");
            		logger.error(site.getCode()+"/CURRENT CURSOR/"+cursorIndex+"/PAGE INDEX/"+pageIndex+"/RETRY CNT/"+retryCnt);
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
