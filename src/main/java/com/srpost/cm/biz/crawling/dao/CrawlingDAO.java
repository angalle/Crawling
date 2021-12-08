package com.srpost.cm.biz.crawling.dao;

import com.srpost.cm.biz.crawling.vo.CrawlingVO;

public interface CrawlingDAO {
	
	public CrawlingVO checkDuplicateCrawling(String code) throws Exception;
	
	public int insertCrawling(CrawlingVO vo) throws Exception;

}
