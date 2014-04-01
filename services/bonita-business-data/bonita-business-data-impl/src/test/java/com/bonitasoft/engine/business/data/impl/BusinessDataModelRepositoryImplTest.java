package com.bonitasoft.engine.business.data.impl;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import org.bonitasoft.engine.dependency.DependencyService;
import org.bonitasoft.engine.dependency.SDependencyDeletionException;
import org.bonitasoft.engine.dependency.SDependencyNotFoundException;
import org.bonitasoft.engine.dependency.model.SDependency;
import org.bonitasoft.engine.dependency.model.SDependencyMapping;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.bonitasoft.engine.BOMBuilder;
import com.bonitasoft.engine.business.data.SBusinessDataRepositoryException;

@RunWith(MockitoJUnitRunner.class)
public class BusinessDataModelRepositoryImplTest {

    @Mock
    private DependencyService dependencyService;

    private BusinessDataModelRepositoryImpl businessDataModelRepository;

    @Before
    public void setUp() throws Exception {
        dependencyService = mock(DependencyService.class);
        businessDataModelRepository = spy(new BusinessDataModelRepositoryImpl(dependencyService, mock(SchemaUpdater.class)));
    }

    @Test
    public void deployABOMShouldCreatetheBOMJARAndAddANewTenantDependency() throws Exception {
        SDependency sDependency = mock(SDependency.class);
        SDependencyMapping dependencyMapping = mock(SDependencyMapping.class);
        doReturn(sDependency).when(businessDataModelRepository).createSDependency(anyLong(), any(byte[].class));
        doReturn(dependencyMapping).when(businessDataModelRepository).createDependencyMapping(1, sDependency);

        businessDataModelRepository.deploy(BOMBuilder.aBOM().buildZip(), 1);

        verify(dependencyService).createDependency(sDependency);
        verify(dependencyService).createDependencyMapping(dependencyMapping);
    }

    @Test
    public void uninstall_should_delete_a_dependency() throws Exception {
        businessDataModelRepository.undeploy(45l);

        verify(dependencyService).deleteDependency("BDR");
    }

    @Test
    public void uninstall_should_ignore_exception_if_the_dependency_does_not_exist() throws Exception {
        doThrow(new SDependencyNotFoundException("error")).when(dependencyService).deleteDependency("BDR");

        businessDataModelRepository.undeploy(45l);
    }

    @Test(expected = SBusinessDataRepositoryException.class)
    public void uninstall_should_throw_an_exception_if_an_exception_occurs_during_the_dependency_deletion() throws Exception {
        doThrow(new SDependencyDeletionException("error")).when(dependencyService).deleteDependency("BDR");

        businessDataModelRepository.undeploy(45l);
    }
}
