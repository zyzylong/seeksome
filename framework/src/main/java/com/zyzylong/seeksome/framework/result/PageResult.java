package com.zyzylong.seeksome.framework.result;

import lombok.Data;

import java.util.List;

/**
 * 分页结果。
 */
@Data
public class PageResult<T> {

    private long total;

    private long pageNum;

    private long pageSize;

    private List<T> list;

    private PageResult(long total, long pageNum, long pageSize, List<T> list) {
        this.total = total;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.list = list;
    }

    public static <T> PageResult<T> of(long total, long pageNum, long pageSize, List<T> list) {
        return new PageResult<>(total, pageNum, pageSize, list);
    }
}
