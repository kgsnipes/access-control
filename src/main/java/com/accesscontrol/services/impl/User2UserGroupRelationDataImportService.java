package com.accesscontrol.services.impl;

import com.accesscontrol.beans.AccessControlContext;
import com.accesscontrol.beans.PageResult;
import com.accesscontrol.constants.AccessControlConfigConstants;
import com.accesscontrol.exception.UserGroupNotFoundException;
import com.accesscontrol.exception.UserNotFoundException;
import com.accesscontrol.models.User;
import com.accesscontrol.models.User2UserGroupRelation;
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

public class User2UserGroupRelationDataImportService implements DataImportService<User2UserGroupRelation> {

    private static Logger log= LogManager.getLogger(User2UserGroupRelationDataImportService.class);
    @Autowired
    private UserService userService;

    @Autowired
    @Qualifier(AccessControlConfigConstants.ACCESS_CONTROL_CONFIG)
    private Properties accessControlConfigProperties;


    @Override
    public PageResult<User2UserGroupRelation> process(List<User2UserGroupRelation> relations, AccessControlContext ctx) {
        if(CollectionUtils.isEmpty(relations) || Objects.isNull(ctx))
        {
            throw new IllegalArgumentException("list of relationships cannot be empty or context cannot be null");
        }
        PageResult<User2UserGroupRelation> result=new PageResult<>();
        result.setErrors(new ArrayList<>());
        result.setResults(new ArrayList<>());
        relations.stream().forEach(u->{
            try {
                User existingUser=null;
                UserGroup existingParentUserGroup=null;

                try
                {
                    existingUser=userService.getUserById(u.getUserId());
                    existingParentUserGroup=userService.getUserGroupByCode(u.getUserGroupCode());

                }
                catch (UserGroupNotFoundException | UserNotFoundException ex)
                {

                }

                PageResult<UserGroup> existingRelationship=userService.getParentUserGroupsForUser(existingUser.getUserId(),-1);
                boolean relationShipAvailable=existingRelationship.getResults().stream().filter(userGroup -> userGroup.getCode().equals(u.getUserGroupCode())).findAny().isPresent();
                if(Objects.nonNull(existingUser) && Objects.nonNull(existingParentUserGroup) && !relationShipAvailable)
                {
                    userService.addUserToUserGroup(existingUser,existingParentUserGroup,ctx);
                    result.getResults().add(u);
                    result.getErrors().add(null);
                }

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
    public PageResult<User2UserGroupRelation> process(Reader reader, AccessControlContext ctx) {
        CSVReader csvReader=new CSVReaderBuilder(reader).withVerifyReader(true).withCSVParser(new CSVParserBuilder().withSeparator(accessControlConfigProperties.getProperty(AccessControlConfigConstants.CSV_DELIMITER).charAt(0)).build()).withSkipLines((Integer) accessControlConfigProperties.get(AccessControlConfigConstants.CSV_SKIPLINES)).build();

        List<User2UserGroupRelation> list=new ArrayList<>();


        Iterator<String[]> itr=csvReader.iterator();
        while (itr.hasNext())
        {
            String arr[]=itr.next();
            if(StringUtils.isNotEmpty(StringUtils.join(arr)))
            {
                User2UserGroupRelation relation=new User2UserGroupRelation();
               relation.setUserId(StringUtils.trimToNull(arr[0]));
                relation.setUserGroupCode(StringUtils.trimToNull(arr[1]));
                list.add(relation);
            }
        }
        return  process(list,ctx);
    }
}
