WordsCount
===================================
本项目为一个基于Maven 3.3.9  的mvn工程

实现功能
-----------------------------------
* 根据一个英文文档小文件生成大文件；   

* 查询大文件中中出现的不同单词；   

* 统计出这些单词出现的次数；   

* 按首字母A-Z顺序输出单词和对应出现次数。

调试环境
-----------------------------------
* JDK 1.8.0_77  

* Eclipse Mars.2 Release (4.5.2)   

* Maven 3.3.9  

mvn命令行运行
-----------------------------------
pom.xml中已设置mainclass为com.gh.WordsCount.WordsCount
在windows的cmd下：
可直接输入
```
mvn exec:java
```
或者在不设置mainclass时输入
```
mvn exec:java -Dexec.mainClass=com.gh.WordsCount.WordsCount
```
优化过程
-----------------------------------
###version 1.0
