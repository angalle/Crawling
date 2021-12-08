package com.srpost.cm.biz.crawling.vo;

public class CrawlingVO {

	private String id;
	private String site;
	private int board_no;
	private String board_type;
	private String title;
	private String writer;
	// default
	private String created_document;
	private Boolean isFile;
	private String documents;
	private String detail_url;
	private String site_url;
	private String status;
	// default
	private String created;
	

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getSite() {
		return site;
	}

	public void setSite(String site) {
		this.site = site;
	}

	public String getBoard_type() {
		return board_type;
	}

	public void setBoard_type(String board_type) {
		this.board_type = board_type;
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

	public String getCreated_document() {
		return created_document;
	}

	public void setCreated_document(String created_document) {
		created_document = created_document.replace(".", "-");
		this.created_document = created_document;
	}

	public Boolean getIsFile() {
		return isFile;
	}

	public void setIsFile(Boolean isFile) {
		this.isFile = isFile;
	}

	public String getDocuments() {
		return documents;
	}

	public void setDocuments(String documents) {
		this.documents = documents;
	}

	public String getDetail_url() {
		return detail_url;
	}

	public void setDetail_url(String detail_url) {
		this.detail_url = detail_url;
	}

	public String getSite_url() {
		return site_url;
	}

	public void setSite_url(String site_url) {
		this.site_url = site_url;
	}

	public String getCreated() {
		return created;
	}

	public void setCreated(String created) {
		this.created = created;
	}

	public int getBoard_no() {
		return board_no;
	}

	public void setBoard_no(int board_no) {
		this.board_no = board_no;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

}
