package com.savushkin.ignite.apllication.cache;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class MetricsResponseDTO {
    private String reads;
    private String writes;
    private String hits;
    private String misses;
}

