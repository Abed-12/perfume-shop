package com.abed.perfumeshop.admin.controller;

import com.abed.perfumeshop.Item.dto.CreatePerfumeRequest;
import com.abed.perfumeshop.Item.dto.UpdatePerfumeRequest;
import com.abed.perfumeshop.admin.service.AdminPerfumeService;
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

    @PostMapping
    public ResponseEntity<Response<?>> createPerfume(
            @RequestPart @Valid CreatePerfumeRequest createPerfumeRequest,
            @RequestPart List<MultipartFile> images,
            @RequestParam Integer primaryImageIndex,
            @RequestParam List<Integer> imageOrder
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(adminPerfumeService.createPerfume(createPerfumeRequest, images, primaryImageIndex, imageOrder));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Response<?>> updatePerfume(
            @PathVariable Long id,
            @RequestBody @Valid UpdatePerfumeRequest updatePerfumeRequest
    ) {
        return ResponseEntity.ok(adminPerfumeService.updatePerfume(id, updatePerfumeRequest));
    }

    @PostMapping("/{perfumeId}/images")
    public ResponseEntity<Response<?>> addPerfumeImage(
            @PathVariable Long perfumeId,
            @RequestPart MultipartFile image,
            @RequestParam(defaultValue = "false") Boolean isPrimary
    ) {
        return ResponseEntity.ok(adminPerfumeService.addPerfumeImage(perfumeId, image, isPrimary));
    }

    @PutMapping("/{perfumeId}/images/{imageId}")
    public ResponseEntity<Response<?>> updatePerfumeImage(
            @PathVariable Long perfumeId,
            @PathVariable Long imageId,
            @RequestPart MultipartFile image
    ){
        return ResponseEntity.ok(adminPerfumeService.updatePerfumeImage(perfumeId, imageId, image));
    }

    @DeleteMapping("/{perfumeId}/images/{imageId}")
    public ResponseEntity<Response<?>> deletePerfumeImage(@PathVariable Long perfumeId, @PathVariable Long imageId){
        return ResponseEntity.ok(adminPerfumeService.deletePerfumeImage(perfumeId, imageId));
    }

}
