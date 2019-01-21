package com.nikolazhang.spider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Iterator;
import java.util.Scanner;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.nikolazhang.util.HttpRequestUtil;

public class SpiderPicFromBing {
	
	private final static String[] strs = {"src", "data-src"};
	
	public static void main(String[] args) throws IOException {
		Scanner scanner = new Scanner(System.in);
		System.out.println("请输入参数, 回车执行!");		
		String[] params = scanner.nextLine().split(" ");
		System.out.println("开始下载----------");
		System.out.println(params[0]);
		System.out.println(params[1]);
		System.out.println(params[2]);
		visitHtml(params);
	}
	
	/**
	 * 访问bing html界面, 获取界面http链接, 过滤, 下载图片
	 * @param params 输入关键词
	 * @param filepath 本地存放路径
	 */
	private static void visitHtml(String[] args) {
		int count = Integer.valueOf(args[2]);
		int start = 1;
		for(int i = 0; i<count; i++) {
			int res = downloadPic(args, start);
			if (res != -1) {
				start += res + 2;
				i+=res;
			} else {
				System.out.println("***********下载出错!程序退出!");
			}
		}
	}


	private static int downloadPic(String[] args, int start) {
		String filepath = args[0];
		String params =args[1];
		String url = "https://cn.bing.com/images/async?q="+params+"&first="+start+"&mmasync=1";
		Connection conn = Jsoup.connect(url);
		int i = 0;
		try {
			
			Document doc = conn.get();
			Elements imgTags = doc.getElementsByTag("img");
			Iterator<Element> elems = imgTags.iterator();

			File file = new File(filepath);
			if(!file.exists()) {
				file.mkdirs();
			}
			while(elems.hasNext()) {
				Element img = elems.next();
				String attr = getImgUrl(img);
				if(!"".equals(attr) && attr.startsWith("https")) {
					System.out.println("获取图片: "+attr);
					InputStream requestIO = HttpRequestUtil.httpRequestIO(attr);
					long date = new Date().getTime();
					String localpath = filepath + date+".png";
					saveImageToDisk(requestIO, localpath);
					i++;
				}
			}
			System.out.println("==== INFORMATION =========================");
			System.out.println("下载路径: " + url);
			System.out.println("存储路径: " + filepath);
			System.out.println("获取资源: " + params);
			System.out.println("获取图片数量: " + i);
			System.out.println("===== END ========================");
		} catch (IOException e) {
			e.printStackTrace();
			return -1;
		}
		return i;
	}

	private static String getImgUrl(Element img) {
		String attr = "";
		for(String str : strs) {
			attr = img.attr(str);
			if(attr != null && !"".equals(attr) && attr.indexOf("&")!=-1) {
				attr = attr.split("&")[0];
				return attr;
			}
		}
		return "";
	} 
	
	private static void saveImageToDisk(InputStream inputStream, String filepath) {
		byte[] data = new byte[1024];
		int len = 0;
		FileOutputStream fileOutputStream = null;
		try {
			fileOutputStream = new FileOutputStream(filepath);
			while ((len = inputStream.read(data)) != -1) {
				fileOutputStream.write(data, 0, len);
 
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
 
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (fileOutputStream != null) {
				try {
					fileOutputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
 
	}
	
//	界面过滤器
//	https://cn.bing.com/images/search?
//	&q=%e6%97%a5%e5%b8%b8+%e5%8a%a8%e6%bc%ab
//	&qft=+filterui:color2-FGcls_RED
//		+filterui:photo-clipart
//		+filterui:imagesize-wallpaper
//	&FORM=IRFLTR
	
	
}
