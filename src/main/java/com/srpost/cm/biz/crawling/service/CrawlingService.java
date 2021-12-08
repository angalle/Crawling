package com.srpost.cm.biz.crawling.service;

import com.srpost.cm.biz.crawling.Site;

public interface CrawlingService {
	
//	public List<CrawlingVO> selectCrawling() throws Exception;
	public boolean executeGN01(Site site) throws Exception;
	public boolean executeGN02(Site site) throws Exception;
}
