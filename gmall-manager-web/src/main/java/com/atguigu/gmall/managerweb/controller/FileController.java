package com.atguigu.gmall.managerweb.controller;


import com.atguigu.gmall.entity.SpuInfo;
import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@CrossOrigin
public class FileController {

    @Value("${fileServer.url}")
    private String serviceUrl;
    @PostMapping("fileUpload")
    public String FileUpload(@RequestParam("file") MultipartFile file) throws IOException, MyException {
        String uploadfile = this.getClass().getResource("/tracker.conf").getFile();
        ClientGlobal.init(uploadfile);
        TrackerClient trackerClient = new TrackerClient();
        TrackerServer trackerServer = trackerClient.getConnection();
        StorageClient storageClient = new StorageClient(trackerServer,null);
        byte[] bytes = file.getBytes();
        String[] jpgs = storageClient.upload_file(bytes, org.apache.commons.lang3.StringUtils.substringAfter(file.getOriginalFilename(),"."), null);
        String url = serviceUrl;
        for (int i = 0; i < jpgs.length; i++) {
            String jpg = jpgs[i];
            url += "/"+jpg;
        }
        return url;
    }

}
