package com.dunzo.coffeemachine.service;

import com.dunzo.coffeemachine.entity.Beverage;

import java.util.concurrent.CompletableFuture;

public interface BeverageService {
    /**
     * Request a beverage, will dispense items from ItemService and make the requested beverage
     *
     * @param beverageName beverageName from the menu of the machine
     * @return CompletableFuture<Beverage>
     */
    CompletableFuture<Beverage> makeBeverage(String beverageName);
}
