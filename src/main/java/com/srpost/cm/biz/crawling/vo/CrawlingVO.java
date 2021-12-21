package com.srpost.cm.biz.crawling.vo;

public class CrawlingVO {

	private String crawlingSeq;
	private int docSeq;
	private String docYear;
	private String siteType;
	private String docType;
	private String title;
	private String writer;
	private String docRegDt;
	private Boolean fileYn = false;
	private String contents;
	private String detailUrl;
	private String formParams;
	private String successYn = "N";
	private String regDt;
	
	public String getDocYear() {
		return docYear;
	}

	public void setDocYear(String docYear) {
		this.docYear = docYear;
	}

	public String getCrawlingSeq() {
		return crawlingSeq;
	}

	public void setCrawlingSeq(String crawlingSeq) {
		this.crawlingSeq = crawlingSeq;
	}

	public int getDocSeq() {
		return docSeq;
	}

	public void setDocSeq(int docSeq) {
		this.docSeq = docSeq;
	}

	public String getSiteType() {
		return siteType;
	}

	public void setSiteType(String siteType) {
		this.siteType = siteType;
	}

	public String getDocType() {
		return docType;
	}

	public void setDocType(String docType) {
		this.docType = docType;
	}

	public String getDocRegDt() {
		return docRegDt;
	}

	public void setDocRegDt(String docRegDt) {
		this.docRegDt = null;
		if(docRegDt != null) {
			docRegDt = docRegDt.replace(".", "-");
			this.docRegDt = docRegDt;
		}				
	}

	public Boolean getFileYn() {
		return fileYn;
	}

	public void setFileYn(Boolean fileYn) {
		this.fileYn = fileYn;
	}

	public String getContents() {
		return contents;
	}

	public void setContents(String contents) {
		this.contents = contents;
	}

	public String getDetailUrl() {
		return detailUrl;
	}

	public void setDetailUrl(String detailUrl) {
		this.detailUrl = detailUrl;
	}

	public String getSuccessYn() {
		return successYn;
	}

	public void setSuccessYn(String successYn) {
		this.successYn = successYn;
	}

	public String getRegDt() {
		return regDt;
	}

	public void setRegDt(String regDt) {
		this.regDt = regDt;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getWriter() {
		return writer;
	}

	public void setWriter(String writer) {
		this.writer = writer;
	}

	public String getFormParams() {
		return formParams;
	}

	public void setFormParams(String formParams) {
		this.formParams = formParams;
	}

}
