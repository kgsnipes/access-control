package com.accesscontrol.services.impl;

import com.accesscontrol.beans.AccessControlContext;
import com.accesscontrol.beans.PageResult;
import com.accesscontrol.constants.AccessControlConfigConstants;
import com.accesscontrol.models.AccessPermission;
import com.accesscontrol.models.AccessPermission2UserGroupRelation;
import com.accesscontrol.models.UserGroup;
import com.accesscontrol.repository.AccessPermissionRepository;
import com.accesscontrol.services.DataImportService;
import com.accesscontrol.services.UserService;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.io.Reader;
import java.util.*;

public class AccessPermission2UserGroupRelationDataImportService implements DataImportService<AccessPermission2UserGroupRelation> {

    private static final Logger log= LogManager.getLogger(AccessPermission2UserGroupRelationDataImportService.class);
    @Autowired
    private UserService userService;

    @Autowired
    private AccessPermissionRepository accessPermissionRepository;

    @Autowired
    @Qualifier(AccessControlConfigConstants.ACCESS_CONTROL_CONFIG)
    private Properties accessControlConfigProperties;


    @Override
    public PageResult<AccessPermission2UserGroupRelation> process(List<AccessPermission2UserGroupRelation> relations, AccessControlContext ctx) {
        if(CollectionUtils.isEmpty(relations) || Objects.isNull(ctx))
        {
            throw new IllegalArgumentException("list of relationships cannot be empty or context cannot be null");
        }
        PageResult<AccessPermission2UserGroupRelation> result=new PageResult<>();
        result.setErrors(new ArrayList<>());
        result.setResults(new ArrayList<>());
        relations.stream().forEach(u->{
            try {
                AccessPermission existingAccessPermission=null;
                UserGroup existingUserGroup=null;

                existingAccessPermission=accessPermissionRepository.findById(u.getAccessPermissionId()).get();
                existingUserGroup=userService.getUserGroupByCode(u.getUserGroupCode());

                PageResult<AccessPermission> existingRelationship=userService.getPermissionsByResourceAndUserGroup(existingAccessPermission.getResource(),existingUserGroup.getCode(),false,-1);
                AccessPermission finalExistingAccessPermission = existingAccessPermission;
                boolean relationShipAvailable=existingRelationship.getResults().stream().anyMatch(accessPermission -> accessPermission.getResource().equals(finalExistingAccessPermission.getResource())&& accessPermission.getPermission().equals(finalExistingAccessPermission.getPermission()));
                log.info("Importing Relation for {}",u.getUserGroupCode());
                if(Objects.nonNull(existingAccessPermission) && Objects.nonNull(existingUserGroup) && !relationShipAvailable)
                {

                    userService.enablePermission(existingAccessPermission,existingUserGroup,ctx);
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
    public PageResult<AccessPermission2UserGroupRelation> process(Reader reader, AccessControlContext ctx) {
        CSVReader csvReader=new CSVReaderBuilder(reader).withVerifyReader(true).withCSVParser(new CSVParserBuilder().withSeparator(accessControlConfigProperties.getProperty(AccessControlConfigConstants.CSV_DELIMITER).charAt(0)).build()).withSkipLines((Integer) accessControlConfigProperties.get(AccessControlConfigConstants.CSV_SKIPLINES)).build();

        List<AccessPermission2UserGroupRelation> list=new ArrayList<>();


        Iterator<String[]> itr=csvReader.iterator();
        while (itr.hasNext())
        {
            String[] arr=itr.next();
            if(StringUtils.isNotEmpty(StringUtils.join(arr)))
            {
                AccessPermission2UserGroupRelation relation=new AccessPermission2UserGroupRelation();
                relation.setUserGroupCode(StringUtils.trimToNull(arr[0]));
                relation.setAccessPermissionId(accessPermissionRepository.findByResourceAndPermission(StringUtils.trimToNull(arr[1]),StringUtils.trimToNull(arr[2])).getId());
                relation.setEnabled(BooleanUtils.toBoolean(arr[3]));
                list.add(relation);
            }
        }
        return  process(list,ctx);
    }
}
