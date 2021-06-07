package com.dunzo.coffeemachine.entity;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Beverage {
    private String name;
    private List<Item> items;
}
