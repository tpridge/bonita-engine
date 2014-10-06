/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api.impl.application;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.exceptions.SObjectAlreadyExistsException;
import org.bonitasoft.engine.commons.exceptions.SObjectCreationException;
import org.bonitasoft.engine.commons.exceptions.SObjectModificationException;
import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.RetrieveException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.search.SearchResult;

import com.bonitasoft.engine.api.impl.convertor.ApplicationConvertor;
import com.bonitasoft.engine.api.impl.transaction.application.SearchApplicationPages;
import com.bonitasoft.engine.business.application.ApplicationNotFoundException;
import com.bonitasoft.engine.business.application.ApplicationRoute;
import com.bonitasoft.engine.business.application.ApplicationPageNotFoundException;
import com.bonitasoft.engine.business.application.ApplicationService;
import com.bonitasoft.engine.business.application.SInvalidDisplayNameException;
import com.bonitasoft.engine.business.application.SInvalidNameException;
import com.bonitasoft.engine.business.application.model.SApplicationPage;
import com.bonitasoft.engine.business.application.model.builder.SApplicationPageBuilder;
import com.bonitasoft.engine.business.application.model.builder.SApplicationPageBuilderFactory;
import com.bonitasoft.engine.business.application.model.builder.SApplicationUpdateBuilder;
import com.bonitasoft.engine.business.application.model.builder.SApplicationUpdateBuilderFactory;
import com.bonitasoft.engine.exception.InvalidDisplayNameException;
import com.bonitasoft.engine.exception.InvalidTokenException;
import com.bonitasoft.engine.service.TenantServiceAccessor;

/**
 * @author Elias Ricken de Medeiros
 *
 */
public class ApplicationPageAPIDelegate {

    private final ApplicationConvertor convertor;
    private final ApplicationService applicationService;
    private final SearchApplicationPages searchApplicationPages;

    public ApplicationPageAPIDelegate(final TenantServiceAccessor accessor, final ApplicationConvertor convertor,
            final SearchApplicationPages searchApplicationPages) {
        this.searchApplicationPages = searchApplicationPages;
        applicationService = accessor.getApplicationService();
        this.convertor = convertor;
    }

    public void setApplicationHomePage(final long applicationId, final long applicationPageId) throws UpdateException, InvalidTokenException,
            InvalidDisplayNameException, AlreadyExistsException, ApplicationNotFoundException {
        final SApplicationUpdateBuilder updateBuilder = BuilderFactory.get(SApplicationUpdateBuilderFactory.class).createNewInstance();
        updateBuilder.updateHomePageId(applicationPageId);
        try {
            applicationService.updateApplication(applicationId, updateBuilder.done());

        } catch (final SObjectModificationException e) {
            throw new UpdateException(e);
        } catch (final SInvalidNameException e) {
            throw new InvalidTokenException(e.getMessage());
        } catch (final SInvalidDisplayNameException e) {
            throw new InvalidDisplayNameException(e.getMessage());
        } catch (final SBonitaReadException e) {
            throw new UpdateException(e.getMessage());
        } catch (final SObjectAlreadyExistsException e) {
            throw new AlreadyExistsException(e.getMessage());
        } catch (final SObjectNotFoundException e) {
            throw new ApplicationNotFoundException(applicationId);
        }
    }

    public ApplicationRoute createApplicationPage(final long applicationId, final long pagedId, final String name) throws AlreadyExistsException,
    CreationException, InvalidTokenException, InvalidDisplayNameException {
        final SApplicationPageBuilderFactory factory = BuilderFactory.get(SApplicationPageBuilderFactory.class);
        final SApplicationPageBuilder builder = factory.createNewInstance(applicationId, pagedId, name);
        SApplicationPage sAppPage;
        try {
            sAppPage = applicationService.createApplicationPage(builder.done());
            return convertor.toApplicationPage(sAppPage);
        } catch (final SObjectCreationException e) {
            throw new CreationException(e);
        } catch (final SObjectAlreadyExistsException e) {
            throw new AlreadyExistsException(e.getMessage());
        } catch (final SInvalidNameException e) {
            throw new InvalidTokenException(e.getMessage());
        } catch (final SInvalidDisplayNameException e) {
            throw new InvalidDisplayNameException(e.getMessage());
        }
    }

    public ApplicationRoute getApplicationPage(final String applicationName, final String applicationPageName) throws ApplicationPageNotFoundException {
        try {
            final SApplicationPage sAppPage = applicationService.getApplicationPage(applicationName, applicationPageName);
            return convertor.toApplicationPage(sAppPage);
        } catch (final SBonitaReadException e) {
            throw new RetrieveException(e);
        } catch (final SObjectNotFoundException e) {
            throw new ApplicationPageNotFoundException(e.getMessage());
        }
    }

    public ApplicationRoute getApplicationPage(final long applicationPageId) throws ApplicationPageNotFoundException {
        try {
            final SApplicationPage sApplicationPage = applicationService.getApplicationPage(applicationPageId);
            return convertor.toApplicationPage(sApplicationPage);
        } catch (final SBonitaReadException e) {
            throw new RetrieveException(e);
        } catch (final SObjectNotFoundException e) {
            throw new ApplicationPageNotFoundException(e.getMessage());
        }
    }

    public void deleteApplicationPage(final long applicationpPageId) throws DeletionException {
        try {
            applicationService.deleteApplicationPage(applicationpPageId);
        } catch (final SBonitaException e) {
            throw new DeletionException(e);
        }
    }

    public ApplicationRoute getApplicationHomePage(final long applicationId) throws ApplicationPageNotFoundException {
        SApplicationPage sHomePage;
        try {
            sHomePage = applicationService.getApplicationHomePage(applicationId);
            return convertor.toApplicationPage(sHomePage);
        } catch (final SBonitaReadException e) {
            throw new RetrieveException(e);
        } catch (final SObjectNotFoundException e) {
            throw new ApplicationPageNotFoundException(e.getMessage());
        }
    }

    public SearchResult<ApplicationRoute> searchApplicationPages() throws SearchException {
        try {
            searchApplicationPages.execute();
            return searchApplicationPages.getResult();
        } catch (final SBonitaException e) {
            throw new SearchException(e);
        }
    }

}
