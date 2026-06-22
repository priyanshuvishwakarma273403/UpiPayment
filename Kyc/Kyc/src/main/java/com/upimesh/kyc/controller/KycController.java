package com.upimesh.kyc.controller;

import com.upimesh.kyc.model.entity.KycAuditLog;
import com.upimesh.kyc.model.request.AadhaarOtpRequest;
import com.upimesh.kyc.model.request.AadhaarVerifyRequest;
import com.upimesh.kyc.model.request.PanVerifyRequest;
import com.upimesh.kyc.model.response.AadhaarOtpResponse;
import com.upimesh.kyc.model.response.ApiResponse;
import com.upimesh.kyc.model.response.KycStatusResponse;
import com.upimesh.kyc.service.KycService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/kyc")
@RequiredArgsConstructor
public class KycController {

    private final KycService kycService;

    @PostMapping("/initiate-aadhaar")
    public ResponseEntity<ApiResponse<AadhaarOtpResponse>> initiateAadhaar(
            @Valid @RequestBody AadhaarOtpRequest request,
            HttpServletRequest httpServletRequest) {
        
        String ipAddress = getIpAddress(httpServletRequest);
        String deviceId = httpServletRequest.getHeader("X-Device-Id");

        AadhaarOtpResponse response = kycService.initiateAadhaarOtp(request, ipAddress, deviceId);
        return ResponseEntity.ok(ApiResponse.success("Aadhaar verification OTP sent successfully", response));
    }

    @PostMapping("/verify-aadhaar")
    public ResponseEntity<ApiResponse<KycStatusResponse>> verifyAadhaar(
            @Valid @RequestBody AadhaarVerifyRequest request,
            HttpServletRequest httpServletRequest) {

        String ipAddress = getIpAddress(httpServletRequest);
        String deviceId = httpServletRequest.getHeader("X-Device-Id");

        KycStatusResponse response = kycService.verifyAadhaarOtp(request, ipAddress, deviceId);
        return ResponseEntity.ok(ApiResponse.success("Aadhaar OTP verified successfully", response));
    }

    @PostMapping("/verify-pan")
    public ResponseEntity<ApiResponse<KycStatusResponse>> verifyPan(
            @Valid @RequestBody PanVerifyRequest request,
            HttpServletRequest httpServletRequest) {

        String ipAddress = getIpAddress(httpServletRequest);
        String deviceId = httpServletRequest.getHeader("X-Device-Id");

        KycStatusResponse response = kycService.verifyPan(request, ipAddress, deviceId);
        return ResponseEntity.ok(ApiResponse.success("PAN verified successfully and KYC upgraded to LEVEL_1", response));
    }

    @PostMapping("/face-match")
    public ResponseEntity<ApiResponse<KycStatusResponse>> faceMatch(
            @Valid @RequestBody FaceMatchRequest request,
            HttpServletRequest httpServletRequest) {

        String ipAddress = getIpAddress(httpServletRequest);
        String deviceId = httpServletRequest.getHeader("X-Device-Id");

        KycStatusResponse response = kycService.performFaceMatch(request.getKycId(), request.getSelfieImageBase64(), ipAddress, deviceId);
        return ResponseEntity.ok(ApiResponse.success("Face match verified successfully and KYC upgraded to LEVEL_2", response));
    }

    @GetMapping("/status/user/{userId}")
    public ResponseEntity<ApiResponse<KycStatusResponse>> getStatusByUserId(@PathVariable String userId) {
        KycStatusResponse response = kycService.getKycStatus(userId);
        return ResponseEntity.ok(ApiResponse.success("KYC status retrieved successfully", response));
    }

    @GetMapping("/status/upi/{userUpiId}")
    public ResponseEntity<ApiResponse<KycStatusResponse>> getStatusByUserUpiId(@PathVariable String userUpiId) {
        KycStatusResponse response = kycService.getKycStatusByUpiId(userUpiId);
        return ResponseEntity.ok(ApiResponse.success("KYC status retrieved successfully", response));
    }

    @GetMapping("/status")
    public ResponseEntity<ApiResponse<KycStatusResponse>> getStatus(@RequestParam String userId) {
        KycStatusResponse response = kycService.getKycStatus(userId);
        return ResponseEntity.ok(ApiResponse.success("KYC status retrieved successfully", response));
    }

    @GetMapping("/audit-logs/{kycId}")
    public ResponseEntity<ApiResponse<List<KycAuditLog>>> getAuditLogs(@PathVariable String kycId) {
        List<KycAuditLog> logs = kycService.getAuditLogs(kycId);
        return ResponseEntity.ok(ApiResponse.success("Audit logs retrieved successfully", logs));
    }

    private String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    @Data
    public static class FaceMatchRequest {
        @NotBlank(message = "KYC ID is required")
        private String kycId;

        @NotBlank(message = "Selfie image (base64) is required")
        private String selfieImageBase64;
    }
}
