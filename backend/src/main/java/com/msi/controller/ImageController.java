package com.msi.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;

@RestController
@RequestMapping("/api/images")
public class ImageController {
    private static final Logger logger = LoggerFactory.getLogger(ImageController.class);
    private final Path uploadDir;
    @Value("${aliyun.oss.endpoint:}")
    private String ossEndpoint;
    @Value("${aliyun.oss.bucket:}")
    private String ossBucket;
    @Value("${aliyun.oss.accessKeyId:}")
    private String ossAccessKeyId;
    @Value("${aliyun.oss.accessKeySecret:}")
    private String ossAccessKeySecret;
    @Value("${aliyun.oss.host:}")
    private String ossHost;

    public ImageController() {
        this.uploadDir = Paths.get(System.getProperty("user.dir")).resolve("uploads");
        try {
            Files.createDirectories(uploadDir);
        } catch (Exception e) {
            throw new RuntimeException("无法创建上传目录");
        }
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> upload(
        @RequestPart("file") MultipartFile file
    ) {
        try {
            if (file == null || file.isEmpty()) {
                throw new IllegalArgumentException("文件不能为空");
            }
            String original = file.getOriginalFilename();
            String ext = "";
            if (original != null && original.contains(".")) {
                ext = original.substring(original.lastIndexOf('.') + 1);
            }
            String name = UUID.randomUUID().toString().replace("-", "");
            String filename = ext.isEmpty() ? name : name + "." + ext;
            if (ossEndpoint == null || ossEndpoint.isEmpty() || ossBucket == null || ossBucket.isEmpty()
                || ossAccessKeyId == null || ossAccessKeyId.isEmpty() || ossAccessKeySecret == null || ossAccessKeySecret.isEmpty()) {
                throw new IllegalArgumentException("OSS配置不完整");
            }
            OSS ossClient = new OSSClientBuilder().build(ossEndpoint, ossAccessKeyId, ossAccessKeySecret);
            try {
                ossClient.putObject(ossBucket, filename, file.getInputStream());
            } finally {
                ossClient.shutdown();
            }
            Map<String, String> resp = new HashMap<>();
            resp.put("filename", filename);
            String url;
            if (ossHost != null && !ossHost.isEmpty()) {
                url = ossHost.endsWith("/") ? (ossHost + filename) : (ossHost + "/" + filename);
            } else {
                String endpointHost = ossEndpoint.replaceFirst("^https?://", "");
                url = "https://" + ossBucket + "." + endpointHost + "/" + filename;
            }
            resp.put("url", url);
            return ResponseEntity.ok(resp);
        } catch (IllegalArgumentException e) {
            logger.error("图片上传失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            logger.error("图片上传失败", e);
            return ResponseEntity.internalServerError().body(null);
        }
    }

    @GetMapping(value = "/{filename}")
    public ResponseEntity<Resource> download(@PathVariable String filename) {
        try {
            if (filename == null || filename.isEmpty()) {
                throw new IllegalArgumentException("文件名不能为空");
            }
            String url;
            if (ossHost != null && !ossHost.isEmpty()) {
                url = ossHost.endsWith("/") ? (ossHost + filename) : (ossHost + "/" + filename);
            } else {
                String endpointHost = ossEndpoint.replaceFirst("^https?://", "");
                url = "https://" + ossBucket + "." + endpointHost + "/" + filename;
            }
            return ResponseEntity.status(302)
                .header(HttpHeaders.LOCATION, url)
                .build();
        } catch (IllegalArgumentException e) {
            logger.error("图片下载失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("图片下载失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
