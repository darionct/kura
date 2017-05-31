/*******************************************************************************
 * Copyright (c) 2017 Eurotech and/or its affiliates and others
 *
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License v1.0
 *   which accompanies this distribution, and is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/

package org.eclipse.kura.web.server;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.eclipse.kura.KuraException;
import org.eclipse.kura.certificate.CertificatesService;
import org.eclipse.kura.core.testutil.TestUtil;
import org.eclipse.kura.ssl.SslManagerService;
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

public class GwtCertificatesServiceImplTest {

    private static SslManagerService sslManager;
    private static CertificatesService certService;

    private GwtCertificatesServiceImpl gwtCertificatesService;
    private GwtXSRFToken xsrfToken;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        sslManager = mock(SslManagerService.class);
        ServiceReference<SslManagerService> svcRefSslManager = mock(ServiceReference.class);

        certService = mock(CertificatesService.class);
        ServiceReference<CertificatesService> svcRefCertificate = mock(ServiceReference.class);

        BundleContext bundleContext = mock(BundleContext.class);
        when(bundleContext.getServiceReference(SslManagerService.class)).thenReturn(svcRefSslManager);
        when(bundleContext.getService(svcRefSslManager)).thenReturn(sslManager);
        when(bundleContext.getServiceReference(CertificatesService.class)).thenReturn(svcRefCertificate);
        when(bundleContext.getService(svcRefCertificate)).thenReturn(certService);

        ComponentContext componentContext = mock(ComponentContext.class);
        when(componentContext.getBundleContext()).thenReturn(bundleContext);

        TestUtil.setFieldValue(new Console(), "s_context", componentContext); // set static field
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        xsrfToken = new GwtXSRFToken();
        xsrfToken.setToken("token");

        gwtCertificatesService = new GwtCertificatesServiceImpl();
        ThreadLocal<HttpServletRequest> requestVar = new ThreadLocal<HttpServletRequest>();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute(GwtSecurityTokenServiceImpl.XSRF_TOKEN_KEY)).thenReturn(xsrfToken);
        when(request.getSession()).thenReturn(session);
        requestVar.set(request);
        TestUtil.setFieldValue(gwtCertificatesService, "perThreadRequest", requestVar);

    }

    @After
    public void tearDown() throws Exception {
        TestUtil.setFieldValue(gwtCertificatesService, "perThreadRequest", null);
        gwtCertificatesService = null;
        xsrfToken = null;
    }

    @Test
    public void testStorePublicPrivateKeys() throws IOException, GwtKuraException, GeneralSecurityException {

        byte[] publicKey = Files.readAllBytes(Paths.get("src/test/resources/chain.p7b"));
        byte[] privateKey = Files.readAllBytes(Paths.get("src/test/resources/priv.key"));

        Integer result = gwtCertificatesService.storePublicPrivateKeys(xsrfToken,
                new String(privateKey, StandardCharsets.UTF_8), new String(publicKey, StandardCharsets.UTF_8), "test",
                "test");
        assertEquals(new Integer(1), result);
        verify(sslManager).installPrivateKey(any(), any(), any(), any());
    }

    @Test
    public void testStoreSSLPublicChain() throws GwtKuraException, IOException, GeneralSecurityException {
        byte[] publicKey = Files.readAllBytes(Paths.get("src/test/resources/chain.p7b"));
        Integer result = gwtCertificatesService.storeSSLPublicChain(xsrfToken,
                new String(publicKey, StandardCharsets.UTF_8), "test");
        assertEquals(new Integer(1), result);
        verify(sslManager).installTrustCertificate(any(), any());
    }

    @Test
    public void testStoreApplicationPublicChain() throws GwtKuraException, IOException, KuraException {
        byte[] publicKey = Files.readAllBytes(Paths.get("src/test/resources/chain.p7b"));
        Integer result = gwtCertificatesService.storeApplicationPublicChain(xsrfToken, new String(publicKey, StandardCharsets.UTF_8),
                "test");
        assertEquals(new Integer(1), result);
        verify(certService).storeCertificate(any(), any());
    }

}
