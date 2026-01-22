package com.safhao.moomook.config;

import com.safhao.moomook.domain.Store;
import com.safhao.moomook.repository.StoreRepository;
import java.time.Instant;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class StoreInitializer implements ApplicationRunner {
    private final StoreRepository storeRepository;

    public StoreInitializer(StoreRepository storeRepository) {
        this.storeRepository = storeRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (storeRepository.count() > 0) {
            return;
        }

        Store store = new Store();
        Instant now = Instant.now();
        store.setName("모묵 본점");
        store.setSlug("moomook-default");
        store.setCreatedAt(now);
        store.setUpdatedAt(now);
        storeRepository.save(store);
    }
}
