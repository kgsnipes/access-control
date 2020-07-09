package com.accesscontrol.services.impl;

import com.accesscontrol.beans.AccessControlContext;
import com.accesscontrol.beans.PageResult;
import com.accesscontrol.constants.AccessControlConfigConstants;
import com.accesscontrol.exception.UserGroupNotFoundException;
import com.accesscontrol.models.UserGroup;
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

public class UserGroupDataImportService implements DataImportService<UserGroup> {

    private static Logger log= LogManager.getLogger(UserGroupDataImportService.class);
    @Autowired
    private UserService userService;

    @Autowired
    @Qualifier(AccessControlConfigConstants.ACCESS_CONTROL_CONFIG)
    private Properties accessControlConfigProperties;

    @Override
    public PageResult<UserGroup> process(List<UserGroup> userGroups, AccessControlContext ctx) {
        if(CollectionUtils.isEmpty(userGroups) || Objects.isNull(ctx))
        {
            throw new IllegalArgumentException("list of user groups cannot be empty or context cannot be null");
        }
        PageResult<UserGroup> result=new PageResult<>();
        result.setErrors(new ArrayList<>());
        result.setResults(new ArrayList<>());
        userGroups.stream().forEach(u->{
            try {
                UserGroup existingUserGroup=null;
                try
                {
                    existingUserGroup=userService.getUserGroupByCode(u.getCode());

                }
                catch (UserGroupNotFoundException ex)
                {

                }
                log.info("Importing Relation for "+u.getCode());
                if(Objects.nonNull(existingUserGroup))
                {
                    result.getResults().add(userService.saveUserGroup(u, ctx));
                }
                else
                {
                    result.getResults().add(userService.createUserGroup(u, ctx));
                }


                result.getErrors().add(null);
            }
            catch (Exception e)
            {
                result.getResults().add(u);
                result.getErrors().add(e);
            }
        });
        return result;
    }

    @Override
    public PageResult<UserGroup> process(Reader reader, AccessControlContext ctx) {

        CSVReader csvReader=new CSVReaderBuilder(reader).withVerifyReader(true).withCSVParser(new CSVParserBuilder().withSeparator(accessControlConfigProperties.getProperty(AccessControlConfigConstants.CSV_DELIMITER).charAt(0)).build()).withSkipLines((Integer) accessControlConfigProperties.get(AccessControlConfigConstants.CSV_SKIPLINES)).build();

        List<UserGroup> list=new ArrayList<>();


        Iterator<String[]> itr=csvReader.iterator();
        while (itr.hasNext())
        {
            String arr[]=itr.next();
            if(StringUtils.isNotEmpty(StringUtils.join(arr)))
            {
                UserGroup group=new UserGroup();
                group.setCode(StringUtils.trimToNull(arr[0]));
                group.setName(StringUtils.trimToNull(arr[1]));
                group.setEnabled(Boolean.getBoolean(arr[2]));
                list.add(group);
            }
        }
        return  process(list,ctx);
    }
}
