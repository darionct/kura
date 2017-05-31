package org.eclipse.kura.web.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.eclipse.kura.KuraException;
import org.eclipse.kura.configuration.ComponentConfiguration;
import org.eclipse.kura.configuration.ConfigurableComponent;
import org.eclipse.kura.configuration.ConfigurationService;
import org.eclipse.kura.core.configuration.ComponentConfigurationImpl;
import org.eclipse.kura.core.configuration.metatype.Tocd;
import org.eclipse.kura.core.testutil.TestUtil;
import org.eclipse.kura.web.Console;
import org.eclipse.kura.web.shared.GwtKuraException;
import org.eclipse.kura.web.shared.model.GwtConfigComponent;
import org.eclipse.kura.web.shared.model.GwtConfigParameter;
import org.eclipse.kura.web.shared.model.GwtXSRFToken;
import org.eclipse.kura.wire.WireHelperService;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.ComponentContext;

public class GwtComponentServiceImplTest {

    private static ConfigurableComponent configurableComponentSvc;
    private static ConfigurationService configurationSvc;
    private static WireHelperService wireHelperService;
    private static ConfigurationAdmin configurationAdmin;

    private GwtXSRFToken xsrfToken;
    private GwtComponentServiceImpl gwtCompService;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        HashMap compCfgProperties = new HashMap();
        // compCfgProperties.
        ComponentConfigurationImpl compCfg = new ComponentConfigurationImpl("testPid", new Tocd(), compCfgProperties);
        List<ComponentConfiguration> compCfgList = new ArrayList<>();
        compCfgList.add(compCfg);

        configurableComponentSvc = mock(ConfigurableComponent.class);
        ServiceReference<ConfigurableComponent> svcRef = mock(ServiceReference.class);

        configurationSvc = mock(ConfigurationService.class);
        when(configurationSvc.getComponentConfiguration("testPid")).thenReturn(compCfg);
        when(configurationSvc.getComponentConfigurations()).thenReturn(compCfgList);
        ServiceReference<ConfigurationService> svcRefConfigSvc = mock(ServiceReference.class);

        wireHelperService = mock(WireHelperService.class);
        ServiceReference<WireHelperService> svcWireHelperSvc = mock(ServiceReference.class);

        configurationAdmin = mock(ConfigurationAdmin.class);
        ServiceReference<ConfigurationAdmin> svcConfigAdmin = mock(ServiceReference.class);

        BundleContext bundleContext = mock(BundleContext.class);
        when(bundleContext.getServiceReference(ConfigurableComponent.class)).thenReturn(svcRef);
        when(bundleContext.getService(svcRef)).thenReturn(configurableComponentSvc);

        when(bundleContext.getServiceReference(ConfigurationService.class)).thenReturn(svcRefConfigSvc);
        when(bundleContext.getService(svcRefConfigSvc)).thenReturn(configurationSvc);

        when(bundleContext.getServiceReference(WireHelperService.class)).thenReturn(svcWireHelperSvc);
        when(bundleContext.getService(svcWireHelperSvc)).thenReturn(wireHelperService);

        when(bundleContext.getServiceReference(ConfigurationAdmin.class)).thenReturn(svcConfigAdmin);
        when(bundleContext.getService(svcConfigAdmin)).thenReturn(configurationAdmin);

        ComponentContext componentContext = mock(ComponentContext.class);
        when(componentContext.getBundleContext()).thenReturn(bundleContext);

        TestUtil.setFieldValue(new Console(), "s_context", componentContext); // set static field
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        gwtCompService = new GwtComponentServiceImpl();

        xsrfToken = new GwtXSRFToken();
        xsrfToken.setToken("token");

