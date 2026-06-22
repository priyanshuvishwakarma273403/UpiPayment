package com.upimesh.bankgateway.service;

import com.upimesh.bankgateway.exception.InvalidIfscException;
import com.upimesh.bankgateway.model.entity.IfscDetail;
import com.upimesh.bankgateway.model.enums.BankCode;
import com.upimesh.bankgateway.model.response.IfscResponse;
import com.upimesh.bankgateway.repository.IfscDetailRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class IfscServiceTest {

    @Mock
    private IfscDetailRepository ifscRepo;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private IfscService ifscService;

    @Test
    public void validIfscFromCache() {
        String ifscCode = "HDFC0001234";
        IfscDetail detail = IfscDetail.builder()
                .ifscCode(ifscCode)
                .bankCode(BankCode.HDFC)
                .bankName("HDFC Bank")
                .branchName("Mumbai Branch")
                .city("Mumbai")
                .state("Maharashtra")
                .impsEnabled(true)
                .neftEnabled(true)
                .rtgsEnabled(false)
                .build();

        when(ifscRepo.findByIfscCode(ifscCode)).thenReturn(Optional.of(detail));

        IfscResponse response = ifscService.getIfscDetails(ifscCode);

        assertNotNull(response);
        assertTrue(response.isFromCache());
        assertEquals(ifscCode, response.getIfscCode());
        assertEquals("HDFC Bank", response.getBankName());
        verifyNoInteractions(restTemplate);
    }

    @Test
    public void invalidIfscFormatRejected() {
        assertThrows(InvalidIfscException.class, () -> {
            ifscService.getIfscDetails("HDFC00123");
        });
        assertThrows(InvalidIfscException.class, () -> {
            ifscService.getIfscDetails("hdfc0001234"); // lowercase prefix rejected or matches format? Format: ^[A-Z]{4}0[A-Z0-9]{6}$
        });
    }

    @Test
    public void fetchFromApiAndCache() {
        String ifscCode = "SBIN0000416";
        Map<String, Object> apiResponse = new HashMap<>();
        apiResponse.put("BANK", "State Bank of India");
        apiResponse.put("BRANCH", "Lucknow");
        apiResponse.put("CITY", "Lucknow");
        apiResponse.put("DISTRICT", "Lucknow");
        apiResponse.put("STATE", "Uttar Pradesh");
        apiResponse.put("ADDRESS", "Lucknow Chowk");
        apiResponse.put("CONTACT", "0522-12345");
        apiResponse.put("IMPS", true);
        apiResponse.put("NEFT", true);
        apiResponse.put("RTGS", true);

        when(ifscRepo.findByIfscCode(ifscCode)).thenReturn(Optional.empty());
        when(restTemplate.getForObject(any(String.class), eq(Map.class))).thenReturn(apiResponse);
        when(ifscRepo.save(any(IfscDetail.class))).thenAnswer(invocation -> invocation.getArgument(0));

        IfscResponse response = ifscService.getIfscDetails(ifscCode);

        assertNotNull(response);
        assertFalse(response.isFromCache());
        assertEquals(ifscCode, response.getIfscCode());
        assertEquals("State Bank of India", response.getBankName());
        assertTrue(response.isImpsEnabled());
        assertTrue(response.isRtgsEnabled());

        verify(ifscRepo, times(1)).save(any(IfscDetail.class));
    }
}
