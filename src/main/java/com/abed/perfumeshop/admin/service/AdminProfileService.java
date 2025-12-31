package com.abed.perfumeshop.admin.service;

import com.abed.perfumeshop.admin.dto.AdminDTO;
import com.abed.perfumeshop.admin.dto.AdminUpdateRequest;
import com.abed.perfumeshop.common.dto.UpdatePasswordRequest;
import com.abed.perfumeshop.common.res.Response;

public interface AdminProfileService {

    Response<AdminDTO> getMyProfile();

    Response<?> updateMyProfile(AdminUpdateRequest adminUpdateRequest);

    Response<?> updatePassword(UpdatePasswordRequest updatePasswordRequest);

}
