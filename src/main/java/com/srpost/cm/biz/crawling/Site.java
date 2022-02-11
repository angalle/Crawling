package com.srpost.cm.biz.crawling;

public enum Site {
	GN001("CJHS", "https://ccei.creativekorea.or.kr/gyeongnam/allim/allim_list.do?div_code=1", ""),
	GN002("TCNP", "https://www.gntp.or.kr/biz/infoList", "https://www.gntp.or.kr/biz/bizInfo"),
	GN003("JSVT", "https://www.mss.go.kr/site/gyeongnam/ex/bbs/List.do?cbIdx=292", ""),
	GN004("SNBO", "https://www.gnsinbo.or.kr/bbs/board.php?bo_table=1_2_3", ""),
	GN005("JSJS", "https://www2.ripc.org/regional/notice/changwon/bizNoticeList.do", "https://www2.ripc.org/regional/notice/changwon/bizNoticeDetail.do");
	
	
	private final String code;
    private final String value;
    private final String detail;

    Site(String code, String value, String detail){
    	this.code = code;
    	this.value = value;
    	this.detail = detail;
	}

    public String getValue() { return value; }
    public String getCode() { return code; }
	public String getDetail() {	return detail; }
}
