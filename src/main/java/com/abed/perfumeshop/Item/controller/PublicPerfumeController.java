package com.abed.perfumeshop.Item.controller;

import com.abed.perfumeshop.Item.dto.response.PerfumeCardDTO;
import com.abed.perfumeshop.Item.dto.response.PerfumeDetailDTO;
import com.abed.perfumeshop.Item.entity.PerfumeImage;
import com.abed.perfumeshop.Item.service.PublicPerfumeService;
import com.abed.perfumeshop.common.dto.response.PageResponse;
import com.abed.perfumeshop.common.enums.PerfumeSeason;
import com.abed.perfumeshop.common.enums.PerfumeType;
import com.abed.perfumeshop.common.res.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
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
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) PerfumeType perfumeType,
            @RequestParam(required = false) PerfumeSeason perfumeSeason
    ){
        PageResponse<PerfumeCardDTO> pageResponse = publicPerfumeService.getActivePerfumes(page, size, perfumeType, perfumeSeason);

        return ResponseEntity.ok(
                Response.<PageResponse<PerfumeCardDTO>>builder()
                        .statusCode(HttpStatus.OK.value())
                        .message("perfumes.retrieved")
                        .data(pageResponse)
                        .build()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Response<PerfumeDetailDTO>> getPerfumeById(@PathVariable Long id){
        PerfumeDetailDTO perfumeDetail = publicPerfumeService.getPerfumeById(id);

        return ResponseEntity.ok(
                Response.<PerfumeDetailDTO>builder()
                        .statusCode(HttpStatus.OK.value())
                        .message("perfume.retrieved")
                        .data(perfumeDetail)
                        .build()
        );
    }

    @GetMapping("/search")
    public ResponseEntity<Response<PageResponse<PerfumeCardDTO>>> searchPerfumes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam String keyword
    ){
        PageResponse<PerfumeCardDTO> pageResponse = publicPerfumeService.searchPerfumes(page, size, keyword);

        return ResponseEntity.ok(
                Response.<PageResponse<PerfumeCardDTO>>builder()
                        .statusCode(HttpStatus.OK.value())
                        .message("perfumes.search.retrieved")
                        .data(pageResponse)
                        .build()
        );
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
