/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.search.descriptor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.search.descriptor.FieldDescriptor;
import org.bonitasoft.engine.search.descriptor.SearchEntityDescriptor;

import com.bonitasoft.engine.business.application.ApplicationRouteSearchDescriptor;
import com.bonitasoft.engine.business.application.model.SApplicationPage;
import com.bonitasoft.engine.business.application.model.builder.SApplicationPageBuilderFactory;

/**
 * @author Elias Ricken de Medeiros
 *
 */
public class SearchApplicationPageDescriptor extends SearchEntityDescriptor {

    private final Map<String, FieldDescriptor> keys;

    private final Map<Class<? extends PersistentObject>, Set<String>> allFields;

    SearchApplicationPageDescriptor() {
        final SApplicationPageBuilderFactory keyProvider = BuilderFactory.get(SApplicationPageBuilderFactory.class);
        keys = new HashMap<String, FieldDescriptor>(4);
        keys.put(ApplicationRouteSearchDescriptor.ID, new FieldDescriptor(SApplicationPage.class, keyProvider.getIdKey()));
        keys.put(ApplicationRouteSearchDescriptor.TOKEN, new FieldDescriptor(SApplicationPage.class, keyProvider.getNameKey()));
        keys.put(ApplicationRouteSearchDescriptor.APPLICATION_ID, new FieldDescriptor(SApplicationPage.class, keyProvider.getApplicationIdKey()));
        keys.put(ApplicationRouteSearchDescriptor.PAGE_ID, new FieldDescriptor(SApplicationPage.class, keyProvider.getPageIdKey()));

        allFields = new HashMap<Class<? extends PersistentObject>, Set<String>>(1);

        final Set<String> pageFields = new HashSet<String>(1);
        pageFields.add(keyProvider.getNameKey());
        allFields.put(SApplicationPage.class, pageFields);
    }

    @Override
    protected Map<String, FieldDescriptor> getEntityKeys() {
        return keys;
    }

    @Override
    protected Map<Class<? extends PersistentObject>, Set<String>> getAllFields() {
        return allFields;
    }

}
