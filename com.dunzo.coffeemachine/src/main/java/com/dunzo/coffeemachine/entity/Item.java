package com.dunzo.coffeemachine.entity;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Item {
    private String name;
    private Integer quantity;
}
