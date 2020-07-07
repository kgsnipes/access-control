package com.accesscontrol.services;


import java.io.Writer;


public interface DataExportService<T> {

    void process(Writer writer, Integer pageNumber, Integer pageLimit);
}
