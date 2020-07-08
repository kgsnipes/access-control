package com.accesscontrol.services.impl;

import com.accesscontrol.beans.AccessControlContext;
import com.accesscontrol.beans.PageResult;
import com.accesscontrol.constants.AccessControlConfigConstants;
import com.accesscontrol.exception.UserGroupNotFoundException;
import com.accesscontrol.models.UserGroup;
import com.accesscontrol.models.UserGroup2UserGroupRelation;
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

public class UserGroup2UserGroupRelationDataImportService implements DataImportService<UserGroup2UserGroupRelation> {

    private static Logger log= LogManager.getLogger(UserGroup2UserGroupRelationDataImportService.class);
    @Autowired
    private UserService userService;

    @Autowired
    @Qualifier(AccessControlConfigConstants.ACCESS_CONTROL_CONFIG)
    private Properties accessControlConfigProperties;


    @Override
    public PageResult<UserGroup2UserGroupRelation> process(List<UserGroup2UserGroupRelation> relations, AccessControlContext ctx) {
        if(CollectionUtils.isEmpty(relations) || Objects.isNull(ctx))
        {
            throw new IllegalArgumentException("list of relationships cannot be empty or context cannot be null");
        }
        PageResult<UserGroup2UserGroupRelation> result=new PageResult<>();
        result.setErrors(new ArrayList<>());
        result.setResults(new ArrayList<>());
        relations.stream().forEach(u->{
            try {
                UserGroup existingChildUserGroup=null;
                UserGroup existingParentUserGroup=null;

                try
                {
                    existingChildUserGroup=userService.getUserGroupByCode(u.getChildUserGroupCode());
                    existingParentUserGroup=userService.getUserGroupByCode(u.getParentUserGroupCode());

                }
                catch (UserGroupNotFoundException ex)
                {

                }

                PageResult<UserGroup> existingRelationship=userService.getChildUserGroupsForUserGroup(existingParentUserGroup.getCode(),-1);
                boolean relationShipAvailable=existingRelationship.getResults().stream().filter(userGroup -> userGroup.getCode().equals(u.getChildUserGroupCode())).findAny().isPresent();
                if(Objects.nonNull(existingChildUserGroup) && Objects.nonNull(existingParentUserGroup) && !relationShipAvailable)
                {
                    userService.addUserGroupToUserGroup(existingChildUserGroup,existingParentUserGroup,ctx);
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
    public PageResult<UserGroup2UserGroupRelation> process(Reader reader, AccessControlContext ctx) {
        CSVReader csvReader=new CSVReaderBuilder(reader).withVerifyReader(true).withCSVParser(new CSVParserBuilder().withSeparator(accessControlConfigProperties.getProperty(AccessControlConfigConstants.CSV_DELIMITER).charAt(0)).build()).withSkipLines((Integer) accessControlConfigProperties.get(AccessControlConfigConstants.CSV_SKIPLINES)).build();

        List<UserGroup2UserGroupRelation> list=new ArrayList<>();


        Iterator<String[]> itr=csvReader.iterator();
        while (itr.hasNext())
        {
            String arr[]=itr.next();
            if(StringUtils.isNotEmpty(StringUtils.join(arr)))
            {
                UserGroup2UserGroupRelation relation=new UserGroup2UserGroupRelation();
               relation.setChildUserGroupCode(StringUtils.trimToNull(arr[0]));
                relation.setParentUserGroupCode(StringUtils.trimToNull(arr[1]));
                list.add(relation);
            }
        }
        return  process(list,ctx);
    }
}
