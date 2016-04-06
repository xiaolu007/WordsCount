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
####第一个版本如下所示：(WordCount.java)
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
 
3.对分割后的字符串数组进行数目统计，将结果存在HashMap中    

4.输出单词和对应个数

测试的结果如下：  

test数据："The" you're the (sap ) ! |.   

结果：   
the : 2  
sap : 1   
you're : 1  

该算法能对文本中的单词进行准确计数，排除其他符号和大小写干扰，基本实现功能。
但是在大容量文件中执行速度会很慢，于是考虑多线程处理文件的方式。

###version 2.0
在上一版本的基础上添加大文件的产生和多线程处理
####大文件产生(WordsCount.java)
大于1G的英文文档文件很难下载到，即使下载到也无法正确知道里面英文单词的数量，因此考虑自己生成一个大文件，可清楚知道里面各个单词的数量。
```Java
	File file = new File("1.txt");
	FileChannel fileChannel = new RandomAccessFile(file, "rw").getChannel();
	FileLock lock = fileChannel.lock(0, file.length(), false);
	MappedByteBuffer mbBuf = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, file.length());
	String str = Charset.forName("UTF-8").decode(mbBuf).toString() + "\r\n";
	File file2 = new File("2.txt");
	if (file2.exists())
	{
		file2.delete();
	}	
        FileOutputStream outputFileStream = new FileOutputStream(file2 ,true);
        for (int i = 0; i < 1000; i++)
        {
        	outputFileStream.write(str.getBytes("UTF-8"));
        }      
        outputFileStream.close();
        lock.release();
        fileChannel.close();
```
将1.txt（1849KB）复制1000次可得2.txt（约1.8G），并且明确知道每个单词的出现频率，满足输入数据要求。
####多线程处理
#####分成多个子线程统计每个英文单词出现的次数(DealFileText.java)
```Java
	for (int num = 0; num < threadNum; num++)
	{
	    	if (currentPos < file.length())
	    	{
	    		CountWordsThread countWordsThread = null;
	    		if (currentPos + splitSize < file.length())
	    		{
	    			RandomAccessFile raf = new RandomAccessFile(file,"r");
	    			raf.seek(currentPos + splitSize);
	    			int offset = 0;
	    			while(true)
	    			{
	    				char ch = (char)raf.read();
	    				//是否到文件末尾，到了跳出
	    				if (-1 == ch)
	    					break;
	    				//是否是字母和'，都不是跳出（防止单词被截断）
	    				if(false == Character.isLetter(ch) && '\'' != ch)
	    					break;
	    				offset++;
	    			}
	    				
	    			countWordsThread = new CountWordsThread(file, currentPos, splitSize + offset);
	    			currentPos += splitSize + offset;
	    			raf.close();
	    			}
	    		else
	    		{
	    			countWordsThread = new CountWordsThread(file, currentPos, file.length() - currentPos);
	    			currentPos = file.length();
	    		}
	    			Thread thread = new Thread(countWordsThread);
	    			thread.start();
	    			listCountWordsThreads.add(countWordsThread);
	    			listThread.add(thread);
	    		}
	    	}
	    	//判断线程是否执行完成
	    	while(true) 
            {
            	boolean threadsDone = true;
            	
            	for (int loop = 0; loop < listThread.size(); loop++)
            	{
            		if (listThread.get(loop).getState() != Thread.State.TERMINATED)
            		{
            			threadsDone = false;
            			break;
            		}
            	}
            	if (true == threadsDone)
            		break;
            }
  ```
分割文件大小为splitSize，产生threadNum个子线程去统计每一个分割文件中单词出现次数。   

刚开始分割文件时未考虑单词被截断的状况，后面考虑到这种情况加入if(false == Character.isLetter(ch) && '\'' != ch)判断，若分割位置下一个byte是字母或‘，则不在此处分割继续往下找分割位置，防止单词截断。
#####子线程处理函数(CountWordsThread.java)
```Java
 //重写run()方法
    @Override
    public void run() 
    {
        String str = Charset.forName("UTF-8").decode(mbBuf).toString();
        str = str.toLowerCase();
        String[] strArray = str.split("[^a-zA-Z']+");
        for(int i = 0; i<strArray.length; i++)
	{		
		if(strArray[i].equals(""))
			continue;
		if (hashMap.get(strArray[i]) == null)
        	{
			hashMap.put(strArray[i], 1);
		}
		else
            	{
		  	hashMap.put(strArray[i], hashMap.get(strArray[i]) + 1);
            	}
	}
    }
     ```
子线程run()方法里对字符串分割并统计次数，如version1.0里处理方式相同。
#####统计总数目线程(DealFileText.java)
```Java
    //当分别统计的线程结束后，开始统计总数目的线程
    	Thread mainThread = new Thread() 
    	{
    	   //重写run()方法
        	 public void run() 
        	 {
                    // 使用TreeMap保证结果有序（按首字母排序）
                    TreeMap<String, Integer> tMap = new TreeMap<String, Integer>();
                	for (int loop = 0; loop < listCountWordsThreads.size(); loop++)
                	{
                		Map<String, Integer> hMap = listCountWordsThreads.get(loop).getResultMap();
                        	Set<String> keys = hMap.keySet();
                        	Iterator<String> iterator = keys.iterator();
                        	while (iterator.hasNext()) 
                	 	{
                            		String key = (String) iterator.next();
                            		if(key.equals(""))
        					continue;			
        				if (tMap.get(key) == null)
        	                	{
        					tMap.put(key, hMap.get(key));
        	        	 	}
        				else
        	                	{
        					tMap.put(key, tMap.get(key) + hMap.get(key));
        	                	}
                		}
                	}
                	for (int loop = 0; loop < listThread.size(); loop++)
                	{
                		listThread.get(loop).interrupt();
                	}
                	Set<String> keys = tMap.keySet();
                	Iterator<String> iterator = keys.iterator();
                    	while (iterator.hasNext()) 
                	{
                		 String key = (String) iterator.next();
                        	System.out.println("key:" + key + " value:" + tMap.get(key));
                    	}
                    return;
                }
        };
        ```
当所有统计单词子线程的程序执行完成后，开始此线程，将子线程的统计结果存入hashmap中。后来考虑到单词数目很多的情况下不方便查看，所以改为存在TreeMap中，按首字母（A~Z）的顺序输出单词和对应出现次数。   

经2.txt数据输入测试，能正确输出结果。

###version 2.1
上一版能对大文件进行处理，但是并未考虑几个线程，分割文件大小多大时，处理时间最短。
