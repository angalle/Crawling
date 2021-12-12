package com.srpost.cm.biz.crawling;

import java.util.Locale;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.srpost.cm.biz.crawling.service.CrawlingService;

/**
 * Handles requests for the application home page.
 */
@Controller
public class HomeController {

	private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

	public static final String WEB_DRIVER_ID = "webdriver.chrome.driver"; // 드라이버 ID
	public static final String WEB_DRIVER_PATH = "/Users/test/Documents/eclipse-workspace/Crawling/chromedriver"; // 드라이버 경로

	@Inject
	private CrawlingService service;

	/**
	 * Simply selects the home view to render by returning its name.
	 */
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String home(Locale locale, Model model) throws Exception {
		logger.info("home");
		setSystemProperties();
		
//		if(!service.executeGN01(Site.GN001)) {
//			return "Fail01";	
//		}
		
//		if(!service.executeGN02(Site.GN002)) {
//			return "Fail02";
//		}
		
//		if(!service.executeGN04(Site.GN003)) {
//			return "Fail03";
//		}
		
//		if(!service.executeGN04(Site.GN004)) {
//			return "Fail04";
//		}
		
		if(!service.executeGN05(Site.GN005)) {
			return "Fail05";
		}
		
		return "Success";
		

	}

	private void setSystemProperties() {
		try {
			System.setProperty(WEB_DRIVER_ID, WEB_DRIVER_PATH);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
