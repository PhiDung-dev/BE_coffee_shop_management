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
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TablesService {

    TablesRepository tablesRepository;
    AreaRepository areaRepository;
    TablesMapper tablesMapper;

    @Transactional
    public TablesResponse createTable(TablesCreateRequest request) {
        if (tablesRepository.existsByTableNumber(request.getTableNumber())) {
            throw new AppException(ErrorCode.TABLE_NUMBER_EXISTED);
        }

        Area area = areaRepository.findById(request.getAreaId())
                .orElseThrow(() -> new AppException(ErrorCode.AREA_NOT_EXISTED));

        Tables table = tablesMapper.toTables(request);
        table.setArea(area);

        if (table.getStatus() == null || table.getStatus().isBlank()) {
            table.setStatus("AVAILABLE");
        }

        return tablesMapper.toTablesResponse(tablesRepository.save(table));
    }

    @Transactional(readOnly = true)
    public TablesResponse getTableById(Long id) {
        Tables table = tablesRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.TABLE_NOT_EXISTED));
        return tablesMapper.toTablesResponse(table);
    }

    @Transactional(readOnly = true)
    public TablesResponse getTableByNumber(String tableNumber) {
        Tables table = tablesRepository.findByTableNumber(tableNumber)
                .orElseThrow(() -> new AppException(ErrorCode.TABLE_NOT_EXISTED));
        return tablesMapper.toTablesResponse(table);
    }

    @Transactional(readOnly = true)
    public List<TablesResponse> getAllTables() {
        return tablesRepository.findAll()
                .stream()
                .map(tablesMapper::toTablesResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TablesResponse> getTablesByArea(Long areaId) {
        return tablesRepository.findByAreaId(areaId)
                .stream()
                .map(tablesMapper::toTablesResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TablesResponse> getTablesByStatus(String status) {
        return tablesRepository.findByStatus(status)
                .stream()
                .map(tablesMapper::toTablesResponse)
                .toList();
    }

    @Transactional
    public TablesResponse updateTable(Long id, TablesUpdateRequest request) {
        Tables table = tablesRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.TABLE_NOT_EXISTED));

        if (request.getTableNumber() != null
                && !request.getTableNumber().equals(table.getTableNumber())
                && tablesRepository.existsByTableNumber(request.getTableNumber())) {
            throw new AppException(ErrorCode.TABLE_NUMBER_EXISTED);
        }

        tablesMapper.updateTables(request, table);

        if (request.getAreaId() != null) {
            Area area = areaRepository.findById(request.getAreaId())
                    .orElseThrow(() -> new AppException(ErrorCode.AREA_NOT_EXISTED));
            table.setArea(area);
        }

        return tablesMapper.toTablesResponse(tablesRepository.save(table));
    }

    @Transactional
    public TablesResponse updateTableStatus(Long id, String status) {
        Tables table = tablesRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.TABLE_NOT_EXISTED));
        table.setStatus(status);
        return tablesMapper.toTablesResponse(tablesRepository.save(table));
    }

    @Transactional
    public void deleteTable(Long id) {
        Tables table = tablesRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.TABLE_NOT_EXISTED));
        tablesRepository.delete(table);
    }
}
