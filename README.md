-database: datawarehouse;
-user: root;
-password: "";
-tìm path của mysql-connector-j-9.1.0.jar
-Setting - Project Structure - Module - Dependencies - paste đường dẫn vào
-thêm ( <!-- MySQL Connector/J -->
<dependency>
<groupId>mysql</groupId>
<artifactId>mysql-connector-java</artifactId>
<version>8.0.28</version>
</dependency> ) vào file pom.xml nếu chưa có


