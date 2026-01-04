package com.abed.perfumeshop.admin.service;

import com.abed.perfumeshop.Item.dto.AdminPerfumeCardDTO;
import com.abed.perfumeshop.Item.dto.CreatePerfumeRequest;
import com.abed.perfumeshop.Item.dto.UpdatePerfumeRequest;
import com.abed.perfumeshop.common.dto.PageResponse;
import com.abed.perfumeshop.common.res.Response;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AdminPerfumeService {

    Response<PageResponse<AdminPerfumeCardDTO>> getAllPerfumes(int page, int size);

    Response<?> createPerfume(CreatePerfumeRequest createPerfumeRequest, List<MultipartFile> images, Integer primaryImageIndex, List<Integer> imageOrder);

    Response<?> updatePerfume(Long id, UpdatePerfumeRequest updatePerfumeRequest);

    Response<?> addPerfumeImage(Long perfumeId, MultipartFile image, Boolean isPrimary);

    Response<?> updatePerfumeImage(Long perfumeId, Long imageId, MultipartFile image);

    Response<?> deletePerfumeImage(Long perfumeId, Long imageId);

}
