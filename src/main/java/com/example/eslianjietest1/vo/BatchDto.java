package com.example.eslianjietest1.vo;

import lombok.Data;

import java.util.Map;

/**
 * @(#) BatchDto 1.0 2018/5/22
 * <p>
 * Copyright (c) 2017, YUNXI. All rights reserved.
 * YUNXI PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
@Data
public class BatchDto {

    private String docId ;

    private String parentId ;

    private String routingId ;

    private Map<String, Object> source ;

    public BatchDto() {
        super();
    }

    public BatchDto(String docId, String parentId, String routingId, Map<String, Object> source) {
        this.docId = docId;
        this.parentId = parentId;
        this.routingId = routingId;
        this.source = source;
    }
}
