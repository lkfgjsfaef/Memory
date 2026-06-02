package com.niit.memory.controller;

import com.niit.memory.config.QiniuConfig;
import com.niit.memory.config.Result;
import com.qiniu.common.QiniuException;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/qiniu")
public class QiniuController {

    private static final Logger log = LoggerFactory.getLogger(QiniuController.class);

    private final QiniuConfig qiniuConfig;

    public QiniuController(QiniuConfig qiniuConfig) {
        this.qiniuConfig = qiniuConfig;
    }

    @DeleteMapping("/delete-by-url")
    public Result deleteByUrl(@RequestParam String url) {
        try {
            String domain = qiniuConfig.getDomain();
            if (!url.startsWith(domain + "/")) {
                return Result.error("URL 不属于当前存储空间");
            }
            String key = url.substring((domain + "/").length());
            Auth auth = Auth.create(qiniuConfig.getAccessKey(), qiniuConfig.getSecretKey());
            BucketManager bucketManager = new BucketManager(auth, new Configuration());
            bucketManager.delete(qiniuConfig.getBucket(), key);
            log.info("Deleted file from Qiniu: bucket={}, key={}", qiniuConfig.getBucket(), key);
            return Result.success();
        } catch (QiniuException e) {
            log.error("Qiniu delete failed: {}", e.getMessage());
            return Result.error("删除文件失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("Delete error: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/upload-token")
    public Result getUploadToken() {
        try {
            Auth auth = Auth.create(qiniuConfig.getAccessKey(), qiniuConfig.getSecretKey());
            StringMap putPolicy = new StringMap();
            putPolicy.put("returnBody",
                    "{\"key\":\"$(key)\",\"hash\":\"$(etag)\",\"url\":\""
                            + qiniuConfig.getDomain() + "/$(key)\"}");
            String token = auth.uploadToken(qiniuConfig.getBucket(), null, 3600, putPolicy);
            Map<String, String> data = new HashMap<>();
            data.put("token", token);
            data.put("domain", qiniuConfig.getDomain());
            return Result.success(data);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
