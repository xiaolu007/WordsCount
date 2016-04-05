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
第一个版本如下所示：
```Java
package com.test.WordCount;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class WordCount 
{
	private static String file_path = "D:\\test.txt";
	private static HashMap<String, Integer> result = new HashMap<String, Integer>();
	
    public static void main( String[] args )
    {
    	File file = new File(file_path);
    	String current_line = null;
    	String[] current_words = null;
    	BufferedReader br = null;
    	try 
    	{
    		br = new BufferedReader(new FileReader(file.getPath()));
			while((current_line = br.readLine()) != null)
			{
				current_words = current_line.split("[^a-zA-Z']+");
				for(int i=0; i<current_words.length; i++)
				{		

					if(current_words[i].equals(""))
						continue;			
					
				    if (result.get(current_words[i].toLowerCase()) == null)
	                {
				    	result.put(current_words[i].toLowerCase(), 1);
	                }
				    else
	                {
	                	result.put(current_words[i].toLowerCase(), result.get(current_words[i])+1);
	                }
	             				
					
				}
			}
    	}
    	catch (IOException e) 
    	{
            e.printStackTrace();
        }
    	
    	for (HashMap.Entry<String, Integer> entry : result.entrySet()) 
    	{
    		System.out.println( entry.getKey() + " : " + entry.getValue());
    	}
    	
    	try 
    	{
			br.close();
		} 
    	catch (IOException e) 
    	{
			e.printStackTrace();
		}
    }
}
```
这个算法思路如下：    

1.定义一个BufferedReader对象，读取文本文件内容  

2.逐行读取文本内容，使用spilt()方法分割字符串（正则表达式"[^a-zA-Z']+",除英文字母和'其他字符均为分隔符）   
 
3.对分割后的字符串数组进行数目统计    

4.输出单词和对应个数


