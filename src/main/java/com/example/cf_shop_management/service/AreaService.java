package com.example.cf_shop_management.service;

import com.example.cf_shop_management.dto.request.AreaCreateRequest;
import com.example.cf_shop_management.dto.request.AreaUpdateRequest;
import com.example.cf_shop_management.dto.response.AreaResponse;
import com.example.cf_shop_management.entity.Area;
import com.example.cf_shop_management.exception.AppException;
import com.example.cf_shop_management.exception.ErrorCode;
import com.example.cf_shop_management.mapper.AreaMapper;
import com.example.cf_shop_management.repository.AreaRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AreaService {

    AreaRepository areaRepository;
    AreaMapper areaMapper;

    @Transactional
    public AreaResponse createArea(AreaCreateRequest request) {
        if (areaRepository.existsByName(request.getName())) {
            throw new AppException(ErrorCode.AREA_NAME_EXISTED);
        }

        Area area = areaMapper.toArea(request);
        return areaMapper.toAreaResponse(areaRepository.save(area));
    }

    @Transactional(readOnly = true)
    public AreaResponse getAreaById(Long id) {
        Area area = areaRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.AREA_NOT_EXISTED));
        return areaMapper.toAreaResponse(area);
    }

    @Transactional(readOnly = true)
    public List<AreaResponse> getAllAreas() {
        return areaRepository.findAll()
                .stream()
                .map(areaMapper::toAreaResponse)
                .toList();
    }

    @Transactional
    public AreaResponse updateArea(Long id, AreaUpdateRequest request) {
        Area area = areaRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.AREA_NOT_EXISTED));

        if (request.getName() != null
                && !request.getName().equals(area.getName())
                && areaRepository.existsByName(request.getName())) {
            throw new AppException(ErrorCode.AREA_NAME_EXISTED);
        }

        areaMapper.updateArea(request, area);
        return areaMapper.toAreaResponse(areaRepository.save(area));
    }

    @Transactional
    public void deleteArea(Long id) {
        Area area = areaRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.AREA_NOT_EXISTED));
        areaRepository.delete(area);
    }
}
