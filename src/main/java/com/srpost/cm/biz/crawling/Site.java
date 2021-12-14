package com.srpost.cm.biz.crawling;

public enum Site {
	GN001("CJHS", "https://ccei.creativekorea.or.kr/gyeongnam/allim/allim_list.do?div_code=1"),
	GN002("TCNP", "https://www.gntp.or.kr/biz/infoList"),
	GN003("JSVT", "https://www.mss.go.kr/site/gyeongnam/ex/bbs/List.do?cbIdx=292"),
	GN004("SNBO", "https://www.gnsinbo.or.kr/bbs/board.php?bo_table=1_2_3"),
	GN005("JSJS", "https://www2.ripc.org/regional/notice/changwon/bizNoticeList.do");
	
	
	private final String code;
    private final String value;

    Site(String code, String value){
    	this.code = code;
    	this.value = value; 
	}

    public String getValue() { return value; }
    public String getCode() { return code; }
}
