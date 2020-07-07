package com.accesscontrol.services;

import com.accesscontrol.beans.AccessControlContext;
import com.accesscontrol.beans.PageResult;
import com.accesscontrol.models.*;

import java.io.Reader;
import java.util.List;

public interface DataImportService<T> {

    void process(List<T> objects,AccessControlContext ctx);

    void process(Reader reader, AccessControlContext ctx);
}
