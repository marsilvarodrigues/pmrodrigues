package com.pmrodrigues.commons.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public final class Password {
    private String password;
    private String cleanPassword;
}
