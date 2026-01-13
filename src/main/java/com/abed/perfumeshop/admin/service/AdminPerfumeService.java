package com.abed.perfumeshop.admin.service;

import com.abed.perfumeshop.Item.dto.AdminPerfumeCardDTO;
import com.abed.perfumeshop.Item.dto.CreatePerfumeRequest;
import com.abed.perfumeshop.Item.dto.UpdatePerfumeRequest;
import com.abed.perfumeshop.common.dto.PageResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AdminPerfumeService {

    PageResponse<AdminPerfumeCardDTO> getAllPerfumes(int page, int size);

    void createPerfume(CreatePerfumeRequest createPerfumeRequest, List<MultipartFile> images, Integer primaryImageIndex, List<Integer> imageOrder);

    void updatePerfume(Long id, UpdatePerfumeRequest updatePerfumeRequest);

    void addPerfumeImage(Long perfumeId, MultipartFile image, Boolean isPrimary);

    void updatePerfumeImage(Long perfumeId, Long imageId, MultipartFile image);

    void deletePerfumeImage(Long perfumeId, Long imageId);

}
