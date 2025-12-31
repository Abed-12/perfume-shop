package com.abed.perfumeshop.admin.controller;

import com.abed.perfumeshop.admin.dto.AdminDTO;
import com.abed.perfumeshop.admin.dto.AdminUpdateRequest;
import com.abed.perfumeshop.admin.service.AdminProfileService;
import com.abed.perfumeshop.common.dto.UpdatePasswordRequest;
import com.abed.perfumeshop.common.res.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/profile")
@RequiredArgsConstructor
public class AdminProfileController {

    private final AdminProfileService adminProfileService;

    @GetMapping
    public ResponseEntity<Response<AdminDTO>> getMyProfile(){
        return ResponseEntity.ok(adminProfileService.getMyProfile());
    }

    @PutMapping
    public ResponseEntity<Response<?>> updateMyProfile(@RequestBody @Valid AdminUpdateRequest adminUpdateRequest){
        return ResponseEntity.ok(adminProfileService.updateMyProfile(adminUpdateRequest));
    }

    @PutMapping("/update-password")
    public ResponseEntity<Response<?>> updatePassword(@RequestBody @Valid UpdatePasswordRequest updatePasswordRequest){
        return ResponseEntity.ok(adminProfileService.updatePassword(updatePasswordRequest));
    }

}
