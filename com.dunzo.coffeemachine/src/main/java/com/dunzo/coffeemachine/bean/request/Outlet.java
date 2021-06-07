package com.dunzo.coffeemachine.bean.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Outlet {

    @JsonProperty("count_n")
    private int count;
}
