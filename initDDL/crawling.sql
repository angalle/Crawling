CREATE TABLE crawling.crawling_info (
	id INTEGER auto_increment NOT NULL,
	board_no INTEGER NULL,
	site CHAR(4) NOT NULL,
	board_type varchar(100) NULL,
	title varchar(250) NULL,
	writer varchar(250) NULL,
	created_document DATETIME NULL,
	isFile BOOL DEFAULT false NOT NULL,
	documents BLOB NULL,
	detail_url varchar(250) NULL,
	site_url varchar(250) NULL,
	status char(1) NULL,
	created DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
	CONSTRAINT NewTable_PK PRIMARY KEY (id)
)
ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_general_ci;