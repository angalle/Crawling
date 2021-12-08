package com.srpost.cm.biz.crawling.dao;

import javax.inject.Inject;

import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Repository;

import com.srpost.cm.biz.crawling.vo.CrawlingVO;

@Repository
public class CrawlingDAOImpl implements CrawlingDAO{
	
    @Inject
    private SqlSession sqlSession;
    
    private static final String Namespace = "com.srpost.cm.biz.crawling.mappers.CrawlingMapper";


	@Override
	public CrawlingVO checkDuplicateCrawling(String code) throws Exception {
		return sqlSession.selectOne(Namespace+".checkDuplicateCrawling", code);
	}


	@Override
	public int insertCrawling(CrawlingVO vo) throws Exception {
		// TODO Auto-generated method stub
		return sqlSession.insert(Namespace+".insertCrawling", vo);
	}

}
