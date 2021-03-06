package com.accesscontrol.beans;

import java.util.Collection;
import java.util.List;

/**
 * Any API call that returns a paged result will use this DTO to return the results.
 * @param <T>
 */
public class PageResult<T>{

    private Collection<T> results;
    private Integer pageNumber;
    private Integer pageSize;
    private Integer totalResults;
    private Collection<Throwable> errors;

    public PageResult() {
    }

    public PageResult(List<T> results, Integer pageNumber, Integer pageSize, Integer totalResults) {
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

    public Collection<T> getResults() {
        return results;
    }

    public void setResults(Collection<T> results) {
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

    public Collection<Throwable> getErrors() {
        return errors;
    }

    public void setErrors(Collection<Throwable> errors) {
        this.errors = errors;
    }
}
