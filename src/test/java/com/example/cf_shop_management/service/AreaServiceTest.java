package com.example.cf_shop_management.service;

import com.example.cf_shop_management.dto.request.AreaCreateRequest;
import com.example.cf_shop_management.dto.request.AreaUpdateRequest;
import com.example.cf_shop_management.dto.response.AreaResponse;
import com.example.cf_shop_management.entity.Area;
import com.example.cf_shop_management.exception.AppException;
import com.example.cf_shop_management.exception.ErrorCode;
import com.example.cf_shop_management.mapper.AreaMapper;
import com.example.cf_shop_management.repository.AreaRepository;
import com.example.cf_shop_management.repository.TablesRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AreaServiceTest {

    @Mock
    private AreaRepository areaRepository;

    @Mock
    private TablesRepository tablesRepository;

    @Mock
    private AreaMapper areaMapper;

    @InjectMocks
    private AreaService areaService;

    private Area area;
    private AreaResponse areaResponse;

    @BeforeEach
    void setUp() {
        area = Area.builder()
                .id(1L)
                .name("Tầng 1 - Trong nhà")
                .build();

        areaResponse = AreaResponse.builder()
                .id(1L)
                .name("Tầng 1 - Trong nhà")
                .build();
    }

    @Test
    void createArea_Success() {
        AreaCreateRequest request = AreaCreateRequest.builder()
                .name("  Tầng 1 - Trong nhà  ")
                .build();

        Area mappedArea = new Area();

        when(areaRepository.existsByNameIgnoreCase("Tầng 1 - Trong nhà")).thenReturn(false);
        when(areaMapper.toArea(request)).thenReturn(mappedArea);
        when(areaRepository.save(mappedArea)).thenReturn(area);
        when(areaMapper.toAreaResponse(area)).thenReturn(areaResponse);

        AreaResponse result = areaService.createArea(request);

        assertNotNull(result);
        assertEquals("Tầng 1 - Trong nhà", mappedArea.getName());
        assertEquals("Tầng 1 - Trong nhà", result.getName());
        verify(areaRepository).save(mappedArea);
    }

    @Test
    void createArea_Existed_ThrowsException() {
        AreaCreateRequest request = AreaCreateRequest.builder()
                .name("  Tầng 1 - Trong nhà  ")
                .build();

        when(areaRepository.existsByNameIgnoreCase("Tầng 1 - Trong nhà")).thenReturn(true);

        AppException exception = assertThrows(AppException.class,
                () -> areaService.createArea(request));

        assertEquals(ErrorCode.AREA_EXISTED, exception.getErrorCode());
        verify(areaRepository, never()).save(any());
    }

    @Test
    void getAllAreas_WithKeyword_TrimsKeyword_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Area> areaPage = new PageImpl<>(List.of(area), pageable, 1);

        when(areaRepository.searchAreas("Tầng 1", pageable)).thenReturn(areaPage);
        when(areaMapper.toAreaResponse(area)).thenReturn(areaResponse);

        Page<AreaResponse> result = areaService.getAllAreas("  Tầng 1  ", pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("Tầng 1 - Trong nhà", result.getContent().get(0).getName());
        verify(areaRepository).searchAreas("Tầng 1", pageable);
    }

    @Test
    void getAllAreas_BlankKeyword_PassesNull_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Area> areaPage = new PageImpl<>(List.of(area), pageable, 1);

        when(areaRepository.searchAreas(null, pageable)).thenReturn(areaPage);
        when(areaMapper.toAreaResponse(area)).thenReturn(areaResponse);

        Page<AreaResponse> result = areaService.getAllAreas("   ", pageable);

        assertEquals(1, result.getTotalElements());
        verify(areaRepository).searchAreas(null, pageable);
    }

    @Test
    void getAllAreasList_Success() {
        when(areaRepository.findAll(Sort.by(Sort.Direction.ASC, "name"))).thenReturn(List.of(area));
        when(areaMapper.toAreaResponse(area)).thenReturn(areaResponse);

        List<AreaResponse> result = areaService.getAllAreasList();

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }

    @Test
    void getAreaById_Success() {
        when(areaRepository.findById(1L)).thenReturn(Optional.of(area));
        when(areaMapper.toAreaResponse(area)).thenReturn(areaResponse);

        AreaResponse result = areaService.getAreaById(1L);

        assertEquals(1L, result.getId());
        assertEquals("Tầng 1 - Trong nhà", result.getName());
    }

    @Test
    void getAreaById_NotFound_ThrowsException() {
        when(areaRepository.findById(99L)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class,
                () -> areaService.getAreaById(99L));

        assertEquals(ErrorCode.AREA_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void updateArea_Success() {
        AreaUpdateRequest request = AreaUpdateRequest.builder()
                .name("  Sân thượng  ")
                .build();

        AreaResponse updatedResponse = AreaResponse.builder()
                .id(1L)
                .name("Sân thượng")
                .build();

        when(areaRepository.findById(1L)).thenReturn(Optional.of(area));
        when(areaRepository.existsByNameIgnoreCaseAndIdNot("Sân thượng", 1L)).thenReturn(false);
        when(areaRepository.save(area)).thenReturn(area);
        when(areaMapper.toAreaResponse(area)).thenReturn(updatedResponse);

        AreaResponse result = areaService.updateArea(1L, request);

        assertEquals("Sân thượng", request.getName());
        assertEquals("Sân thượng", result.getName());
        verify(areaMapper).updateArea(request, area);
        verify(areaRepository).save(area);
    }

    @Test
    void updateArea_NotFound_ThrowsException() {
        AreaUpdateRequest request = AreaUpdateRequest.builder().name("Sân thượng").build();
        when(areaRepository.findById(99L)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class,
                () -> areaService.updateArea(99L, request));

        assertEquals(ErrorCode.AREA_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void updateArea_NameExisted_ThrowsException() {
        AreaUpdateRequest request = AreaUpdateRequest.builder()
                .name("Sân thượng")
                .build();

        when(areaRepository.findById(1L)).thenReturn(Optional.of(area));
        when(areaRepository.existsByNameIgnoreCaseAndIdNot("Sân thượng", 1L)).thenReturn(true);

        AppException exception = assertThrows(AppException.class,
                () -> areaService.updateArea(1L, request));

        assertEquals(ErrorCode.AREA_EXISTED, exception.getErrorCode());
        verify(areaRepository, never()).save(any());
    }

    @Test
    void deleteArea_Success() {
        when(areaRepository.findById(1L)).thenReturn(Optional.of(area));
        when(tablesRepository.existsByAreaId(1L)).thenReturn(false);
        doNothing().when(areaRepository).delete(area);

        areaService.deleteArea(1L);

        verify(areaRepository).delete(area);
    }

    @Test
    void deleteArea_NotFound_ThrowsException() {
        when(areaRepository.findById(99L)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class,
                () -> areaService.deleteArea(99L));

        assertEquals(ErrorCode.AREA_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void deleteArea_InUse_ThrowsException() {
        when(areaRepository.findById(1L)).thenReturn(Optional.of(area));
        when(tablesRepository.existsByAreaId(1L)).thenReturn(true);

        AppException exception = assertThrows(AppException.class,
                () -> areaService.deleteArea(1L));

        assertEquals(ErrorCode.AREA_IN_USE, exception.getErrorCode());
        verify(areaRepository, never()).delete(any());
    }
}