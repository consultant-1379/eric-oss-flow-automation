/*******************************************************************************
 * COPYRIGHT Ericsson 2021
 *
 *
 *
 * The copyright to the computer program(s) herein is the property of
 *
 * Ericsson Inc. The programs may be used and/or copied only with written
 *
 * permission from Ericsson Inc. or in accordance with the terms and
 *
 * conditions stipulated in the agreement/contract under which the
 *
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.services.flowautomation.test.fwk;

import java.util.Arrays;
import java.util.List;

/** 
 * Usertask input
 */
public class UsertaskInput {
    private String path;
    private List<String> pathAsList;
    private Object value;
    private Object extraValue;
    
    public UsertaskInput(final String path, final Object value) {
        this(path, value, null);
    }
    
    public UsertaskInput(final String path, final Object value, final Object extraValue) {
        this.path = path;
        
        final String[] pathElements = path.split(" > ");
        pathAsList = Arrays.asList(pathElements);

        this.value = value;

        this.extraValue = extraValue;
    }
    
    public String getPath() {
        return path;
    }
    
    public List<String> getPathAsList() {
        return pathAsList;
    }
    
    public Object getValue() {
        return value;
    }
    
    public Object getExtraValue() {
        return extraValue;
    }

    @Override
    public String toString() {
        return "UsertaskInput [path=" + path + ", pathAsList=" + pathAsList + ", value=" + value + ", extraValue="
                + extraValue + "]";
    }
}
