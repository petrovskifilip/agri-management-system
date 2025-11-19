package com.finki.agrimanagement.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FarmResponseDTO {

    private Long id;
    private String name;
    private String location;
    private LocalDateTime createdAt;
    private int parcelCount;

}