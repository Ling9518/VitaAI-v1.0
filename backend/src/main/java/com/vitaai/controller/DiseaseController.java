package com.vitaai.controller;

import com.vitaai.dto.ApiResponse;
import com.vitaai.dto.PageResponse;
import com.vitaai.entity.Disease;
import com.vitaai.entity.DiseaseDrugRel;
import com.vitaai.entity.Drug;
import com.vitaai.repository.DiseaseDrugRelRepository;
import com.vitaai.repository.DrugRepository;
import com.vitaai.service.DiseaseService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/diseases")
@RequiredArgsConstructor
public class DiseaseController {

    private final DiseaseService diseaseService;
    private final DiseaseDrugRelRepository diseaseDrugRelRepository;
    private final DrugRepository drugRepository;

    @GetMapping
    public ApiResponse<PageResponse<Disease>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String classification) {
        Page<Disease> diseases = diseaseService.getDiseases(page, pageSize, keyword, classification);
        return ApiResponse.success(PageResponse.of(
                diseases.getContent(), page, pageSize, diseases.getTotalElements()));
    }

    @GetMapping("/{id}")
    public ApiResponse<Disease> detail(@PathVariable Long id) {
        return ApiResponse.success(diseaseService.getDisease(id));
    }

    @PostMapping
    public ApiResponse<Disease> create(@RequestBody Disease disease, HttpServletRequest req) {
        Long userId = (Long) req.getAttribute("userId");
        return ApiResponse.success("创建成功", diseaseService.createDisease(disease, userId));
    }

    @PutMapping("/{id}")
    public ApiResponse<Disease> update(@PathVariable Long id, @RequestBody Disease disease) {
        return ApiResponse.success("更新成功", diseaseService.updateDisease(id, disease));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        diseaseService.deleteDisease(id);
        return ApiResponse.success("删除成功", null);
    }

    @GetMapping("/top")
    public ApiResponse<List<Disease>> top(@RequestParam(defaultValue = "10") int limit) {
        return ApiResponse.success(diseaseService.getTopDiseases(limit));
    }

    @GetMapping("/{id}/drugs")
    public ApiResponse<List<Drug>> getDiseaseDrugs(@PathVariable Long id) {
        List<DiseaseDrugRel> rels = diseaseDrugRelRepository.findByDiseaseId(id);
        List<Long> drugIds = rels.stream().map(DiseaseDrugRel::getDrugId).collect(Collectors.toList());
        List<Drug> drugs = drugRepository.findAllById(drugIds);
        return ApiResponse.success(drugs);
    }
}
