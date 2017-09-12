package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * 创建日期：2016/12/20 on 16:18
 * 描述:
 * 作者:马飞奔 Administrator
 */
public class Paginator {
    @SerializedName("limit")
    @Expose
    private int limit;
    @SerializedName("page")
    @Expose
    private int page;
    @SerializedName("totalCount")
    @Expose
    private int totalCount;
    @SerializedName("offset")
    @Expose
    private int offset;
    @SerializedName("firstPage")
    @Expose
    private boolean firstPage;
    @SerializedName("lastPage")
    @Expose
    private boolean lastPage;
    @SerializedName("prePage")
    @Expose
    private int prePage;
    @SerializedName("nextPage")
    @Expose
    private int nextPage;
    @SerializedName("hasPrePage")
    @Expose
    private boolean hasPrePage;
    @SerializedName("hasNextPage")
    @Expose
    private boolean hasNextPage;
    @SerializedName("startRow")
    @Expose
    private int startRow;
    @SerializedName("endRow")
    @Expose
    private int endRow;
    @SerializedName("totalPages")
    @Expose
    private int totalPages;
    @SerializedName("slider")
    @Expose
    private List<Integer> slider = null;

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public boolean getFirstPage() {
        return firstPage;
    }

    public void setFirstPage(boolean firstPage) {
        this.firstPage = firstPage;
    }

    public boolean getLastPage() {
        return lastPage;
    }

    public void setLastPage(boolean lastPage) {
        this.lastPage = lastPage;
    }

    public int getPrePage() {
        return prePage;
    }

    public void setPrePage(int prePage) {
        this.prePage = prePage;
    }

    public int getNextPage() {
        return nextPage;
    }

    public void setNextPage(int nextPage) {
        this.nextPage = nextPage;
    }

    public boolean getHasPrePage() {
        return hasPrePage;
    }

    public void setHasPrePage(boolean hasPrePage) {
        this.hasPrePage = hasPrePage;
    }

    public boolean getHasNextPage() {
        return hasNextPage;
    }

    public void setHasNextPage(boolean hasNextPage) {
        this.hasNextPage = hasNextPage;
    }

    public int getStartRow() {
        return startRow;
    }

    public void setStartRow(int startRow) {
        this.startRow = startRow;
    }

    public int getEndRow() {
        return endRow;
    }

    public void setEndRow(int endRow) {
        this.endRow = endRow;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public List<Integer> getSlider() {
        return slider;
    }

    public void setSlider(List<Integer> slider) {
        this.slider = slider;
    }
}
