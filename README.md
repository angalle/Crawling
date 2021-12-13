# Crawling

### 개발환경
```
Mysql 5.7 char set - utf8mb4
JAVA 1.8
```

## 1. create table by db
 - *initDDL Folder* 안에 sql 파일을 실행한다.
 
---
### Chcking files 
1. src 폴더는 com.srpost.cm.biz.crawling와 같은 구조를 가진다.
2. Service, DAO, VO 패키지 폴더 안에 존재
3. 리소스 폴더안에 mybatis-config.xml, mappers/* 파일들이 존재함.
4. 프로젝트 루크 경로에 chromedriver 가 존재함. 해당 파일이 경로에 맞게 실행될 수 있도록 지정해야함.


---
## pom.xml 
### 꼭 추가해야 하는 파일 
```
<!-- https://mvnrepository.com/artifact/org.seleniumhq.selenium/selenium-java -->
<dependency>
    <groupId>org.seleniumhq.selenium</groupId>
    <artifactId>selenium-java</artifactId>
    <version>3.141.59</version>
</dependency>
```

### 작업시 추가했던 maven version
```
<!-- https://mvnrepository.com/artifact/mysql/mysql-connector-java -->
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <version>6.0.6</version>
</dependency>

<!-- https://mvnrepository.com/artifact/org.mybatis/mybatis -->
<dependency>
    <groupId>org.mybatis</groupId>
    <artifactId>mybatis</artifactId>
    <version>3.4.1</version>
</dependency>

<!-- https://mvnrepository.com/artifact/org.mybatis/mybatis-spring -->
<dependency>
    <groupId>org.mybatis</groupId>
    <artifactId>mybatis-spring</artifactId>
    <version>1.3.0</version>
</dependency>

 <!-- https://mvnrepository.com/artifact/org.springframework/spring-jdbc -->
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-jdbc</artifactId>
    <version>${org.springframework-version}</version>
</dependency>
```

---

# 작업 요청 사항 History

## GET / POST 방식 구분 
```
	GN001("GN01", "https://ccei.creativekorea.or.kr/gyeongnam/allim/allim_list.do?div_code=1"), *GET*
	GN002("GN02", "https://www.gntp.or.kr/biz/infoList"),                                       *POST*
	GN003("GN03", "https://www.mss.go.kr/site/gyeongnam/ex/bbs/List.do?cbIdx=292"),             *GET*
	GN004("GN04", "https://www.gnsinbo.or.kr/bbs/board.php?bo_table=1_2_3"),                    *GET*
	GN005("GN05", "https://www2.ripc.org/regional/notice/changwon/bizNoticeList.do");           *POST*
 ```
 
 
 DB snake case 적용 -
 java camel case 적용 -
