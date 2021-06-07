package com.dunzo.coffeemachine.impl;

import com.dunzo.coffeemachine.bean.response.ItemsTransactionResponse;
import com.dunzo.coffeemachine.entity.Beverage;
import com.dunzo.coffeemachine.exception.BeverageException;
import com.dunzo.coffeemachine.exception.ItemException;
import com.dunzo.coffeemachine.service.BeverageService;
import com.dunzo.coffeemachine.service.ItemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

public class BeverageServiceImpl implements BeverageService {

    private static final Logger logger = LoggerFactory.getLogger(BeverageService.class);
    private static final Integer DEFAULT_BEVERAGE_MAKING_TIME = 5 * 1000;
    private final ExecutorService executor;
    private final ItemService itemService;
    private final Map<String, Beverage> menu;

    public BeverageServiceImpl(ExecutorService executor, ItemService itemService, List<Beverage> menu) {
        this.executor = executor;
        this.itemService = itemService;
        this.menu = new ConcurrentHashMap<>();
        for (Beverage beverage : menu) {
            _addBeverage(beverage);
        }
    }

    private void _addBeverage(Beverage beverage) {
        logger.info("Beverage Add Request Received {}", beverage);
        synchronized (BeverageService.class) {
            if (beverage == null || beverage.getName() == null || beverage.getItems() == null || beverage.getItems().isEmpty()) {
                logger.error("Invalid Beverage Add Request {}", beverage);
                throw new ItemException("Invalid beverage");
            }

            if (menu.get(beverage.getName()) == null) {
                menu.put(beverage.getName(), new Beverage(beverage.getName(), new ArrayList<>(beverage.getItems())));
                logger.info("Beverage Added {}", beverage);
            }
        }
    }

    private Beverage _makeBeverage(Beverage beverage) {
        logger.info("Making Beverage {}", beverage);
        try {
            Thread.sleep(DEFAULT_BEVERAGE_MAKING_TIME);
        } catch (InterruptedException e) {
            logger.error("Unhandled Error while making Beverage {}, {}", beverage, e);
        }
        logger.info("Beverage made {}", beverage);
        return beverage;
    }

    @Override
    public CompletableFuture<Beverage> makeBeverage(String beverageName) {
        return CompletableFuture.supplyAsync(() -> {
            logger.info("Beverage Request Received {}", beverageName);
            Beverage beverage = menu.get(beverageName);
            if (beverage == null)
                throw new BeverageException("Invalid Beverage Request");

            ItemsTransactionResponse response = itemService.transactItems(beverage.getItems()).join();
            switch (response.getStatus()) {
                case FAILED:
                    logger.error("Beverage Request Failed {}, {}", beverageName, response.getFailureReason());
                    throw new BeverageException("Error while fetching items for beverage " + response.getFailureReason());
                case ITEM_INSUFFICIENT:
                    logger.error("Beverage Request Failed {}, Insufficient Items for Beverage {}",
                            beverageName, response.getFailureReason());
                    throw new BeverageException("Insufficient Items for Beverage " + beverage.getName());
            }
            return _makeBeverage(beverage);
        }, executor);
    }
}
