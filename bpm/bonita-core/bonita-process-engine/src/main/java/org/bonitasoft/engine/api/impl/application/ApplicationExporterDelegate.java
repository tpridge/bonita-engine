/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 ******************************************************************************/

package org.bonitasoft.engine.api.impl.application;

import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.bonitasoft.engine.business.application.ApplicationService;
import org.bonitasoft.engine.business.application.exporter.ApplicationExporter;
import org.bonitasoft.engine.business.application.filter.ApplicationsWithIdsFilterBuilder;
import org.bonitasoft.engine.business.application.model.SApplication;
import org.bonitasoft.engine.exception.ExportException;
import org.bonitasoft.engine.persistence.SBonitaReadException;

/**
 * @author Elias Ricken de Medeiros
 */
public class ApplicationExporterDelegate {

    private final ApplicationService applicationService;
    private final ApplicationExporter exporter;

    public ApplicationExporterDelegate(ApplicationService applicationService, ApplicationExporter exporter) {
        this.applicationService = applicationService;
        this.exporter = exporter;
    }

    public byte[] exportApplications(long... applicationIds) throws ExportException {
        ApplicationsWithIdsFilterBuilder filterBuilder = new ApplicationsWithIdsFilterBuilder(ArrayUtils.toObject(applicationIds));
        try {
            List<SApplication> applications = applicationService.searchApplications(filterBuilder.buildQueryOptions());
            return exporter.export(applications);
        } catch (SBonitaReadException e) {
            throw new ExportException(e);
        }
    }

}
