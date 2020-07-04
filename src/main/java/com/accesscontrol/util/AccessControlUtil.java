package com.accesscontrol.util;


import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public class AccessControlUtil {

    public static Collection getPagedResult(Collection collection, int page, int limit) throws IllegalAccessException, InstantiationException {
        if(page>=1) {
            Collection retVal = collection.getClass().newInstance();
            collection.stream().skip((page - 1) * limit).limit(limit).forEach(ele -> {
                retVal.add(ele);
            });
            return retVal;
        }
        return collection;
    }

    public static Pageable getPageParameter(JpaRepository repository, Integer pageNumber,Integer limit)
    {
        if(pageNumber<1)
        {
            return PageRequest.of(0, (int) repository.count());
        }
        else
        {
            return PageRequest.of(pageNumber-1,limit);
        }

    }
}
