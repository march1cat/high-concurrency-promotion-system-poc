package ind.poc.demo.service;

import ind.poc.demo.annotation.DBReadOnly;
import ind.poc.demo.annotation.UsePrimaryDatabase;
import ind.poc.demo.data.Product;
import ind.poc.demo.repository.ProductsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DatabaseAccessService {

    private final ProductsRepository productsRepository;

    @UsePrimaryDatabase
    public Product randomPickProduct(){
        final List<Product> all = productsRepository.findAll();
        if(all.isEmpty()) {
            throw new RuntimeException("No test product found!!");
        }
        int rnd = (int)(Math.random() * 236456)  % all.size();
        return all.get(rnd);
    }

    @DBReadOnly
    public List<Product> getProducts(List<String> ids){
        return productsRepository.findAllById(ids);
    }

}
