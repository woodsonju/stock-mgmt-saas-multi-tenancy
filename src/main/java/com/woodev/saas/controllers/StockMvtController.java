package com.woodev.saas.controllers;

import com.woodev.saas.common.PageResponse;
import com.woodev.saas.requests.StockMvtRequest;
import com.woodev.saas.responses.StockMvtResponse;
import com.woodev.saas.services.StockMvtService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/stocks")
@RequiredArgsConstructor
@Tag(name = "StockMvt", description = "Stock Mvt API")
public class StockMvtController {

    private final StockMvtService stockMvtService;

    @PostMapping
    public ResponseEntity<Void> createStockMvt(@RequestBody @Valid StockMvtRequest stockMvtRequest){
        this.stockMvtService.create(stockMvtRequest);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{stock-mvt-id}")
    public ResponseEntity<Void> updateStockMvt(
            @PathVariable("stock-mvt-id")
            @NotNull(message = "Stock Mvt ID cannot be null")
            final String id,
            @RequestBody
            @Valid
            final StockMvtRequest stockMvtRequest){
        this.stockMvtService.update(id, stockMvtRequest);
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/{stock-mvt-id}")
    public ResponseEntity<StockMvtResponse> findStockMvtById(
            @PathVariable("stock-mvt-id")
            @NotNull(message = "Stock Mvt ID cannot be null")
            final String id){
        return ResponseEntity.ok(this.stockMvtService.findById(id));
    }

    @GetMapping
    public ResponseEntity<PageResponse<StockMvtResponse>> findAllStockMvts(
            @RequestParam(name="page", defaultValue = "0") final int page,
            @RequestParam(name="size", defaultValue = "10") final int size
    ) {
        return ResponseEntity.ok(this.stockMvtService.findAll(page, size));
    }

   @DeleteMapping("/{stock-mvt-id}")
    public ResponseEntity<Void> deleteStockMvt(
            @PathVariable("stock-mvt-id")
            @NotNull(message = "Stock Mvt ID cannot be null")
            final String id){
       this.stockMvtService.delete(id);
       return ResponseEntity.noContent().build();
    }
}
