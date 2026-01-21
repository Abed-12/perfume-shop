package com.abed.perfumeshop.admin.controller;

import com.abed.perfumeshop.admin.dto.response.AdminDTO;
import com.abed.perfumeshop.admin.dto.request.AdminUpdateRequest;
import com.abed.perfumeshop.admin.service.AdminProfileService;
import com.abed.perfumeshop.common.dto.request.UpdatePasswordRequest;
import com.abed.perfumeshop.common.res.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/profile")
@RequiredArgsConstructor
public class AdminProfileController {

    private final AdminProfileService adminProfileService;

    @GetMapping
    public ResponseEntity<Response<AdminDTO>> getMyProfile(){
        AdminDTO adminDTO = adminProfileService.getMyProfile();

        return ResponseEntity.ok(
                Response.<AdminDTO>builder()
                        .statusCode(HttpStatus.OK.value())
                        .message("user.retrieved")
                        .data(adminDTO)
                        .build()
        );
    }

    @PutMapping
    public ResponseEntity<Response<Void>> updateMyProfile(@RequestBody @Valid AdminUpdateRequest adminUpdateRequest){
        adminProfileService.updateMyProfile(adminUpdateRequest);

        return ResponseEntity.ok(
                Response.<Void>builder()
                        .statusCode(HttpStatus.OK.value())
                        .message("user.profile.updated")
                        .build()
        );
    }

    @PutMapping("/update-password")
    public ResponseEntity<Response<Void>> updatePassword(@RequestBody @Valid UpdatePasswordRequest updatePasswordRequest){
        adminProfileService.updatePassword(updatePasswordRequest);

        return ResponseEntity.ok(
                Response.<Void>builder()
                        .statusCode(HttpStatus.OK.value())
                        .message("auth.password.changed.success")
                        .build()
        );
    }

}
