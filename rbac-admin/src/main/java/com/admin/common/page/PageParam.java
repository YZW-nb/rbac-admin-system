package com.admin.common.page;

/**
 * 通用分页参数工具
 */
public class PageParam {

    /** 默认页码 */
    public static final int DEFAULT_PAGE_NUM = 1;

    /** 默认每页条数 */
    public static final int DEFAULT_PAGE_SIZE = 10;

    /** 每页最大条数 */
    public static final int MAX_PAGE_SIZE = 200;

    /**
     * 限制 pageSize 上限，防止恶意传超大值导致 OOM
     *
     * @param pageSize 原始 pageSize
     * @return 限制后的 pageSize（1 ~ MAX_PAGE_SIZE）
     */
    public static int limitPageSize(int pageSize) {
        if (pageSize <= 0) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(pageSize, MAX_PAGE_SIZE);
    }

    /**
     * 限制 pageNum 下限
     *
     * @param pageNum 原始 pageNum
     * @return 限制后的 pageNum（最小 1）
     */
    public static int limitPageNum(int pageNum) {
        return Math.max(pageNum, DEFAULT_PAGE_NUM);
    }

    private PageParam() {
    }
}
