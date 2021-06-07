package com.dunzo.coffeemachine.bean;

import lombok.ToString;

@ToString
public enum ItemsTransactionStatus {
    COMPLETED,
    FAILED,
    ITEM_INSUFFICIENT
}
