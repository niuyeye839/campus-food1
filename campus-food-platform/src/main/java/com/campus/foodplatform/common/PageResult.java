/** 分页结果封装类，包含总数和当前页数据列表 */
package com.campus.foodplatform.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {
    private long total;
    private List<T> records;

    public static <T> PageResult<T> of(long total, List<T> records) {
        return new PageResult<>(total, records);
    }
}
