package com.lakshmigarments.context;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserInfo {
    private String userId;
    private String role;
}
