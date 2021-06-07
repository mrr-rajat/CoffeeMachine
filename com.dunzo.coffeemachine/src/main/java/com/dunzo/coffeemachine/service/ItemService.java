package com.dunzo.coffeemachine.service;

import com.dunzo.coffeemachine.bean.response.ItemsTransactionResponse;
import com.dunzo.coffeemachine.entity.Item;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ItemService {
    /**
     * Adds item if not present or increase quantity if present.
     *
     * @param item the item to add
     * @return future
     */
    CompletableFuture<Void> addItem(Item item) throws Exception;

    /**
     * Dispenses items from item repository
     *
     * @param itemList all the items to dispense from item repository
     * @return CompletableFuture<ItemsTransactionResponse>
     */
    CompletableFuture<ItemsTransactionResponse> transactItems(List<Item> itemList);

    /**
     * to check the availability and to check the status, it will return full state of repository
     *
     * @return CompletableFuture<List < Items>
     */
    CompletableFuture<List<Item>> getAllAvailableItems();
}
