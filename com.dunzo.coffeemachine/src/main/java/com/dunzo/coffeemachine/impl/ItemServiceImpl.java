package com.dunzo.coffeemachine.impl;

import com.dunzo.coffeemachine.bean.ItemsTransactionStatus;
import com.dunzo.coffeemachine.bean.response.ItemsTransactionResponse;
import com.dunzo.coffeemachine.entity.Item;
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

public class ItemServiceImpl implements ItemService {
    private static final Logger logger = LoggerFactory.getLogger(BeverageService.class);
    private final Map<String, Item> itemRepository;

    public ItemServiceImpl(List<Item> itemList) {
        itemRepository = new ConcurrentHashMap<>();
        if (itemList != null && !itemList.isEmpty()) {
            for (Item item : itemList) {
                _addItem(item);
            }
        }
    }

    private List<Item> _getAllAvailableItems() {
        List<Item> result = new ArrayList<>();
        for (Item item : itemRepository.values()) {
            result.add(new Item(item.getName(), item.getQuantity()));
        }
        return result;
    }

    private void _addItem(Item item) throws ItemException {
        logger.info("Item Add Request Received {}", item);
        synchronized (ItemService.class) {
            if (item == null || item.getName() == null || item.getQuantity() == null) {
                logger.error("Invalid Item Add Request {}", item);
                throw new ItemException("Invalid Item");
            }

            if (itemRepository.get(item.getName()) == null) {
                itemRepository.put(item.getName(), new Item(item.getName(), item.getQuantity()));
                logger.info("Item Added {}", item);
            } else {
                itemRepository.get(item.getName()).setQuantity(itemRepository.get(item.getName()).getQuantity() + item.getQuantity());
                logger.info("Item Quantity Increased {}", item);
            }
        }
    }

    private boolean _checkItemAvailability(Item item) throws ItemException {
        if (item == null || item.getName() == null || item.getQuantity() == null)
            throw new ItemException("Invalid Item");

        Item repositoryItem = itemRepository.get(item.getName());
        if (repositoryItem == null)
            throw new ItemException("Invalid Item " + item.getName());

        return item.getQuantity() <= repositoryItem.getQuantity();
    }

    private void _transactItem(String name, Integer quantity) {
        itemRepository.get(name)
                .setQuantity(
                        itemRepository.get(name).getQuantity() - quantity);
    }

    private ItemsTransactionResponse _transactItems(List<Item> itemList) throws ItemException {
        logger.info("Transact Items Request Received {}", itemList);
        synchronized (ItemService.class) {
            if (itemList == null || itemList.isEmpty())
                return new ItemsTransactionResponse(ItemsTransactionStatus.COMPLETED, null, null);

            boolean itemsSufficient = true;
            List<Item> insufficientItemList = new ArrayList<>();
            for (Item item : itemList) {
                if (!_checkItemAvailability(item)) {
                    itemsSufficient = false;
                    insufficientItemList.add(item);
                }
            }
            if (!itemsSufficient) {
                logger.info("Transact Items Request Denied, Insufficient Items {}", insufficientItemList);
                return new ItemsTransactionResponse(ItemsTransactionStatus.ITEM_INSUFFICIENT, "Item insufficient",
                        insufficientItemList);
            }
            for (Item item : itemList) {
                _transactItem(item.getName(), item.getQuantity());
            }
            logger.info("Transact Items Request Resolved {}", itemList);
            return new ItemsTransactionResponse(ItemsTransactionStatus.COMPLETED, null, null);
        }
    }

    @Override
    public CompletableFuture<Void> addItem(Item item) {
        return CompletableFuture.supplyAsync(() -> {
            _addItem(item);
            return null;
        });
    }

    @Override
    public CompletableFuture<ItemsTransactionResponse> transactItems(List<Item> itemList) {
        return CompletableFuture.supplyAsync(() -> _transactItems(itemList));
    }

    @Override
    public CompletableFuture<List<Item>> getAllAvailableItems() {
        return CompletableFuture.supplyAsync(this::_getAllAvailableItems);
    }
}
