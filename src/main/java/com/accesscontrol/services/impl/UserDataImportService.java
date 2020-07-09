package com.accesscontrol.services.impl;

import com.accesscontrol.beans.AccessControlContext;
import com.accesscontrol.beans.PageResult;
import com.accesscontrol.constants.AccessControlConfigConstants;
import com.accesscontrol.exception.UserNotFoundException;
import com.accesscontrol.models.User;
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

public class UserDataImportService implements DataImportService<User>{

    private static Logger log= LogManager.getLogger(UserDataImportService.class);
    @Autowired
    private UserService userService;

    @Autowired
    @Qualifier(AccessControlConfigConstants.ACCESS_CONTROL_CONFIG)
    private Properties accessControlConfigProperties;

    @Override
    public PageResult<User> process(List<User> users, AccessControlContext ctx) {

        if(CollectionUtils.isEmpty(users) || Objects.isNull(ctx))
        {
            throw new IllegalArgumentException("list of users cannot be empty or context cannot be null");
        }

        PageResult<User> result=new PageResult<>();
        result.setErrors(new ArrayList<>());
        result.setResults(new ArrayList<>());
        users.stream().forEach(u->{
            try {

                User existingUser=null;
                try
                {
                    existingUser=userService.getUserById(u.getUserId());

                }
                catch (UserNotFoundException ex)
                {

                }
                log.info("Importing User "+u.getUserId());
                if(Objects.nonNull(existingUser))
                {
                    result.getResults().add(userService.saveUser(u, ctx));
                }
                else
                {
                    result.getResults().add(userService.createUser(u, ctx));
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
    public PageResult<User>  process(Reader reader, AccessControlContext ctx) {
        CSVReader csvReader=new CSVReaderBuilder(reader).withVerifyReader(true).withCSVParser(new CSVParserBuilder().withSeparator(accessControlConfigProperties.getProperty(AccessControlConfigConstants.CSV_DELIMITER).charAt(0)).build()).withSkipLines((Integer) accessControlConfigProperties.get(AccessControlConfigConstants.CSV_SKIPLINES)).build();
        List<User> userList=new ArrayList<>();


        Iterator<String[]> itr=csvReader.iterator();
        while (itr.hasNext())
        {
            String arr[]=itr.next();
            if(StringUtils.isNotEmpty(StringUtils.join(arr)))
            {
                User user=new User();
                user.setUserId(StringUtils.trimToNull(arr[0]));
                user.setPassword(StringUtils.trimToNull(arr[1]));
                user.setFirstName(StringUtils.trimToNull(arr[2]));
                user.setLastName(StringUtils.trimToNull(arr[3]));
                user.setEnabled(Boolean.getBoolean(arr[4]));
                userList.add(user);
            }
        }
        return  process(userList,ctx);
    }
}