        ThreadLocal<HttpServletRequest> requestVar = new ThreadLocal<HttpServletRequest>();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute(GwtSecurityTokenServiceImpl.XSRF_TOKEN_KEY)).thenReturn(xsrfToken);
        when(request.getSession()).thenReturn(session);
        requestVar.set(request);
        TestUtil.setFieldValue(gwtCompService, "perThreadRequest", requestVar);
    }

    @After
    public void tearDown() throws Exception {
        TestUtil.setFieldValue(gwtCompService, "perThreadRequest", null);
        xsrfToken = null;
        gwtCompService = null;
    }

    @Test
    public void testFindTrackedPids() throws GwtKuraException {
        List<String> trackedPids = gwtCompService.findTrackedPids(xsrfToken);
        verify(configurationSvc).getConfigurableComponentPids();
    }

    @Test
    public void testFindServicesConfigurations() throws GwtKuraException {
        List<GwtConfigComponent> serviceConfigList = gwtCompService.findServicesConfigurations(xsrfToken);
        assertTrue(serviceConfigList.size() > 0);
        GwtConfigComponent gwtConfigComponent = serviceConfigList.get(0);
        assertEquals("testPid", gwtConfigComponent.getComponentId());
    }

    @Test
    public void testFindFilteredComponentConfigurations() throws GwtKuraException {
        List<GwtConfigComponent> filteredCompConfig = gwtCompService.findFilteredComponentConfigurations(xsrfToken);
        assertTrue(filteredCompConfig.size() > 0);
        GwtConfigComponent gwtConfigComponent = filteredCompConfig.get(0);
        assertEquals("testPid", gwtConfigComponent.getComponentId());
    }

    @Test
    public void testFindFilteredComponentConfiguration() throws GwtKuraException {
        List<GwtConfigComponent> filteredCompConfig = gwtCompService.findFilteredComponentConfiguration(xsrfToken,
                "testPid");
        assertTrue(filteredCompConfig.size() > 0);
        GwtConfigComponent gwtConfigComponent = filteredCompConfig.get(0);
        assertEquals("testPid", gwtConfigComponent.getComponentId());
    }

    @Test
    public void testFindComponentConfigurations() throws GwtKuraException {
        List<GwtConfigComponent> compConfigs = gwtCompService.findComponentConfigurations(xsrfToken);
        assertTrue(compConfigs.size() > 0);
        GwtConfigComponent gwtConfigComponent = compConfigs.get(0);
        assertEquals("testPid", gwtConfigComponent.getComponentId());
    }

    @Test
    public void testFindComponentConfiguration() throws GwtKuraException {
        List<GwtConfigComponent> compConfigs = gwtCompService.findComponentConfiguration(xsrfToken, "testPid");
        assertTrue(compConfigs.size() > 0);
        GwtConfigComponent gwtConfigComponent = compConfigs.get(0);
        assertEquals("testPid", gwtConfigComponent.getComponentId());
    }

    @Test
    public void testUpdateComponentConfiguration() throws GwtKuraException, KuraException {
        GwtConfigParameter param = new GwtConfigParameter();
        param.setMax("0");
        param.setMin("100");
        List<GwtConfigParameter> parameters = new ArrayList<>();
        parameters.add(param);
        GwtConfigComponent gwtCompConfig = new GwtConfigComponent();
        gwtCompConfig.setComponentName("testComponent");
        gwtCompConfig.setComponentId("testPid");
        gwtCompConfig.setParameters(parameters);
        gwtCompService.updateComponentConfiguration(xsrfToken, gwtCompConfig);
        verify(configurationSvc).updateConfiguration(eq("testPid"), any());
    }

    @Test
    public void testCreateFactoryComponent() throws GwtKuraException, KuraException {
        gwtCompService.createFactoryComponent(xsrfToken, "factoryPid", "testPid");
        verify(configurationSvc).createFactoryConfiguration("factoryPid", "testPid", null, true);
    }

    @Test
    public void testDeleteFactoryConfiguration() throws GwtKuraException, KuraException {
        gwtCompService.deleteFactoryConfiguration(xsrfToken, "testPid", true);
        verify(configurationSvc).deleteFactoryConfiguration("testPid", true);
    }

    @Test
    public void testFindFactoryComponents() throws GwtKuraException {
        // gwtCompService.findFactoryComponents(xsrfToken); // TODO
    }

    @Test
    public void testFindWireComponentConfigurationFromPid() throws GwtKuraException {
        GwtConfigComponent wireConfigComp = gwtCompService.findWireComponentConfigurationFromPid(xsrfToken, "testPid",
                "factoryPid", new HashMap<>());
        assertNotNull(wireConfigComp);
    }

    @Test
    public void testUpdateProperties() throws GwtKuraException {
        boolean result = gwtCompService.updateProperties(xsrfToken, "testPid", new HashMap<>());
        assertTrue(result);
    }

}
