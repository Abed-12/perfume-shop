package com.abed.perfumeshop.admin.service;

import com.abed.perfumeshop.Item.dto.response.AdminPerfumeCardDTO;
import com.abed.perfumeshop.Item.dto.request.CreatePerfumeRequest;
import com.abed.perfumeshop.Item.dto.request.UpdatePerfumeRequest;
import com.abed.perfumeshop.common.dto.response.PageResponse;
import com.abed.perfumeshop.common.enums.PerfumeSeason;
import com.abed.perfumeshop.common.enums.PerfumeType;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AdminPerfumeService {

    PageResponse<AdminPerfumeCardDTO> getAllPerfumes(int page, int size, PerfumeType perfumeType, PerfumeSeason perfumeSeason);

    PageResponse<AdminPerfumeCardDTO> searchPerfumes(int page, int size, String keyword);

    void createPerfume(CreatePerfumeRequest createPerfumeRequest, List<MultipartFile> images);

    void updatePerfume(Long id, UpdatePerfumeRequest updatePerfumeRequest);

    void addPerfumeImage(Long perfumeId, MultipartFile image, Boolean isPrimary);

    void updatePerfumeImage(Long perfumeId, Long imageId, MultipartFile image);

    void deletePerfumeImage(Long perfumeId, Long imageId);

}
