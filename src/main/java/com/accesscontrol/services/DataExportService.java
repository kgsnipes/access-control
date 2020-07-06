package com.accesscontrol.services;

import com.accesscontrol.beans.PageResult;
import com.accesscontrol.models.User;
import org.apache.commons.io.IOUtils;

import java.nio.file.Path;
import java.util.Date;

public interface DataExportService {

    PageResult<User> exportUserData(Date fromDate, Date toDate, Integer pageNumber, Integer pageLimit);

    void exportUserDataToFile(Path path, Date fromDate, Date toDate, Integer pageNumber, Integer pageLimit);

}
