package com.nikolazhang.util;

import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CountDownLatch;

/**
 * ���߳�����ģ��
 *
 * @author bridge
 */
public class MutiThreadDownLoad {
	/**
	 * ͬʱ���ص��߳���
	 */
	private int threadCount;
	/**
	 * ����������·��
	 */
	private String serverPath;
	/**
	 * ����·��
	 */
	private String localPath;
	/**
	 * �̼߳���ͬ������
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
			// ���������ص����ݵĳ��ȣ�ʵ���Ͼ����ļ��ĳ���,��λ���ֽ�
			int length = conn.getContentLength();
			int code = conn.getResponseCode();
			System.out.println("�ļ��ܳ���:" + length + "�ֽ�(B)/״̬��: "+conn.getResponseCode());
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
			// ָ���������ļ��ĳ���
			raf.setLength(length);
			raf.close();
			// �ָ��ļ�
			int blockSize = length / threadCount;
			for (int threadId = 1; threadId <= threadCount; threadId++) {
				// ��һ���߳����صĿ�ʼλ��
				int startIndex = (threadId - 1) * blockSize;
				int endIndex = startIndex + blockSize - 1;
				if (threadId == threadCount) {
					// ���һ���߳����صĳ�����΢��һ��
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
	 * �ڲ�������ʵ������
	 */
	public class DownLoadThread extends Thread {
		/**
		 * �߳�ID
		 */
		private int threadId;
		/**
		 * ������ʼλ��
		 */
		private int startIndex;
		/**
		 * ���ؽ���λ��
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
				System.out.println("�߳�" + threadId + "��������...");
				URL url = new URL(serverPath);
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setRequestMethod("GET");
				// ������������ز��ֵ��ļ���ָ��λ��
				conn.setRequestProperty("Range", "bytes=" + startIndex + "-" + endIndex);
				conn.setConnectTimeout(60000);
				int code = conn.getResponseCode();
				System.out.println("�߳�" + threadId + "���󷵻�code=" + code);

				InputStream is = conn.getInputStream();// ������Դ
				RandomAccessFile raf = new RandomAccessFile(localPath, "rwd");
				// ���д�ļ���ʱ����ĸ�λ�ÿ�ʼд
				raf.seek(startIndex);// ��λ�ļ�

				int len = 0;
				byte[] buffer = new byte[1024];
				while ((len = is.read(buffer)) != -1) {
					raf.write(buffer, 0, len);
				}
				is.close();
				raf.close();
				System.out.println("�߳�" + threadId + "�������");
				// ����ֵ��һ
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
