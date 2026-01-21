package com.abed.perfumeshop.admin.service;

import com.abed.perfumeshop.admin.dto.response.AdminDTO;
import com.abed.perfumeshop.admin.dto.request.AdminUpdateRequest;
import com.abed.perfumeshop.common.dto.request.UpdatePasswordRequest;

public interface AdminProfileService {

    AdminDTO getMyProfile();

    void updateMyProfile(AdminUpdateRequest adminUpdateRequest);

    void updatePassword(UpdatePasswordRequest updatePasswordRequest);

}
