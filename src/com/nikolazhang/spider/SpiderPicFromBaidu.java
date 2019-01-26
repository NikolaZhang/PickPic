package com.nikolazhang.spider;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;

import com.nikolazhang.util.FileDownload;
import com.nikolazhang.util.HttpRequestUtil;


public class SpiderPicFromBaidu {
	private static int cnt = 0;
	
	public static void main(String[] args) throws IOException {
		Scanner scanner = new Scanner(System.in);
		System.out.println("请输入参数[参数之间使用一个空格分割] , 回车执行!");		
		String[] params = scanner.nextLine().split(" ");
		String filepath = params[0];
		String keywords = params[1];
		int count = Integer.valueOf(params[2]);
		
		String url = inputBaiduImageUrl(keywords);
		WebDriver connWeb = connWeb(url);
		Iterator<WebElement> imageUrl = null;
		for(int i = 0; i<count/25; i++) {
			((JavascriptExecutor) connWeb).executeScript("window.scrollBy(0, document.body.scrollHeight)");;
		}
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		try {
			imageUrl = getImageUrl(connWeb);
			// 获取完当前页面图片后下滚
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if(imageUrl != null) {
			downloadImage(imageUrl, filepath);
		} else {
			System.out.println("图片下载失败!!!");
		}
		
	}
	
	/**
	 * 链接目标网站
	 * @param url
	 * @return
	 */
	public static WebDriver connWeb(String url) {
		System.setProperty("webdriver.chrome.driver", 
				"chromedriver.exe");
		WebDriver webDriver = new ChromeDriver();

		webDriver.get(url);
		System.out.println("+*+*+*+* 已连接网站: 【"+webDriver.getTitle()+"】");
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return webDriver;
	}
	
	/**
	 * 输入关键词
	 */
	public static String inputBaiduImageUrl(String text) {
		String url = "https://image.baidu.com"
				+ "/search/index"
				+ "?tn=baiduimage"
				+ "&word="+text;
		return url;
	}
	
	public static Iterator<WebElement> getImageUrl(WebDriver webDriver) throws InterruptedException {
		Actions actions = new Actions(webDriver);
		List<WebElement> imgitem = webDriver.findElements(By.className("imgitem"));
		System.out.println("获取到的相关的图片数量: "+imgitem.size());
		Iterator<WebElement> imgItor = imgitem.iterator();
		return imgItor;
	}
	
	public static void downloadImage(Iterator<WebElement> imgItor, String filepath) {
		File file = new File(filepath);
		if(!file.exists()) {
			file.mkdirs();
		}
		while(imgItor.hasNext()) {
			WebElement nextImg = imgItor.next();
			String addrImg = nextImg.getAttribute("data-objurl");
			InputStream requestIO = HttpRequestUtil.httpRequestIO(addrImg);
			try {
				if (requestIO == null || requestIO.available() == 0) {
					System.out.println("原图链接不能下载， 下载压缩后图片。");
					addrImg = nextImg.getAttribute("data-thumburl");
					requestIO = HttpRequestUtil.httpRequestIO(addrImg);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			String filename =  ++cnt + "." + nextImg.getAttribute("data-ext");
			FileDownload.saveImageToDisk(requestIO, filepath+filename);
			try {
				if(requestIO != null)
					requestIO.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("下载第"+ cnt +"张图片地址为: "+addrImg);
		}
	}
}
