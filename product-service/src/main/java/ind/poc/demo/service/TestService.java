package ind.poc.demo.service;

import ind.poc.demo.data.Product;
import ind.poc.demo.repository.ProductsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TestService {

    private final ProductsRepository productsRepository;

    @Transactional(readOnly = true)
    public List<Product> tryQuery() {
        return productsRepository.findAll();
    }

    @Transactional
    public void tryAdd(){
    Product product = Product.builder()
            .id(UUID.randomUUID().toString())
            .name("test")
            .description("product desc")
            .build();
        productsRepository.save(product);
    }
}



