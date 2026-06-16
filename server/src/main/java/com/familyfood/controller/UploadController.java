package com.familyfood.controller;

import com.familyfood.common.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/api")
public class UploadController {

    @Value("${app.upload-path:./uploads}")
    private String uploadPath;

    @PostMapping("/upload")
    public Result<?> upload(@RequestParam("file") MultipartFile file) throws IOException {
        File dir = new File(uploadPath);
        if (!dir.exists()) dir.mkdirs();

        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
        File dest = new File(dir, filename);
        file.transferTo(dest);

        Map<String, String> result = new HashMap<>();
        result.put("url", "/uploads/" + filename);
        return Result.ok(result);
    }
}
