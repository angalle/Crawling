# Crawling

### 개발환경
```
Mysql 5.7 char set - utf8mb4
JAVA 1.8
```

## 1. create table by db
 - execute sql file in *initDDL Folder*
 
---
### Chcking files 
1. src 폴더는 com.srpost.cm.biz.crawling와 같은 구조를 가진다.
2. Service, DAO, VO 패키지 폴더 안에 존재
3. 리소스 폴더안에 mybatis-config.xml, mappers/* 파일들이 존재함.
4. 프로젝트 루크 경로에 chromedriver 가 존재함. 해당 파일이 경로에 맞게 실행될 수 있도록 지정해야함.


pom.xml 

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
