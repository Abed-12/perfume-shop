package com.abed.perfumeshop.Item.service;

import com.abed.perfumeshop.Item.dto.response.PerfumeCardDTO;
import com.abed.perfumeshop.Item.dto.response.PerfumeDetailDTO;
import com.abed.perfumeshop.Item.entity.PerfumeImage;
import com.abed.perfumeshop.common.dto.response.PageResponse;
import com.abed.perfumeshop.common.enums.PerfumeSeason;
import com.abed.perfumeshop.common.enums.PerfumeType;

public interface PublicPerfumeService {

    PageResponse<PerfumeCardDTO> getActivePerfumes(int page, int size, PerfumeType perfumeType, PerfumeSeason perfumeSeason);

    PerfumeDetailDTO getPerfumeById(Long id);

    PageResponse<PerfumeCardDTO> searchPerfumes(int page, int size, String keyword);

    PerfumeImage getImageById(Long perfumeId, Long imageId);

}
