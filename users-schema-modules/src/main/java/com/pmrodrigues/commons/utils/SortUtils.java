package com.pmrodrigues.commons.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.stream.Stream;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SortUtils {

    //TODO move to specific project
    public static List<Sort.Order> createSortForString(@NonNull String... sort){
        return Stream.of(sort)
                .map(s -> {
                    var sortRule = s.split("\\|");
                    if( sortRule.length == 1) {
                        return Sort.Order.asc(sortRule[0]);
                    }
                    if(Sort.Direction.ASC == Sort.Direction.valueOf(sortRule[1].toUpperCase())){
                        return Sort.Order.asc(sortRule[0]);
                    }else{
                        return  Sort.Order.desc(sortRule[0]);
                    }
                })
                .toList();
    }
}
