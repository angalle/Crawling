package com.srpost.cm.biz.crawling.schdule;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.srpost.cm.biz.crawling.Site;
import com.srpost.cm.biz.crawling.service.CrawlingService;

/**
 * Handles requests for the application home page.
 */
@Component("GNscheduler")
public class Schduler {
	
	@Inject
	private CrawlingService service;
	
	public static final String WEB_DRIVER_ID = "webdriver.chrome.driver"; // 드라이버 ID
	public static final String WEB_DRIVER_PATH = "/Users/test/Documents/eclipse-workspace/Crawling/chromedriver"; // 드라이버 경로

//	@Scheduled(cron="0 0/1 * * * *")
	public void excuteGNJobs() throws Exception {
		setSystemProperties();
		service.executeGN01(Site.GN001);
		service.executeGN02(Site.GN002);
		service.executeGN03(Site.GN003);
		service.executeGN04(Site.GN004);
		service.executeGN05(Site.GN005);
	}
	
	private void setSystemProperties() {
		try {
			System.setProperty(WEB_DRIVER_ID, WEB_DRIVER_PATH);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
