package com.dunzo.coffeemachine.bean.response;

import com.dunzo.coffeemachine.bean.ItemsTransactionStatus;
import com.dunzo.coffeemachine.entity.Item;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
public class ItemsTransactionResponse {
    private ItemsTransactionStatus status;
    private String failureReason;
    private List<Item> insufficientItemList;
}
