package com.atguigu.gmall.managerweb;

import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallManagerWebApplicationTests {

	@Test
	public void contextLoads() {
	}
	@Test
	public void testFds() throws IOException, MyException {
		String file = this.getClass().getResource("/tracker.conf").getFile();
		ClientGlobal.init(file);
		TrackerClient trackerClient = new TrackerClient();
		TrackerServer trackerServer = trackerClient.getConnection();
		StorageClient storageClient = new StorageClient(trackerServer,null);
		String orginalFilename="E:\\javaziliao\\zhongchou\\05_尚硅谷JAVAEE技术_众筹项目_20190401\\课件\\尚筹网页面原型\\img\\services-box1.jpg";
		String[] jpgs = storageClient.upload_file(orginalFilename, "jpg", null);
		for (int i = 0; i < jpgs.length; i++) {
			String jpg = jpgs[i];
			System.out.println("jpg="+jpg);
		}

	}
}
