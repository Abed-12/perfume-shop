package com.abed.perfumeshop.Item.service;

import com.abed.perfumeshop.Item.dto.PerfumeCardDTO;
import com.abed.perfumeshop.Item.dto.PerfumeDetailDTO;
import com.abed.perfumeshop.Item.entity.PerfumeImage;
import com.abed.perfumeshop.common.dto.PageResponse;
import com.abed.perfumeshop.common.res.Response;

public interface PublicPerfumeService {

    Response<PageResponse<PerfumeCardDTO>> getActivePerfumes(int page, int size);

    Response<PerfumeDetailDTO> getPerfumeById(Long id);

    Response<PageResponse<PerfumeCardDTO>> searchPerfumes(String keyword, int page, int size);

    PerfumeImage getImageById(Long perfumeId, Long imageId);

}
