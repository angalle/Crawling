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

import com.srpost.cm.biz.crawling.CrawlingUtils;
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
        
        // 반복문의 flag, 1일때 종료 
        String docSeq = "";
        
        String current_url = url;
        CrawlingUtils.pageMove(driver, current_url, 2000);
        while(!"1".equals(docSeq)){	
            CrawlingVO vo = new CrawlingVO();
            String siteType = site.getCode();
            String writer = null;
            boolean fileYn = false;
            String docRegDt = null;
            String title = null;
            String detailPath = null;
            String contents = null;
            String board_type = null;
            
            try{
            	
            	List<WebElement> aLinks = CrawlingUtils.getaLinkInTable(driver, "table > tbody > tr");
                
                logger.info(site.getCode()+"/CURRENT CURSOR/"+cursorIndex+"/MAX CURSOR/"+aLinks.size()+"/PAGE INDEX/"+pageIndex);
                
                if(aLinks.size() <= cursorIndex){
                	pageIndex++;
                	cursorIndex = 0;
                	
                	CrawlingUtils.executeJavascript(driver, "goPaging("+pageIndex+")");
                	CrawlingUtils.waitingResponse(2000);
                    continue;
                }
                
                
                List<WebElement> tds = driver.findElements(By.cssSelector("table > tbody > tr:nth-child("+(cursorIndex+1)+") > td"));
                docSeq = tds.get(0).getText();
                WebElement clickTarget = tds.get(1).findElement(By.cssSelector("a"));
                String postBoardSeqParam = clickTarget.getAttribute("boardseq");
                title = clickTarget.getText();
                docRegDt = tds.get(2).getText().replace(".", "-");
                fileYn = CrawlingUtils.isFileCheck(tds.get(3).findElements(By.cssSelector("img")));
                
                clickTarget.click();
                CrawlingUtils.waitingResponse(2000);

                detailPath = driver.getCurrentUrl();
                String json = null;
                if(postBoardSeqParam != null) {
                	json = "{" +
                        	"\"targetBoardSeq\": "+postBoardSeqParam+
                        "}";
                }
                
                if(!CrawlingUtils.retryHttpPostRequest(site.getDetail(), json)) {
                	logger.info(site.getCode()+"/CURRENT CURSOR/"+cursorIndex+"/PAGE INDEX/"+pageIndex+"/SITE/"+detailPath);
                	cursorIndex++;
                	continue;
                }
                
                contents = driver.findElement(By.cssSelector("textarea")).getAttribute("innerHTML");
                
                vo = CrawlingUtils.setVOProperty(vo, docSeq, writer, title, fileYn, detailPath, docRegDt, contents, board_type, siteType, json, null);
                
                if(CrawlingUtils.isDuplicateSavePoint(checkDuplicateVo, vo)) {
            		break;
            	}
            	
            	vo.setSuccessYn("Y");
            	dao.insertCrawling(vo);
            	
            	// 이 사이트는 목록으로 가는 방법이 목록버튼 눌러서 이동하는 방법이 대기시간이 적어 이 방법으로 진행한다 .
            	driver.findElement(By.cssSelector("div.contain-div > dl > dd.mar_top_15.txt_alignR > a")).click();
            	CrawlingUtils.waitingResponse(2000);
            	
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
            		vo = CrawlingUtils.setVOProperty(vo, docSeq, writer, title, fileYn, detailPath, docRegDt, contents, board_type, siteType, null, null);
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
            		driver.quit();
            		break;
            	}
            	
            }            
        }
        driver.quit();
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
        
        // 반복문의 flag, 1일때 종료 
        String docSeq = "";
        
        while(!"1".equals(docSeq)){	
            CrawlingVO vo = new CrawlingVO();
            String siteType = site.getCode();
            String writer = null;
            boolean fileYn = false;
            String docRegDt = null;
            String title = null;
            String detailPath = null;
            String contents = null;
            String board_type = null;
            
            try{
            	CrawlingUtils.pageMove(driver, url, 2500);
                
                List<WebElement> aLinks = CrawlingUtils.getaLinkInTable(driver, "table > tbody > tr");
                
                logger.info(site.getCode()+"/CURRENT CURSOR/"+cursorIndex+"/MAX CURSOR/"+aLinks.size()+"/PAGE INDEX/"+pageIndex);
                
                if(aLinks.size() <= cursorIndex){
                	pageIndex++;
                	cursorIndex = 0; 
                    continue;
                }
                
                
                List<WebElement> tds = driver.findElements(By.cssSelector("table > tbody > tr:nth-child("+(cursorIndex+1)+") > td"));
                docSeq = tds.get(0).getText();
                WebElement clickTarget = tds.get(1).findElement(By.cssSelector("a"));
                title = clickTarget.getText();
                writer = tds.get(2).getText();
                docRegDt = tds.get(4).getText();
                
                clickTarget.click();
                CrawlingUtils.waitingResponse(2500);
                
                detailPath = driver.getCurrentUrl();
                if(!CrawlingUtils.isRetryHttpGetRequest(detailPath)) {
                	logger.info(site.getCode()+"/CURRENT CURSOR/"+cursorIndex+"/PAGE INDEX/"+pageIndex+"/SITE/"+detailPath);
                	cursorIndex++;
                	continue;
                }
                contents = driver.findElement(By.cssSelector("#bo_v_atc")).getAttribute("innerHTML");
                fileYn = CrawlingUtils.isFileCheck(driver.findElements(By.cssSelector("#bo_v_file")));
                
                vo = CrawlingUtils.setVOProperty(vo, docSeq, writer, title, fileYn, detailPath, docRegDt, contents, board_type, siteType, null, null);
                
                if(CrawlingUtils.isDuplicateSavePoint(checkDuplicateVo, vo)) {
            		break;
            	}
            	
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
            		vo = CrawlingUtils.setVOProperty(vo, docSeq, writer, title, fileYn, detailPath, docRegDt, contents, board_type, siteType, null, null);
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
            		driver.quit();
            		break;
            	}
            	
            }            
        }
        driver.quit();
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
        
        // 반복문의 flag, 1일때 종료 
        String docSeq = "";
        
        CrawlingUtils.pageMove(driver, url, 3000);
        while(!"1".equals(docSeq)){	
            CrawlingVO vo = new CrawlingVO();
            String siteType = site.getCode();
            String writer = null;
            boolean fileYn = false;
            String docRegDt = null;
            String title = null;
            String detailPath = null;
            String contents = null;
            String board_type = null;
            
            try{       	
            	List<WebElement> aLinks = CrawlingUtils.getaLinkInTable(driver, "table > tbody > tr");
                
                logger.info(site.getCode()+"/CURRENT CURSOR/"+cursorIndex+"/MAX CURSOR/"+aLinks.size()+"/PAGE INDEX/"+pageIndex);
                
                if(aLinks.size() <= cursorIndex){
                	pageIndex++;
                	cursorIndex = 0;
                	
                	CrawlingUtils.executeJavascript(driver, "doBbsFPag("+pageIndex+");");
                    CrawlingUtils.waitingResponse(2000);
                    continue;
                }
                
                List<WebElement> tds = driver.findElements(By.cssSelector("table > tbody > tr:nth-child("+(cursorIndex+1)+") > td"));
                
                docSeq = tds.get(0).getText();
                WebElement clickTarget = tds.get(1).findElement(By.cssSelector("a"));
                title = clickTarget.getText();
                writer = tds.get(2).getText();
                fileYn = CrawlingUtils.isFileCheck(tds.get(3).findElements(By.cssSelector("a")));
                docRegDt = tds.get(4).getText();
                
                CrawlingUtils.executeAttributeJavascript(driver, "onclick", clickTarget);
                CrawlingUtils.waitingResponse(2000);
                
                detailPath = driver.getCurrentUrl();
                if(!CrawlingUtils.isRetryHttpGetRequest(detailPath)) {
                	logger.info(site.getCode()+"/CURRENT CURSOR/"+cursorIndex+"/PAGE INDEX/"+pageIndex+"/SITE/"+detailPath);
                	cursorIndex++;
                	continue;
                }
                contents = driver.findElement(By.cssSelector("#editContents")).getAttribute("innerHTML");
            	
                vo = CrawlingUtils.setVOProperty(vo, docSeq, writer, title, fileYn, detailPath, docRegDt, contents, board_type, siteType, null, null);
                
                if(CrawlingUtils.isDuplicateSavePoint(checkDuplicateVo, vo)) {
            		break;
            	}
            	
            	
            	vo.setSuccessYn("Y");
            	dao.insertCrawling(vo);
            	
            	// 이 사이트는 목록으로 가는 방법이 목록버튼 눌러서 이동하는 방법이 대기시간이 적어 이 방법으로 진행한다 . 
            	WebElement backListPage = driver.findElement(By.cssSelector("div.btn_box > a.go_list"));
            	CrawlingUtils.executeAttributeJavascript(driver, "onclick", backListPage);
            	CrawlingUtils.waitingResponse(2000);
            	
            	retryCnt = 0;
            	cursorIndex++;
            }catch(Exception e){
            	e.printStackTrace();
            	if(retryCnt < RETRY_CNT) {
            		logger.warn(site.getCode()+"/CURRENT CURSOR/"+cursorIndex+"/PAGE INDEX/"+pageIndex+"/RETRY CNT/"+retryCnt);
            		retryCnt++;
            		continue;
            	}else if(retryCnt == RETRY_CNT) {
            		vo = CrawlingUtils.setVOProperty(vo, docSeq, writer, title, fileYn, detailPath, docRegDt, contents, board_type, siteType, null, null);
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
            		driver.quit();
            		break;
            	}            	
            	
                
            }            
        }
        driver.quit();
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
        
        // 반복문의 flag, 1일때 종료 
        String docSeq = null;
        
        CrawlingUtils.pageMove(driver, url, 2000);
        
        List<WebElement> bugFixTarget = driver.findElements(By.cssSelector(".de-search-btn"));
        bugFixTarget.get(1).click();
        CrawlingUtils.waitingResponse(2000);
        
        WebElement startSelect = driver.findElement(By.cssSelector("select.de-search-select"));
    	List<WebElement> startSelectOptions = startSelect.findElements(By.cssSelector("option"));
    	WebElement startClickOption = startSelectOptions.get(0);
    	String year = startClickOption.getText();
    	
        while(true){	
            CrawlingVO vo = new CrawlingVO();
            String siteType = site.getCode();
            String writer = null;
            boolean fileYn = false;
            String docRegDt = null;
            String title = null;
            String detailPath = null;
            String contents = null;
            String board_type = null;
            
            
            try{
            	
                List<WebElement> aLinks = CrawlingUtils.getaLinkInTable(driver, ".de-news > table > tbody > tr.table-contents");
                docSeq = Integer.toString(aLinks.size() - cursorIndex);
                logger.info(site.getCode()+"/CURRENT CURSOR/"+cursorIndex+"/MAX CURSOR/"+aLinks.size()+"/PAGE INDEX/"+pageIndex);
                
                if((aLinks.size()) <= cursorIndex) {
                	// 다음페이지로 
                	year = CrawlingUtils.retryPageReload(driver, pageIndex);
                	if(year.equals("false")) break;
                	
                	CrawlingUtils.waitingResponse(2000);
                	
                	pageIndex++;
                	cursorIndex = 0;
                	continue;
                }
                
                List<WebElement> tds = driver.findElements(By.cssSelector(".de-news > table > tbody > tr.table-contents:nth-child("+(cursorIndex+1)+") > td"));
                WebElement clickTarget = tds.get(1);
                board_type = tds.get(0).getText();
                title = clickTarget.getText();
                String attr = clickTarget.getAttribute("onclick");
                String json = null;
                if(attr != null) {
                	String bizSeq = clickTarget.getAttribute("onclick").split(",")[1];
                    bizSeq = bizSeq.split(":")[1].replaceAll("'", "").replace("}", "");
                    json = "{" +
                    	"\"bizSeq\": "+bizSeq+"," +
                    	"\"history\": {\"url\":[\"/biz/infoList\"]}" +
                    "}";
                }
                
                if(!CrawlingUtils.retryHttpPostRequest(site.getDetail(), json)) {
                	logger.info(site.getCode()+"/CURRENT CURSOR/"+cursorIndex+"/PAGE INDEX/"+pageIndex+"/SITE/"+detailPath);
                	cursorIndex++;
                	continue;
                }
                
                CrawlingUtils.executeAttributeJavascript(driver, null, clickTarget);
                CrawlingUtils.waitingResponse(2000);
                
                detailPath = driver.getCurrentUrl();
                
                
                
                contents = driver.findElement(By.cssSelector("div.de-open-container > div.detail-contents")).getAttribute("innerHTML");
            	
                vo = CrawlingUtils.setVOProperty(vo, docSeq, writer, title, fileYn, detailPath, docRegDt, contents, board_type, siteType, json, year);
                
            	if(CrawlingUtils.isDuplicateSavePointByTwoType(checkDuplicateVo, vo)) {
            		break;
            	}
            	
            	vo.setSuccessYn("Y");
            	dao.insertCrawling(vo);
            	
            	WebElement clickListBackTarget = driver.findElement(By.cssSelector("button.de-btn.blue"));
            	clickListBackTarget.click();
            	CrawlingUtils.waitingResponse(2000);
            	
            	retryCnt = 0;
            	cursorIndex++;
            }catch(Exception e){
            	// retry 경남은  #### Cusor 3번 게시글이 에러가 남.
            	e.printStackTrace();
            	if(retryCnt < RETRY_CNT) {
            		CrawlingUtils.pageMove(driver, url, 2000);
            		year = CrawlingUtils.retryPageReload(driver, pageIndex);
                	CrawlingUtils.waitingResponse(2000);
            		
            		logger.warn(site.getCode()+"/CURRENT CURSOR/"+cursorIndex+"/PAGE INDEX/"+pageIndex+"/RETRY CNT/"+retryCnt);
            		retryCnt++;
            		continue;
            	}else if(retryCnt == RETRY_CNT) {
            		vo = CrawlingUtils.setVOProperty(vo, docSeq, writer, title, fileYn, detailPath, docRegDt, contents, board_type, siteType, null, year);
                	dao.insertCrawling(vo);
                	retryCnt++;
                	// 오류가 났을 시 다음 커서로 이동                
                    cursorIndex++;
                    
                    CrawlingUtils.pageMove(driver, url, 2000);
                	year = CrawlingUtils.retryPageReload(driver, pageIndex);
                	CrawlingUtils.waitingResponse(2000);
                    continue;
            	}else {
            		//커서가 이동한뒤에도 반복적 오류가 발생하면 중단
            		logger.error("ABOVE THIS LINE, ERROR OCCURING AND PLEASE READ LOGS");
            		logger.error(site.getCode()+"/CURRENT CURSOR/"+cursorIndex+"/PAGE INDEX/"+pageIndex+"/RETRY CNT/"+retryCnt);
            		logger.error(site.getCode()+"/SITE URL/"+site.getValue());
            		logger.error("ABOVE THIS LINE, ERROR OCCURING AND PLEASE READ LOGS");
            		driver.quit();
            		break;
            	}       
            	
                
            }            
        }
        driver.quit();
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
        // 반복문의 flag, 1일때 종료 
        String docSeq = null;
        
        
        while(!"1".equals(docSeq)){	
            CrawlingVO vo = new CrawlingVO();
            String siteType = site.getCode();
            String writer = null;
            boolean fileYn = false;
            String docRegDt = null;
            String title = null;
            String detailPath = null;
            String contents = null;
            
            try{
            	String current_url = url + "&page="+ pageIndex;
            	CrawlingUtils.pageMove(driver, current_url, 2500);

                List<WebElement> aLinks = CrawlingUtils.getaLinkInTable(driver, "#list_body > tr > td > a");
                
                // 현재 상태 
                logger.info(site.getCode()+"/CURRENT CURSOR/"+cursorIndex+"/MAX CURSOR/"+aLinks.size()+"/PAGE INDEX/"+pageIndex);
                
                // 다음 페이지 
                if(aLinks.size() <= cursorIndex){
                	pageIndex++;
                	cursorIndex = 0; 
                    continue;
                }
                
                List<WebElement> tds = driver.findElements(By.cssSelector("#list_body > tr:nth-child("+(cursorIndex+1)+") > td"));
                
                docSeq = tds.get(0).getText();
                writer = tds.get(2).getText();
                fileYn = CrawlingUtils.isFileCheck(tds.get(4).findElements(By.cssSelector("span")));
                docRegDt = tds.get(5).getText();
                WebElement a = aLinks.get(cursorIndex);
                title = a.getText();
                
                a.click();
                CrawlingUtils.waitingResponse(2500);

                detailPath = driver.getCurrentUrl();
                if(!CrawlingUtils.isRetryHttpGetRequest(detailPath)) {
                	logger.info(site.getCode()+"/CURRENT CURSOR/"+cursorIndex+"/PAGE INDEX/"+pageIndex+"/SITE/"+detailPath);
                	cursorIndex++;
                	continue;
                }
                contents = driver.findElement(By.cssSelector("div.brd_viewer > div.vw_article")).getAttribute("innerHTML");
                
                vo = CrawlingUtils.setVOProperty(vo, docSeq, writer, title, fileYn, detailPath, docRegDt, contents, null, siteType, null, null);
            	
            	if(CrawlingUtils.isDuplicateSavePoint(checkDuplicateVo, vo)) {
            		break;
            	}
            	
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
            		vo = CrawlingUtils.setVOProperty(vo, docSeq, writer, title, fileYn, detailPath, docRegDt, contents, null, siteType, null, null);
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
            		driver.quit();
            		break;
            	}              	
            	

            }            
        }
        driver.quit();
        logger.info(site.getCode()+"/END WRITE DOCUMENT");
		return true;
	}
	
	
    
}
