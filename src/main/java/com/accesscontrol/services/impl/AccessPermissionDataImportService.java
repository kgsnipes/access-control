package com.accesscontrol.services.impl;

import com.accesscontrol.beans.AccessControlContext;
import com.accesscontrol.beans.PageResult;
import com.accesscontrol.constants.AccessControlConfigConstants;
import com.accesscontrol.models.AccessPermission;
import com.accesscontrol.repository.AccessPermissionRepository;
import com.accesscontrol.services.DataImportService;
import com.accesscontrol.services.UserService;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.io.Reader;
import java.util.*;

public class AccessPermissionDataImportService implements DataImportService<AccessPermission>{

    private static Logger log= LogManager.getLogger(AccessPermissionDataImportService.class);
    @Autowired
    private AccessPermissionRepository accessPermissionRepository;

    @Autowired
    private UserService userService;

    @Autowired
    @Qualifier(AccessControlConfigConstants.ACCESS_CONTROL_CONFIG)
    private Properties accessControlConfigProperties;

    @Override
    public PageResult<AccessPermission> process(List<AccessPermission> permissions, AccessControlContext ctx) {

        if(CollectionUtils.isEmpty(permissions) || Objects.isNull(ctx))
        {
            throw new IllegalArgumentException("list of permissions cannot be empty or context cannot be null");
        }

        PageResult<AccessPermission> result=new PageResult<>();
        result.setErrors(new ArrayList<>());
        result.setResults(new ArrayList<>());
        permissions.stream().forEach(u->{
            try {

                AccessPermission existingPermission=null;

                existingPermission=accessPermissionRepository.findByResourceAndPermission(u.getResource(),u.getPermission());
                log.info("Importing Relation for "+u.getPermission()+"-"+u.getResource());
                if(Objects.nonNull(existingPermission))
                {
                    result.getResults().add(userService.createPermission(u, ctx));
                }
                else
                {
                    result.getResults().add(userService.createPermission(u, ctx));
                }

                result.getErrors().add(null);
            }
            catch (Exception e)
            {
                log.error("Exception in creating user",e);
                result.getResults().add(u);
                result.getErrors().add(e);
            }
        });
        return result;
    }

    @Override
    public PageResult<AccessPermission>  process(Reader reader, AccessControlContext ctx) {
        CSVReader csvReader=new CSVReaderBuilder(reader).withVerifyReader(true).withCSVParser(new CSVParserBuilder().withSeparator(accessControlConfigProperties.getProperty(AccessControlConfigConstants.CSV_DELIMITER).charAt(0)).build()).withSkipLines((Integer) accessControlConfigProperties.get(AccessControlConfigConstants.CSV_SKIPLINES)).build();
        List<AccessPermission> list=new ArrayList<>();


        Iterator<String[]> itr=csvReader.iterator();
        while (itr.hasNext())
        {
            String arr[]=itr.next();
            if(StringUtils.isNotEmpty(StringUtils.join(arr)))
            {
                AccessPermission permission=new AccessPermission();
                permission.setResource(StringUtils.trimToNull(arr[0]));
                permission.setPermission(StringUtils.trimToNull(arr[1]));
                list.add(permission);
            }
        }
        return  process(list,ctx);
    }
}
