package com.woodev.saas.mappers;

import com.woodev.saas.entities.Product;
import com.woodev.saas.entities.StockMvt;
import com.woodev.saas.requests.StockMvtRequest;
import com.woodev.saas.responses.StockMvtResponse;
import org.springframework.stereotype.Component;

@Component
public class StockMvtMapper {

    public StockMvt toEntity(final StockMvtRequest request){
        return StockMvt.builder()
                .typeMvt(request.getTypeMvt())
                .quantity(request.getQuantity())
                .dateMvt(request.getDateMvt())
                .comment(request.getComment())
                .product(Product.builder()
                        .id(request.getProductId())
                        .build())
                .build();
    }

    public StockMvtResponse toResponse(final StockMvt entity) {
        return StockMvtResponse.builder()
                .typeMvt(entity.getTypeMvt())
                .quantity(entity.getQuantity())
                .dateMvt(entity.getDateMvt())
                .comment(entity.getComment())
                .build();
    }

}
