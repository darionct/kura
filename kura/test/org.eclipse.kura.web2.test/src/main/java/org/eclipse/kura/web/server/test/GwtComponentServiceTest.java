package org.eclipse.kura.web.server.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.kura.KuraException;
import org.eclipse.kura.web.shared.service.GwtComponentService;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GwtComponentServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(GwtComponentServiceTest.class);

    private static CountDownLatch dependencyLatch = new CountDownLatch(1);

    private static GwtComponentService gwtComponentSvc;

    @BeforeClass
    public static void setUpClass() throws Exception {
        try {
            boolean ok = dependencyLatch.await(10, TimeUnit.SECONDS);
            assertTrue("Dependencies OK", ok);
        } catch (final InterruptedException e) {
            fail("OSGi dependencies unfulfilled");
        }
    }

    @Before
    public void setup() throws KuraException {
        // TODO
    }

    protected void bindGwtComponentService(final GwtComponentService svc) {
        if (gwtComponentSvc == null) {
            gwtComponentSvc = svc;
            dependencyLatch.countDown();
        }
    }

    protected void unbindGwtComponentService(final GwtComponentService svc) {
        if (gwtComponentSvc == svc) {
            gwtComponentSvc = null;
        }
    }

    @Test
    public void testServiceBound() {
        assertNotNull("FirewallConfiguration service should not be null.", gwtComponentSvc);
    }

    @Test
    public void testFindTrackedPids() {
        fail("Not yet implemented");
    }

    @Test
    public void testFindServicesConfigurations() {
        fail("Not yet implemented");
    }

    @Test
    public void testFindFilteredComponentConfigurations() {
        fail("Not yet implemented");
    }

    @Test
    public void testFindFilteredComponentConfiguration() {
        fail("Not yet implemented");
    }

    @Test
    public void testFindComponentConfigurations() {
        fail("Not yet implemented");
    }

    @Test
    public void testFindComponentConfiguration() {
        fail("Not yet implemented");
    }

    @Test
    public void testUpdateComponentConfiguration() {
        fail("Not yet implemented");
    }

    @Test
    public void testCreateFactoryComponent() {
        fail("Not yet implemented");
    }

    @Test
    public void testDeleteFactoryConfiguration() {
        fail("Not yet implemented");
    }

    @Test
    public void testFindFactoryComponents() {
        fail("Not yet implemented");
    }

    @Test
    public void testFindWireComponentConfigurationFromPid() {
        fail("Not yet implemented");
    }

    @Test
    public void testUpdateProperties() {
        fail("Not yet implemented");
    }

}
