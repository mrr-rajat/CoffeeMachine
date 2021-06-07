package com.dunzo.coffeemachine;

import com.dunzo.coffeemachine.bean.request.MachineInitializationRequest;
import com.dunzo.coffeemachine.entity.Beverage;
import com.dunzo.coffeemachine.entity.Item;
import com.dunzo.coffeemachine.impl.BeverageServiceImpl;
import com.dunzo.coffeemachine.impl.ItemServiceImpl;
import com.dunzo.coffeemachine.service.BeverageService;
import com.dunzo.coffeemachine.service.ItemService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * ItemService - manages all the items quantity, provides api like add item, get all items and transact items
 * BeverageService - this is responsible for making beverages, this service is dependent on ItemService, i could have
 * used any dependency injection framework, for now did it in the code only while starting machine
 * <p>
 * executor - this is outlet part of the machine, it provides threads to execute beverage request
 */
public class CoffeeMachine {

    private static final Logger logger = LoggerFactory.getLogger(CoffeeMachine.class);
    private static final int MAX_QUEUED_REQUEST = 100;
    private static CoffeeMachine INSTANCE;
    private final BeverageService beverageService;
    private final ItemService itemService;
    private final ThreadPoolExecutor executor;

    private CoffeeMachine(String initialConfigJson) throws IOException {
        logger.info("New Machine");
        MachineInitializationRequest request = new ObjectMapper().readValue(initialConfigJson,
                MachineInitializationRequest.class);
        int outlet = request.getMachine().getOutlets().getCount();
        executor = new ThreadPoolExecutor(outlet, outlet, 5000L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(MAX_QUEUED_REQUEST));
        this.itemService = new ItemServiceImpl(getItemListFromMap(request.getMachine().getIngredientQuantityMap()));
        this.beverageService = new BeverageServiceImpl(executor, this.itemService,
                getBeverageListFromMap(request.getMachine().getBeverages()));
        logger.info("Machine Started");
    }

    public static CoffeeMachine getInstance(String initialConfigJson) throws IOException {
        if (INSTANCE != null) return INSTANCE;

        synchronized (CoffeeMachine.class) {
            if (INSTANCE == null) INSTANCE = new CoffeeMachine(initialConfigJson);
            return INSTANCE;
        }
    }

    private List<Beverage> getBeverageListFromMap(Map<String, Map<String, Integer>> beveragesMap) {
        List<Beverage> beverages = new ArrayList<>();
        for (Map.Entry<String, Map<String, Integer>> beverage : beveragesMap.entrySet()) {
            beverages.add(new Beverage(beverage.getKey(), getItemListFromMap(beverage.getValue())));
        }
        return beverages;
    }

    private List<Item> getItemListFromMap(Map<String, Integer> itemsMap) {
        List<Item> items = new ArrayList<>();
        for (Map.Entry<String, Integer> item : itemsMap.entrySet()) {
            items.add(new Item(item.getKey(), item.getValue()));
        }
        return items;
    }

    public BeverageService getBeverageService() {
        return this.beverageService;
    }

    public ItemService getItemService() {
        return this.itemService;
    }

    public void reset() {
        this.executor.shutdown();
        INSTANCE = null;
    }
}
