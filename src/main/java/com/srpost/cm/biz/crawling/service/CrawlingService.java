package com.srpost.cm.biz.crawling.service;

import com.srpost.cm.biz.crawling.Site;

public interface CrawlingService {
	
	public boolean executeGN01(Site site) throws Exception;
	public boolean executeGN02(Site site) throws Exception;
	public boolean executeGN03(Site site) throws Exception;
	public boolean executeGN04(Site site) throws Exception;
	public boolean executeGN05(Site site) throws Exception;
}
