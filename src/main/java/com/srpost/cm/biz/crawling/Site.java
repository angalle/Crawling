package com.srpost.cm.biz.crawling;

public enum Site {
	GN001("GN01", "https://ccei.creativekorea.or.kr/gyeongnam/allim/allim_list.do?div_code=1"),
	GN002("GN02", "https://www.gntp.or.kr/biz/infoList"),
	GN003("GN03", "https://www.mss.go.kr/site/gyeongnam/ex/bbs/List.do?cbIdx=292"),
	GN004("GN04", "https://www.gnsinbo.or.kr/bbs/board.php?bo_table=1_2_3"),
	GN005("GN05", "https://www2.ripc.org/regional/notice/changwon/bizNoticeList.do");
	
	
	private final String code;
    private final String value;

    Site(String code, String value){
    	this.code = code;
    	this.value = value; 
	}

    public String getValue() { return value; }
    public String getCode() { return code; }
}
