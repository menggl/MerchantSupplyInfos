package com.msi.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletResponse;
import com.aliyun.oss.OSS;
import org.springframework.web.multipart.MultipartFile;
import net.coobird.thumbnailator.Thumbnails;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
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
    @Value("${image.host:}")
    private String imageHost;

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
        @RequestPart("file") MultipartFile file,
        @RequestParam(value = "type", defaultValue = "0") Integer type
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
            boolean ossConfigured = ossEndpoint != null && !ossEndpoint.isEmpty()
                && ossBucket != null && !ossBucket.isEmpty()
                && ossAccessKeyId != null && !ossAccessKeyId.isEmpty()
                && ossAccessKeySecret != null && !ossAccessKeySecret.isEmpty();

            InputStream inputStream = file.getInputStream();
            long currentSize = file.getSize();

            if (type != 1 && currentSize > 1024 * 1024) {
                try {
                    double scale = 1.0;
                    double quality = 0.8;
                    // Start with some reduction if huge
                    if (currentSize > 2 * 1024 * 1024) {
                        scale = 0.6;
                    } else {
                        scale = 0.8;
                    }

                    while (scale > 0.1) {
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        Thumbnails.of(file.getInputStream())
                                .scale(scale)
                                .outputQuality(quality)
                                .toOutputStream(bos);
                        
                        byte[] bytes = bos.toByteArray();
                        // If successfully compressed to < 800KB or if we reached the last attempt
                        if (bytes.length < 800 * 1024 || scale <= 0.2) {
                            inputStream = new ByteArrayInputStream(bytes);
                            currentSize = bytes.length;
                            logger.info("Compressed image to {} bytes (scale={})", currentSize, scale);
                            break;
                        }
                        
                        scale -= 0.1;
                    }
                } catch (Exception e) {
                    logger.error("Compression failed, using original file", e);
                    inputStream = file.getInputStream();
                }
            }

            Map<String, String> resp = new HashMap<>();
            resp.put("filename", filename);
            String url;
            if (ossConfigured) {
                OSS ossClient = new OSSClientBuilder().build(ossEndpoint, ossAccessKeyId, ossAccessKeySecret);
                try {
                    ossClient.putObject(ossBucket, filename, inputStream);
                } finally {
                    ossClient.shutdown();
                }
                if (imageHost != null && !imageHost.isEmpty()) {
                    String base = imageHost.endsWith("/") ? imageHost.substring(0, imageHost.length() - 1) : imageHost;
                    url = base + "/api/images/" + filename;
                } else {
                    url = "/api/images/" + filename;
                }
            } else {
                Path target = uploadDir.resolve(filename);
                Files.copy(inputStream, target);
                if (imageHost != null && !imageHost.isEmpty()) {
                    String base = imageHost.endsWith("/") ? imageHost.substring(0, imageHost.length() - 1) : imageHost;
                    url = base + "/api/images/" + filename;
                } else {
                    url = "/api/images/" + filename;
                }
            }
            resp.put("url", url);
            logger.info("图片上传成功: {}", url);
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
    public void download(@PathVariable String filename, HttpServletResponse response) {
        try {
            if (filename == null || filename.isEmpty()) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "文件名不能为空");
                return;
            }
            logger.info("图片下载请求: {}", filename);
            boolean ossConfigured = ossEndpoint != null && !ossEndpoint.isEmpty()
                && ossBucket != null && !ossBucket.isEmpty()
                && ossAccessKeyId != null && !ossAccessKeyId.isEmpty()
                && ossAccessKeySecret != null && !ossAccessKeySecret.isEmpty();

            if (ossConfigured) {
                OSS ossClient = new OSSClientBuilder().build(ossEndpoint, ossAccessKeyId, ossAccessKeySecret);
                try {
                    com.aliyun.oss.model.OSSObject ossObject = ossClient.getObject(ossBucket, filename);
                    
                    String contentType = "image/jpeg";
                    if (filename.toLowerCase().endsWith(".png")) {
                        contentType = "image/png";
                    } else if (filename.toLowerCase().endsWith(".gif")) {
                        contentType = "image/gif";
                    } else if (filename.toLowerCase().endsWith(".webp")) {
                        contentType = "image/webp";
                    }
                    
                    response.setContentType(contentType);
                    
                    try (java.io.InputStream inputStream = ossObject.getObjectContent();
                         java.io.OutputStream outputStream = response.getOutputStream()) {
                        byte[] buffer = new byte[8192];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }
                        outputStream.flush();
                    }
                } catch (Exception e) {
                    logger.error("OSS读取失败: {}", e.getMessage());
                    if (!response.isCommitted()) {
                        response.sendError(HttpServletResponse.SC_NOT_FOUND, "文件不存在或读取失败");
                    }
                } finally {
                    ossClient.shutdown();
                }
            } else {
                Path filePath = uploadDir.resolve(filename);
                if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "文件不存在");
                    return;
                }
                String contentType = Files.probeContentType(filePath);
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }
                response.setContentType(contentType);
                Files.copy(filePath, response.getOutputStream());
                response.flushBuffer();
            }
        } catch (Exception e) {
            logger.error("图片下载失败", e);
            if (!response.isCommitted()) {
                try {
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                } catch (java.io.IOException ex) {
                    // ignore
                }
            }
        }
    }
}
