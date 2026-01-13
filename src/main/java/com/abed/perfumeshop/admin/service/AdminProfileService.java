package com.abed.perfumeshop.admin.service;

import com.abed.perfumeshop.admin.dto.AdminDTO;
import com.abed.perfumeshop.admin.dto.AdminUpdateRequest;
import com.abed.perfumeshop.common.dto.UpdatePasswordRequest;

public interface AdminProfileService {

    AdminDTO getMyProfile();

    void updateMyProfile(AdminUpdateRequest adminUpdateRequest);

    void updatePassword(UpdatePasswordRequest updatePasswordRequest);

}
