package com.dunzo.coffeemachine.bean.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Machine {
    private Outlet outlets;

    @JsonProperty("total_items_quantity")
    private Map<String, Integer> ingredientQuantityMap;

    @JsonProperty("beverages")
    private Map<String, Map<String, Integer>> beverages;
}
