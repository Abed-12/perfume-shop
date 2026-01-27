package com.abed.perfumeshop.admin.controller;

import com.abed.perfumeshop.Item.dto.response.AdminPerfumeCardDTO;
import com.abed.perfumeshop.Item.dto.request.CreatePerfumeRequest;
import com.abed.perfumeshop.Item.dto.request.UpdatePerfumeRequest;
import com.abed.perfumeshop.admin.service.AdminPerfumeService;
import com.abed.perfumeshop.common.dto.response.PageResponse;
import com.abed.perfumeshop.common.enums.PerfumeSeason;
import com.abed.perfumeshop.common.enums.PerfumeType;
import com.abed.perfumeshop.common.res.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/admin/perfumes")
@RequiredArgsConstructor
public class AdminPerfumeController {

    private final AdminPerfumeService adminPerfumeService;

    @GetMapping
    public ResponseEntity<Response<PageResponse<AdminPerfumeCardDTO>>> getAllPerfumes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) PerfumeType perfumeType,
            @RequestParam(required = false) PerfumeSeason perfumeSeason
    ) {
        PageResponse<AdminPerfumeCardDTO> pageResponse = adminPerfumeService.getAllPerfumes(page, size, perfumeType, perfumeSeason);

        return ResponseEntity.ok(
                Response.<PageResponse<AdminPerfumeCardDTO>>builder()
                        .statusCode(HttpStatus.OK.value())
                        .message("perfumes.retrieved")
                        .data(pageResponse)
                        .build()
        );
    }

    @GetMapping("/search")
    public ResponseEntity<Response<PageResponse<AdminPerfumeCardDTO>>> searchPerfumes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam String keyword
    ){
        PageResponse<AdminPerfumeCardDTO> pageResponse = adminPerfumeService.searchPerfumes(page, size, keyword);

        return ResponseEntity.ok(
                Response.<PageResponse<AdminPerfumeCardDTO>>builder()
                        .statusCode(HttpStatus.OK.value())
                        .message("perfumes.search.retrieved")
                        .data(pageResponse)
                        .build()
        );
    }

    @PostMapping
    public ResponseEntity<Response<Void>> createPerfume(
            @RequestPart @Valid CreatePerfumeRequest createPerfumeRequest,
            @RequestPart List<MultipartFile> images
    ) {
        adminPerfumeService.createPerfume(createPerfumeRequest, images);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        Response.<Void>builder()
                                .statusCode(HttpStatus.CREATED.value())
                                .message("perfume.created")
                                .build()
                );
    }

    @PutMapping("/{id}")
    public ResponseEntity<Response<Void>> updatePerfume(
            @PathVariable Long id,
            @RequestBody @Valid UpdatePerfumeRequest updatePerfumeRequest
    ) {
        adminPerfumeService.updatePerfume(id, updatePerfumeRequest);

        return ResponseEntity.ok(
                Response.<Void>builder()
                        .statusCode(HttpStatus.OK.value())
                        .message("perfume.updated")
                        .build()
        );
    }

    @PostMapping("/{perfumeId}/images")
    public ResponseEntity<Response<Void>> addPerfumeImage(
            @PathVariable Long perfumeId,
            @RequestPart MultipartFile image,
            @RequestParam(defaultValue = "false") Boolean isPrimary
    ) {
        adminPerfumeService.addPerfumeImage(perfumeId, image, isPrimary);

        return ResponseEntity.ok(
                Response.<Void>builder()
                        .statusCode(HttpStatus.OK.value())
                        .message("image.added.successfully")
                        .build()
        );
    }

    @PutMapping("/{perfumeId}/images/{imageId}")
    public ResponseEntity<Response<Void>> updatePerfumeImage(
            @PathVariable Long perfumeId,
            @PathVariable Long imageId,
            @RequestPart MultipartFile image
    ){
        adminPerfumeService.updatePerfumeImage(perfumeId, imageId, image);

        return ResponseEntity.ok(
                Response.<Void>builder()
                        .statusCode(HttpStatus.OK.value())
                        .message("image.updated.successfully")
                        .build()
        );
    }

    @DeleteMapping("/{perfumeId}/images/{imageId}")
    public ResponseEntity<Response<Void>> deletePerfumeImage(@PathVariable Long perfumeId, @PathVariable Long imageId){
        adminPerfumeService.deletePerfumeImage(perfumeId, imageId);

        return ResponseEntity.ok(
                Response.<Void>builder()
                        .statusCode(HttpStatus.OK.value())
                        .message("image.deleted.successfully")
                        .build()
        );
    }

}
