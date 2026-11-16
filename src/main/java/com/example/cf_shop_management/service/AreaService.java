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
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AreaService {

    AreaRepository areaRepository;
    TablesRepository tablesRepository;
    AreaMapper areaMapper;

    @Transactional
    public AreaResponse createArea(AreaCreateRequest request) {
        String trimmedName = request.getName().trim();
        if (areaRepository.existsByNameIgnoreCase(trimmedName)) {
            throw new AppException(ErrorCode.AREA_EXISTED);
        }

        Area area = areaMapper.toArea(request);
        area.setName(trimmedName);

        return areaMapper.toAreaResponse(areaRepository.save(area));
    }

    @Transactional(readOnly = true)
    public Page<AreaResponse> getAllAreas(String keyword, Pageable pageable) {
        String normalizedKeyword = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        return areaRepository.searchAreas(normalizedKeyword, pageable)
                .map(areaMapper::toAreaResponse);
    }

    @Transactional(readOnly = true)
    public List<AreaResponse> getAllAreasList() {
        return areaRepository.findAll(Sort.by(Sort.Direction.ASC, "name"))
                .stream()
                .map(areaMapper::toAreaResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AreaResponse getAreaById(Long id) {
        Area area = areaRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.AREA_NOT_FOUND));
        return areaMapper.toAreaResponse(area);
    }

    @Transactional
    public AreaResponse updateArea(Long id, AreaUpdateRequest request) {
        Area area = areaRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.AREA_NOT_FOUND));

        if (request.getName() != null && !request.getName().isBlank()) {
            String trimmedName = request.getName().trim();
            if (areaRepository.existsByNameIgnoreCaseAndIdNot(trimmedName, id)) {
                throw new AppException(ErrorCode.AREA_EXISTED);
            }
            request.setName(trimmedName);
        }

        areaMapper.updateArea(request, area);

        return areaMapper.toAreaResponse(areaRepository.save(area));
    }

    @Transactional
    public void deleteArea(Long id) {
        Area area = areaRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.AREA_NOT_FOUND));

        if (tablesRepository.existsByAreaId(id)) {
            throw new AppException(ErrorCode.AREA_IN_USE);
        }

        areaRepository.delete(area);
    }
}