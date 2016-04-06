package com.gh.WordsCount;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

//大文件处理类
public class DealFileText 
{
 
    // 要处理的文件
    private File file = null;
    
    // 线程数
    private int threadNum;
     
    // 线程表
    private Vector<CountWordsThread> listCountWordsThreads = null;
    private Vector<Thread> listThread = null;
    
    // 文件分割大小
    private long splitSize;
    
    // 当前处理的文件位置
    private long currentPos;
     
    public DealFileText(File file, int threadNum, long splitSize)// 构造函数：文件，线程数，文件分割大小
    {
    	//确定线程数最小是1个
    	if (threadNum < 1)
    		threadNum = 1;
    	//确定线程数最大是10个，防止内存不够用
    	if (threadNum > 10)
    		threadNum = 10;
    	//分割最小为1M大小文件
    	if (splitSize < 1024*1024)
    		splitSize = 1024*1024;
    	//分割最大为10M大小文件
    	if (splitSize > 1024 * 1024 * 10)
    		splitSize = 1024 * 1024 * 10;
    	
        this.file       = file;
        this.threadNum  = threadNum;
        this.splitSize  = splitSize;
        this.currentPos = 0;
        this.listCountWordsThreads = new Vector<CountWordsThread>();
        this.listThread = new Vector<Thread>();
    }
    
    public void doFile() throws IOException
    {
    	while (currentPos < this.file.length())
    	{
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
    	}
    	
    //当分别统计的线程结束后，开始统计总数目的线程
    	new Thread( () ->
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
                        System.out.println("单词:" + key + " 出现次数:" + tMap.get(key));
                    }       	
                    return;
    	}).start();
    }
}