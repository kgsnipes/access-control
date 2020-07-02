package com.accesscontrol.beans;

import java.util.List;

public class PageResult<T>{

    private List<T> results;
    private Integer pageNumber;
    private Integer pageSize;
    private Integer totalResults;

    public PageResult(List results, Integer pageNumber, Integer pageSize, Integer totalResults) {
        this.results = results;
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.totalResults = totalResults;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public List<T> getResults() {
        return results;
    }

    public void setResults(List<T> results) {
        this.results = results;
    }

    public Integer getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(Integer pageNumber) {
        this.pageNumber = pageNumber;
    }

    public Integer getTotalResults() {
        return totalResults;
    }

    public void setTotalResults(Integer totalResults) {
        this.totalResults = totalResults;
    }
}
