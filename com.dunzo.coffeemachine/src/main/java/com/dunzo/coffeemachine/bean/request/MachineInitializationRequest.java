package com.dunzo.coffeemachine.bean.request;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class MachineInitializationRequest {
    private Machine machine;
}
