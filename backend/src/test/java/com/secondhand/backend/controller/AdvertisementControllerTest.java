package com.secondhand.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.secondhand.backend.dto.AdvertisementDto;
import com.secondhand.backend.service.AdvertisementService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdvertisementController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdvertisementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AdvertisementService advertisementService;

    @Test
    void getAdvertisementById_ShouldReturnAdvertisement() throws Exception {

        AdvertisementDto dto = AdvertisementDto.builder()
                .id(1L)
                .title("Laptop")
                .price(25000.0)
                .build();

        when(advertisementService.getAdvertisementById(1L))
                .thenReturn(dto);

        mockMvc.perform(get("/api/advertisements/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Laptop"))
                .andExpect(jsonPath("$.price").value(25000.0));
    }

    @Test
    void getAllActiveAdvertisements_ShouldReturnList() throws Exception {

        AdvertisementDto dto = AdvertisementDto.builder()
                .id(1L)
                .title("Phone")
                .price(12000.0)
                .build();

        when(advertisementService.getAllActiveAdvertisement(anyString(), anyString()))
                .thenReturn(List.of(dto));

        mockMvc.perform(get("/api/advertisements/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Phone"));
    }

    @Test
    void approveAdvertisement_ShouldReturnApprovedAdvertisement() throws Exception {

        AdvertisementDto dto = AdvertisementDto.builder()
                .id(1L)
                .title("TV")
                .build();

        when(advertisementService.approveAdvertisement(1L))
                .thenReturn(dto);

        mockMvc.perform(put("/api/advertisements/1/approve"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("TV"));
    }

    @Test
    void rejectAdvertisement_ShouldReturnRejectedAdvertisement() throws Exception {

        AdvertisementDto dto = AdvertisementDto.builder()
                .id(1L)
                .title("Camera")
                .build();

        when(advertisementService.rejectAdvertisement(1L, "Image is not clear"))
                .thenReturn(dto);

        mockMvc.perform(put("/api/advertisements/1/reject")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"Image is not clear\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Camera"));
    }

    @Test
    void markAsSold_ShouldReturnSoldAdvertisement() throws Exception {

        AdvertisementDto dto = AdvertisementDto.builder()
                .id(1L)
                .title("Monitor")
                .build();

        when(advertisementService.markAsSold(1L))
                .thenReturn(dto);

        mockMvc.perform(put("/api/advertisements/1/sold"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Monitor"));
    }
}