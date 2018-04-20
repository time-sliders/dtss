package com.dtss.web.vo;

/**
 * @author luyun
 * @since 2018.02.03 16:55
 */
public class BaseQuery {

    private static final int DEFAULT_PAGE_SIZE = 20;

    private int pageSize = DEFAULT_PAGE_SIZE;
    private int pageNo = 1;
    private int pageCount = 1;

    /**
     * 设置分页各参数
     */
    public void doPage(int count) {
        if (pageSize <= 0) this.pageSize = DEFAULT_PAGE_SIZE;
        if (count != -1) {
            if (count > pageSize) {
                pageCount = count % pageSize != 0 ? (count / pageSize) + 1 : (count / pageSize);
            } else {
                pageCount = 1;
            }
        }
        if (pageNo <= 0) this.pageNo = 1;
        if (pageNo > pageCount) this.pageNo = pageCount;
    }

    public Integer getPageNo() {
        return pageNo;
    }

    public boolean isHasNextPage() {
        return this.getPageNo() < this.getPageCount();
    }

    public void setHasNextPage(boolean isHasNextPage) {
    }

    public void setPageNo(Integer pageNo) {
        this.pageNo = pageNo;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getPageCount() {
        return pageCount;
    }

    public void setPageCount(Integer pageCount) {
        this.pageCount = pageCount;
    }
}
