package com.abed.perfumeshop.Item.controller;

import com.abed.perfumeshop.Item.dto.PerfumeCardDTO;
import com.abed.perfumeshop.Item.dto.PerfumeDetailDTO;
import com.abed.perfumeshop.Item.entity.PerfumeImage;
import com.abed.perfumeshop.Item.service.PublicPerfumeService;
import com.abed.perfumeshop.common.dto.PageResponse;
import com.abed.perfumeshop.common.res.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/public/perfumes")
@RequiredArgsConstructor
public class PublicPerfumeController {

    private final PublicPerfumeService publicPerfumeService;

    @GetMapping
    public ResponseEntity<Response<PageResponse<PerfumeCardDTO>>> getActivePerfumes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ){
        return ResponseEntity.ok(publicPerfumeService.getActivePerfumes(page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Response<PerfumeDetailDTO>> getPerfumeById(@PathVariable Long id){
        return ResponseEntity.ok(publicPerfumeService.getPerfumeById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<Response<PageResponse<PerfumeCardDTO>>> searchPerfumes(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ){
        return ResponseEntity.ok(publicPerfumeService.searchPerfumes(keyword, page, size));
    }

    @GetMapping("/{perfumeId}/images/{imageId}")
    public ResponseEntity<byte[]> getImageById(@PathVariable Long perfumeId, @PathVariable Long imageId){
        PerfumeImage imageData = publicPerfumeService.getImageById(perfumeId, imageId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(imageData.getMimeType()))
                .contentLength(imageData.getFileSize())
                .cacheControl(
                        CacheControl.maxAge(1, TimeUnit.HOURS)
                            .cachePublic()
                            .immutable()
                )
                .body(imageData.getImageData());
    }

}
