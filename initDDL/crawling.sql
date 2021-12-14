CREATE TABLE si_crawling (
   crawling_seq INTEGER auto_increment NOT NULL COMMENT '게시물 일련번호',		#id
   doc_seq INTEGER NULL COMMENT '원본사이트에서의 게시물 일련번호 없으면 null',		#board_no
   site_type CHAR(4) NOT NULL COMMENT '원본 사이트 구분 값',						#site
   doc_type varchar(100) NULL COMMENT '원본사이트에서의 게시물 구분 없으면 null`',	#board_type
   title varchar(250) NULL COMMENT '제목',									#title
   writer varchar(250) NULL COMMENT '작성자(담당부서)',							#writer
   doc_reg_dt DATETIME NULL COMMENT '원본사이트에서의 등록일',						#created_document
   file_yn char(1) DEFAULT false NOT NULL COMMENT '첨부파일 유무 Y:있음 N:없음`', #isFile
   contents LONGTEXT NULL COMMENT '본문',										#documents
   detail_url varchar(250) NULL COMMENT '상세보기 url',						#detail_url
   form_params varchar(250) NULL COMMENT 'post params',						
   success_yn char(1) NULL COMMENT '취득성공여부 Y:성공 N:실패',					#status
   reg_dt DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '등록일',		#created
   CONSTRAINT pk_si_crawling PRIMARY KEY (crawling_seq)
)


-- initalize version created by hs
--CREATE TABLE crawling.crawling_info (
--	id INTEGER auto_increment NOT NULL,
--	board_no INTEGER NULL,
--	site CHAR(4) NOT NULL,
--	board_type varchar(100) NULL,
--	title varchar(250) NULL,
--	writer varchar(250) NULL,
--	created_document DATETIME NULL,
--	isFile BOOL DEFAULT false NOT NULL,
--	documents LONGTEXT NULL,
--	detail_url varchar(250) NULL,
--	site_url varchar(250) NULL,
--	status char(1) NULL,
--	created DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
--	CONSTRAINT NewTable_PK PRIMARY KEY (id)
--)
--ENGINE=InnoDB
--DEFAULT CHARSET=utf8mb4
--COLLATE=utf8mb4_general_ci;


-- create by client
--CREATE TABLE si_crawling (
--   crawling_seq INTEGER auto_increment NOT NULL COMMENT '게시물 일련번호',		//id
--   doc_seq INTEGER NULL COMMENT '원본사이트에서의 게시물 일련번호 없으면 null',		//board_no
--   site_type CHAR(4) NOT NULL COMMENT '원본 사이트 구분 값',						//site
--   doc_type varchar(100) NULL COMMENT '원본사이트에서의 게시물 구분 없으면 null`',	//board_type
--   title varchar(250) NULL COMMENT '제목',									//title
--   writer varchar(250) NULL COMMENT '작성자(담당부서)',							//writer
--   doc_reg_dt DATETIME NULL COMMENT '원본사이트에서의 등록일',						//created_document
--   file_yn char(1) DEFAULT false NOT NULL COMMENT '첨부파일 유무 Y:있음 N:없음`', //isFile
--   contents LONGTEXT NULL COMMENT '본문',										//documents
--   detail_url varchar(250) NULL COMMENT '상세보기 url',						//detail_url
--   site_url varchar(250) NULL COMMENT '? 목록보기 url ?',						//site_url
--   success_yn char(1) NULL COMMENT '취득성공여부 Y:성공 N:실패',					//status
--   reg_dt DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '등록일',		//created
--   CONSTRAINT pk_si_crawling PRIMARY KEY (crawling_seq)
--)



