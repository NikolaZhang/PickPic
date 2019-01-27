package com.nikolazhang.util;

import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CountDownLatch;

/**
 * 多线程下载模型
 *
 * @author bridge
 */
public class MutiThreadDownLoad {
	/**
	 * 同时下载的线程数
	 */
	private int threadCount;
	/**
	 * 服务器请求路径
	 */
	private String serverPath;
	/**
	 * 本地路径
	 */
	private String localPath;
	/**
	 * 线程计数同步辅助
	 */
	private CountDownLatch latch;

	public MutiThreadDownLoad(int threadCount, String serverPath, String localPath, CountDownLatch latch) {
		this.threadCount = threadCount;
		this.serverPath = serverPath;
		this.localPath = localPath;
		this.latch = latch;
	}

	public void executeDownLoad() {

		URL url = null;;
		HttpURLConnection conn = null;
		try {
			url = new URL(serverPath);
			conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(50000);
			conn.setRequestMethod("GET");
			conn.setReadTimeout(10000);
			// 服务器返回的数据的长度，实际上就是文件的长度,单位是字节
			int length = conn.getContentLength();
			int code = conn.getResponseCode();
			System.out.println("文件总长度:" + length + "字节(B)/状态码: "+conn.getResponseCode());
			if(code == 301) {
				url = new URL(
						serverPath.replaceAll("Http:", "https:")
							.replaceAll("http:", "https:")
							.replaceAll("HTTP:", "https:")
						);
				conn = (HttpURLConnection) url.openConnection();
				conn.setConnectTimeout(50000);
				conn.setRequestMethod("GET");
				conn.setReadTimeout(10000);
			}
			if(code == 403) {
				for(;latch.getCount()>0;latch.countDown());
				return;
			}
			RandomAccessFile raf = new RandomAccessFile(localPath, "rwd");
			// 指定创建的文件的长度
			raf.setLength(length);
			raf.close();
			// 分割文件
			int blockSize = length / threadCount;
			for (int threadId = 1; threadId <= threadCount; threadId++) {
				// 第一个线程下载的开始位置
				int startIndex = (threadId - 1) * blockSize;
				int endIndex = startIndex + blockSize - 1;
				if (threadId == threadCount) {
					// 最后一个线程下载的长度稍微长一点
					endIndex = length;
				}
				new DownLoadThread(threadId, startIndex, endIndex).start();
			}
		} catch (Exception e) {
			conn = null;
			url = null;
		}

	}

	/**
	 * 内部类用于实现下载
	 */
	public class DownLoadThread extends Thread {
		/**
		 * 线程ID
		 */
		private int threadId;
		/**
		 * 下载起始位置
		 */
		private int startIndex;
		/**
		 * 下载结束位置
		 */
		private int endIndex;

		public DownLoadThread(int threadId, int startIndex, int endIndex) {
			this.threadId = threadId;
			this.startIndex = startIndex;
			this.endIndex = endIndex;
		}

		@Override
		public void run() {

			try {
				System.out.println("线程" + threadId + "正在下载...");
				URL url = new URL(serverPath);
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setRequestMethod("GET");
				// 请求服务器下载部分的文件的指定位置
				conn.setRequestProperty("Range", "bytes=" + startIndex + "-" + endIndex);
				conn.setConnectTimeout(60000);
				int code = conn.getResponseCode();
				System.out.println("线程" + threadId + "请求返回code=" + code);

				InputStream is = conn.getInputStream();// 返回资源
				RandomAccessFile raf = new RandomAccessFile(localPath, "rwd");
				// 随机写文件的时候从哪个位置开始写
				raf.seek(startIndex);// 定位文件

				int len = 0;
				byte[] buffer = new byte[1024];
				while ((len = is.read(buffer)) != -1) {
					raf.write(buffer, 0, len);
				}
				is.close();
				raf.close();
				System.out.println("线程" + threadId + "下载完毕");
				// 计数值减一
				latch.countDown();

			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}

	public void downLoadLargeFile(String url, String filepath, int num) {
		CountDownLatch latch = new CountDownLatch(num);
		MutiThreadDownLoad m = new MutiThreadDownLoad(num, serverPath, localPath, latch);
		try {
			m.executeDownLoad();
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		CountDownLatch latch = new CountDownLatch(4);
		try {
			new MutiThreadDownLoad(4, "http://a-ssl.duitang.com/uploads/item/201508/21/20150821143824_TijVv.jpeg",
					"F:\\image.png", latch).executeDownLoad();
			latch.await();
			latch = null;
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
