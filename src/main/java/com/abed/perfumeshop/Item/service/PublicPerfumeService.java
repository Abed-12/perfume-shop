package com.abed.perfumeshop.Item.service;

import com.abed.perfumeshop.Item.dto.PerfumeCardDTO;
import com.abed.perfumeshop.Item.dto.PerfumeDetailDTO;
import com.abed.perfumeshop.Item.entity.PerfumeImage;
import com.abed.perfumeshop.common.dto.PageResponse;

public interface PublicPerfumeService {

    PageResponse<PerfumeCardDTO> getActivePerfumes(int page, int size);

    PerfumeDetailDTO getPerfumeById(Long id);

    PageResponse<PerfumeCardDTO> searchPerfumes(int page, int size, String keyword);

    PerfumeImage getImageById(Long perfumeId, Long imageId);

}
