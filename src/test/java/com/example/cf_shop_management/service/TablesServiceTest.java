package com.example.cf_shop_management.service;

import com.example.cf_shop_management.dto.request.TablesCreateRequest;
import com.example.cf_shop_management.dto.request.TablesUpdateRequest;
import com.example.cf_shop_management.dto.response.TablesResponse;
import com.example.cf_shop_management.entity.Area;
import com.example.cf_shop_management.entity.Tables;
import com.example.cf_shop_management.exception.AppException;
import com.example.cf_shop_management.exception.ErrorCode;
import com.example.cf_shop_management.mapper.TablesMapper;
import com.example.cf_shop_management.repository.AreaRepository;
import com.example.cf_shop_management.repository.TablesRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TablesServiceTest {

    @Mock
    private TablesRepository tablesRepository;

    @Mock
    private AreaRepository areaRepository;

    @Mock
    private TablesMapper tablesMapper;

    @InjectMocks
    private TablesService tablesService;

    private Area area;
    private Tables table;
    private TablesResponse tableResponse;

    @BeforeEach
    void setUp() {
        area = Area.builder()
                .id(1L)
                .name("Tầng 1")
                .build();

        table = Tables.builder()
                .id(10L)
                .tableNumber("B01")
                .status("AVAILABLE")
                .area(area)
                .build();

        tableResponse = TablesResponse.builder()
                .id(10L)
                .tableNumber("B01")
                .status("AVAILABLE")
                .build();
    }

    @Test
    void createTable_DefaultStatusAvailable_Success() {
        TablesCreateRequest request = TablesCreateRequest.builder()
                .tableNumber("  B01  ")
                .areaId(1L)
                .build();

        Tables mappedTable = new Tables();

        when(tablesRepository.existsByTableNumber("B01")).thenReturn(false);
        when(areaRepository.findById(1L)).thenReturn(Optional.of(area));
        when(tablesMapper.toTables(request)).thenReturn(mappedTable);
        when(tablesRepository.save(mappedTable)).thenReturn(table);
        when(tablesMapper.toTablesResponse(table)).thenReturn(tableResponse);

        TablesResponse result = tablesService.createTable(request);

        assertNotNull(result);
        assertEquals("B01", mappedTable.getTableNumber());
        assertEquals("AVAILABLE", mappedTable.getStatus());
        assertEquals(area, mappedTable.getArea());
        verify(tablesRepository).save(mappedTable);
    }

    @Test
    void createTable_TableNumberExisted_ThrowsException() {
        TablesCreateRequest request = TablesCreateRequest.builder()
                .tableNumber("  B01  ")
                .areaId(1L)
                .build();

        when(tablesRepository.existsByTableNumber("B01")).thenReturn(true);

        AppException exception = assertThrows(AppException.class,
                () -> tablesService.createTable(request));

        assertEquals(ErrorCode.TABLE_NUMBER_EXISTED, exception.getErrorCode());
        verify(tablesRepository, never()).save(any());
    }

    @Test
    void createTable_AreaNotExisted_ThrowsException() {
        TablesCreateRequest request = TablesCreateRequest.builder()
                .tableNumber("B02")
                .areaId(99L)
                .build();

        when(tablesRepository.existsByTableNumber("B02")).thenReturn(false);
        when(areaRepository.findById(99L)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class,
                () -> tablesService.createTable(request));

        assertEquals(ErrorCode.AREA_NOT_EXISTED, exception.getErrorCode());
        verify(tablesRepository, never()).save(any());
    }

    @Test
    void getTableById_Success() {
        when(tablesRepository.findById(10L)).thenReturn(Optional.of(table));
        when(tablesMapper.toTablesResponse(table)).thenReturn(tableResponse);

        TablesResponse result = tablesService.getTableById(10L);

        assertEquals(10L, result.getId());
    }

    @Test
    void getTableById_NotFound_ThrowsException() {
        when(tablesRepository.findById(99L)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class,
                () -> tablesService.getTableById(99L));

        assertEquals(ErrorCode.TABLE_NOT_EXISTED, exception.getErrorCode());
    }

    @Test
    void getTableByNumber_Success() {
        when(tablesRepository.findByTableNumber("B01")).thenReturn(Optional.of(table));
        when(tablesMapper.toTablesResponse(table)).thenReturn(tableResponse);

        TablesResponse result = tablesService.getTableByNumber("  B01  ");

        assertEquals("B01", result.getTableNumber());
    }

    @Test
    void getAllTables_Success() {
        when(tablesRepository.findAll()).thenReturn(List.of(table));
        when(tablesMapper.toTablesResponse(table)).thenReturn(tableResponse);

        List<TablesResponse> result = tablesService.getAllTables();

        assertEquals(1, result.size());
    }

    @Test
    void getTablesByArea_Success() {
        when(tablesRepository.findByAreaId(1L)).thenReturn(List.of(table));
        when(tablesMapper.toTablesResponse(table)).thenReturn(tableResponse);

        List<TablesResponse> result = tablesService.getTablesByArea(1L);

        assertEquals(1, result.size());
    }

    @Test
    void getTablesByStatus_Success() {
        when(tablesRepository.findByStatus("AVAILABLE")).thenReturn(List.of(table));
        when(tablesMapper.toTablesResponse(table)).thenReturn(tableResponse);

        List<TablesResponse> result = tablesService.getTablesByStatus("  available  ");

        assertEquals(1, result.size());
        verify(tablesRepository).findByStatus("AVAILABLE");
    }

    @Test
    void updateTable_Success() {
        Area newArea = Area.builder().id(2L).name("Sân thượng").build();
        TablesUpdateRequest request = TablesUpdateRequest.builder()
                .tableNumber("  VIP01  ")
                .status("  occupied  ")
                .areaId(2L)
                .build();

        when(tablesRepository.findById(10L)).thenReturn(Optional.of(table));
        when(tablesRepository.existsByTableNumberIgnoreCaseAndIdNot("VIP01", 10L)).thenReturn(false);
        when(areaRepository.findById(2L)).thenReturn(Optional.of(newArea));
        when(tablesRepository.save(table)).thenReturn(table);
        when(tablesMapper.toTablesResponse(table)).thenReturn(tableResponse);

        TablesResponse result = tablesService.updateTable(10L, request);

        assertNotNull(result);
        assertEquals("VIP01", request.getTableNumber());
        assertEquals("OCCUPIED", request.getStatus());
        assertEquals(newArea, table.getArea());
        verify(tablesMapper).updateTables(request, table);
        verify(tablesRepository).save(table);
    }

    @Test
    void updateTable_TableNumberExisted_ThrowsException() {
        TablesUpdateRequest request = TablesUpdateRequest.builder()
                .tableNumber("B02")
                .build();

        when(tablesRepository.findById(10L)).thenReturn(Optional.of(table));
        when(tablesRepository.existsByTableNumberIgnoreCaseAndIdNot("B02", 10L)).thenReturn(true);

        AppException exception = assertThrows(AppException.class,
                () -> tablesService.updateTable(10L, request));

        assertEquals(ErrorCode.TABLE_NUMBER_EXISTED, exception.getErrorCode());
        verify(tablesRepository, never()).save(any());
    }

    @Test
    void updateTableStatus_Success() {
        when(tablesRepository.findById(10L)).thenReturn(Optional.of(table));
        when(tablesRepository.save(table)).thenReturn(table);
        when(tablesMapper.toTablesResponse(table)).thenReturn(tableResponse);

        TablesResponse result = tablesService.updateTableStatus(10L, "  maintenance  ");

        assertNotNull(result);
        assertEquals("MAINTENANCE", table.getStatus());
        verify(tablesRepository).save(table);
    }

    @Test
    void deleteTable_Success() {
        when(tablesRepository.findById(10L)).thenReturn(Optional.of(table));
        doNothing().when(tablesRepository).delete(table);

        tablesService.deleteTable(10L);

        verify(tablesRepository).delete(table);
    }
}