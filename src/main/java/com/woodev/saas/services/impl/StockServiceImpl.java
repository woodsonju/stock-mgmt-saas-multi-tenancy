package com.woodev.saas.services.impl;

import com.woodev.saas.common.PageResponse;
import com.woodev.saas.entities.Product;
import com.woodev.saas.entities.StockMvt;
import com.woodev.saas.mappers.StockMvtMapper;
import com.woodev.saas.repositories.ProductRepository;
import com.woodev.saas.repositories.StockMvtRepository;
import com.woodev.saas.requests.StockMvtRequest;
import com.woodev.saas.responses.StockMvtResponse;
import com.woodev.saas.services.StockMvtService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockServiceImpl implements StockMvtService {

    private final StockMvtRepository stockMvtRepository;
    private final StockMvtMapper stockMvtMapper;
    private final ProductRepository productRepository;

    @Override
    public void create(StockMvtRequest request) {
        //check if product exists by id
        checkIfProductExistsById(request.getProductId());
        final StockMvt stockMvt = this.stockMvtMapper.toEntity(request);
        this.stockMvtRepository.save(stockMvt);
    }

    @Override
    public void update(String id, StockMvtRequest request) {
        final Optional<StockMvt> stockMvt = this.stockMvtRepository.findById(id);
        if(stockMvt.isEmpty()) {
            log.debug("StockMvt does not exist");
            throw new EntityNotFoundException("StockMvt does not exist");
        }

        //check if product exists by id
        //On check si le produit existe ou non.
        //Pour éviter que l'utilisateur fasse une modification en passant un ID produit qui n'existe pas
        checkIfProductExistsById(request.getProductId());

        final StockMvt stockMvtToUpdate = this.stockMvtMapper.toEntity(request);
        stockMvtToUpdate.setId(id);
        this.stockMvtRepository.save(stockMvtToUpdate);
    }

    @Override
    public PageResponse<StockMvtResponse> findAll(int page, int size) {
        final PageRequest pageRequest = PageRequest.of(page, size);
        final Page<StockMvt> stockMvts = this.stockMvtRepository.findAll(pageRequest);
        final Page<StockMvtResponse> stockMvtResponsePage = stockMvts.map(this.stockMvtMapper::toResponse);
        return PageResponse.of(stockMvtResponsePage);
    }

    @Override
    public StockMvtResponse findById(String id) {
        return this.stockMvtRepository.findById(id)
                .map(this.stockMvtMapper::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("StockMvt does not exist"));
    }

    @Override
    public void delete(String id) {
       final StockMvt stockMvt = stockMvtRepository.findById(id)
               .orElseThrow(() -> new EntityNotFoundException("StockMvt does not exist"));
        stockMvtRepository.delete(stockMvt);

    }


    private void checkIfProductExistsById(final String productId) {
        final Optional<Product> product = this.productRepository.findById(productId);
        if(product.isEmpty()) {
            log.debug("Product does not exist");
            throw new EntityNotFoundException("Product does not exist");
        }
    }

}
