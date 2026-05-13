package com.vitaai.controller;

import com.vitaai.dto.ApiResponse;
import com.vitaai.dto.PageResponse;
import com.vitaai.entity.Drug;
import com.vitaai.service.DrugService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/drugs")
@RequiredArgsConstructor
public class DrugController {

    private final DrugService drugService;

    @GetMapping
    public ApiResponse<PageResponse<Drug>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String drugType) {
        Page<Drug> drugs = drugService.getDrugs(page, pageSize, keyword, drugType);
        return ApiResponse.success(PageResponse.of(
                drugs.getContent(), page, pageSize, drugs.getTotalElements()));
    }

    @GetMapping("/{id}")
    public ApiResponse<Drug> detail(@PathVariable Long id) {
        return ApiResponse.success(drugService.getDrug(id));
    }

    @PostMapping
    public ApiResponse<Drug> create(@RequestBody Drug drug, HttpServletRequest req) {
        Long userId = (Long) req.getAttribute("userId");
        return ApiResponse.success("创建成功", drugService.createDrug(drug, userId));
    }

    @PutMapping("/{id}")
    public ApiResponse<Drug> update(@PathVariable Long id, @RequestBody Drug drug) {
        return ApiResponse.success("更新成功", drugService.updateDrug(id, drug));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        drugService.deleteDrug(id);
        return ApiResponse.success("删除成功", null);
    }

    @GetMapping("/top")
    public ApiResponse<List<Drug>> top(@RequestParam(defaultValue = "10") int limit) {
        return ApiResponse.success(drugService.getTopDrugs(limit));
    }
}
