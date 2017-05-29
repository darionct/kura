/*******************************************************************************
 * Copyright (c) 2017 Eurotech and/or its affiliates and others
 *
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License v1.0
 *   which accompanies this distribution, and is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/

package org.eclipse.kura.web.server;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.eclipse.kura.KuraException;
import org.eclipse.kura.core.testutil.TestUtil;
import org.eclipse.kura.security.SecurityService;
import org.eclipse.kura.web.Console;
import org.eclipse.kura.web.shared.GwtKuraException;
import org.eclipse.kura.web.shared.model.GwtXSRFToken;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;

public class GwtSecurityServiceImplTest {

    private static SecurityService securityService;

    private GwtSecurityServiceImpl gwtSecurityService;
    private GwtXSRFToken xsrfToken;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        securityService = mock(SecurityService.class);
        ServiceReference<SecurityService> svcRef = mock(ServiceReference.class);

        BundleContext bundleContext = mock(BundleContext.class);
        when(bundleContext.getServiceReference(SecurityService.class)).thenReturn(svcRef);
        when(bundleContext.getService(svcRef)).thenReturn(securityService);

        ComponentContext componentContext = mock(ComponentContext.class);
        when(componentContext.getBundleContext()).thenReturn(bundleContext);

        TestUtil.setFieldValue(new Console(), "s_context", componentContext); // set static field
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        gwtSecurityService = new GwtSecurityServiceImpl();

        xsrfToken = new GwtXSRFToken();
        xsrfToken.setToken("token");

        ThreadLocal<HttpServletRequest> requestVar = new ThreadLocal<HttpServletRequest>();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute(GwtSecurityTokenServiceImpl.XSRF_TOKEN_KEY)).thenReturn(xsrfToken);
        when(request.getSession()).thenReturn(session);
        requestVar.set(request);
        TestUtil.setFieldValue(gwtSecurityService, "perThreadRequest", requestVar);
    }

    @After
    public void tearDown() throws Exception {
        TestUtil.setFieldValue(gwtSecurityService, "perThreadRequest", null);
        gwtSecurityService = null;
        xsrfToken = null;
    }

    @Test
    public void testIsSecurityServiceAvailable() {
        Boolean securityServiceAvailable = gwtSecurityService.isSecurityServiceAvailable();
        assertTrue(securityServiceAvailable);
    }

    @Test
    public void testIsDebugMode() {
        Boolean debugMode = gwtSecurityService.isDebugMode();
        verify(securityService).isDebugEnabled();
        assertFalse(debugMode);
    }

    @Test
    public void testReloadSecurityPolicyFingerprint() throws GwtKuraException, KuraException {
        gwtSecurityService.reloadSecurityPolicyFingerprint(xsrfToken);
        verify(securityService).reloadSecurityPolicyFingerprint();
    }

    @Test
    public void testReloadCommandLineFingerprint() throws GwtKuraException, KuraException {
        gwtSecurityService.reloadCommandLineFingerprint(xsrfToken);
        verify(securityService).reloadCommandLineFingerprint();
    }

}
