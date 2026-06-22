package com.upimesh.bankgateway.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.upimesh.bankgateway.model.enums.BankCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BankClientRegistryTest {

    @Mock
    private HdfcBankClient hdfcClient;

    @Mock
    private SbiBankClient sbiClient;

    @Mock
    private IciciBankClient iciciClient;

    @Mock
    private AxisBankClient axisClient;

    @Mock
    private KotakBankClient kotakClient;

    private BankClientRegistry registry;

    @BeforeEach
    public void setUp() {
        when(hdfcClient.getBankCode()).thenReturn(BankCode.HDFC);
        when(sbiClient.getBankCode()).thenReturn(BankCode.SBI);
        when(iciciClient.getBankCode()).thenReturn(BankCode.ICICI);
        when(axisClient.getBankCode()).thenReturn(BankCode.AXIS);
        when(kotakClient.getBankCode()).thenReturn(BankCode.KOTAK);

        List<BankClient> clients = Arrays.asList(hdfcClient, sbiClient, iciciClient, axisClient, kotakClient);
        registry = new BankClientRegistry(clients);
    }

    @Test
    public void allBanksRegistered() {
        assertTrue(registry.isSupported(BankCode.HDFC));
        assertTrue(registry.isSupported(BankCode.SBI));
        assertTrue(registry.isSupported(BankCode.ICICI));
        assertTrue(registry.isSupported(BankCode.AXIS));
        assertTrue(registry.isSupported(BankCode.KOTAK));
        assertEquals(5, registry.getSupportedBanks().size());
    }

    @Test
    public void getClientByIfscHdfc() {
        BankClient client = registry.getClientByIfsc("HDFC0001234");
        assertNotNull(client);
        assertEquals(BankCode.HDFC, client.getBankCode());
    }

    @Test
    public void getClientByUpiHandleSbi() {
        BankClient client1 = registry.getClientByUpiHandle("user@sbi");
        BankClient client2 = registry.getClientByUpiHandle("user@oksbi");
        assertNotNull(client1);
        assertNotNull(client2);
        assertEquals(BankCode.SBI, client1.getBankCode());
        assertEquals(BankCode.SBI, client2.getBankCode());
    }

    @Test
    public void unsupportedBankThrows() {
        assertThrows(BankClientRegistry.UnsupportedBankException.class, () -> {
            registry.getClientOrThrow(BankCode.PNB);
        });
    }

    @Test
    public void unsupportedUpiHandleThrows() {
        assertThrows(BankClientRegistry.UnsupportedBankException.class, () -> {
            registry.getClientByUpiHandle("user@unknownbank");
        });
    }
}
