package com.walmartlabs.concord.it.server;

/*-
 * *****
 * Concord
 * -----
 * Copyright (C) 2017 Wal-Mart Store, Inc.
 * -----
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =====
 */

import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.walmartlabs.concord.server.api.process.ProcessEntry;
import com.walmartlabs.concord.server.api.process.ProcessResource;
import com.walmartlabs.concord.server.api.process.ProcessStatus;
import com.walmartlabs.concord.server.api.process.StartProcessResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.walmartlabs.concord.it.common.ITUtils.archive;
import static com.walmartlabs.concord.it.common.ServerClient.assertLog;
import static com.walmartlabs.concord.it.common.ServerClient.waitForCompletion;
import static org.junit.Assert.assertEquals;

public class HttpTaskIT extends AbstractServerIT {

    public static final String mockHttpAuthToken = "Y249dGVzdDpwYXNzd29yZA==";
    public static final String mockHttpAuthUser = "cn=test";
    public static final String mockHttpAuthPassword = "password";
    public static final String mockHttpBaseUrl = "http://localhost:";
    public static final Integer mockHttpPort = 8089;
    public static final String mockHttpPathToken = "/token";
    public static final String mockHttpPathPassword = "/password";

    @Rule
    public WireMockRule rule = new WireMockRule(WireMockConfiguration.options().port(mockHttpPort).notifier(new ConsoleNotifier(true)));

    @Before
    public void setup() {

        stubForGetSecureEndpoint(mockHttpAuthUser, mockHttpAuthPassword,mockHttpPathPassword);
        stubForPostSecureEndpoint(mockHttpAuthUser, mockHttpAuthPassword,mockHttpPathPassword);
        stubForGetSecureTokenEndpoint(mockHttpAuthToken,mockHttpPathToken);
        stubForPostSecureTokenEndpoint(mockHttpAuthToken,mockHttpPathToken);

    }

    @After
    public void tearDown() {
        rule.shutdownServer();
    }

    @Test(timeout = 30000)
    public void testGetAsString() throws Exception {
        URI dir = HttpTaskIT.class.getResource("httpGetAsString").toURI();
        byte[] payload = archive(dir, ITConstants.DEPENDENCIES_DIR);

        ProcessResource processResource = proxy(ProcessResource.class);
        StartProcessResponse spr = processResource.start(new ByteArrayInputStream(payload), null, false, null);

        ProcessEntry pir = waitForCompletion(processResource, spr.getInstanceId());
        assertEquals(ProcessStatus.FINISHED, pir.getStatus());

        byte[] ab = getLog(pir.getLogFileName());
        assertLog(".*Success response.*", ab);
    }

    @Test(timeout = 30000)
    public void testGet() throws Exception {
        URI dir = HttpTaskIT.class.getResource("httpGet").toURI();
        byte[] payload = archive(dir, ITConstants.DEPENDENCIES_DIR);

        ProcessResource processResource = proxy(ProcessResource.class);
        StartProcessResponse spr = processResource.start(new ByteArrayInputStream(payload), null, false, null);

        ProcessEntry pir = waitForCompletion(processResource, spr.getInstanceId());
        assertEquals(ProcessStatus.FINISHED, pir.getStatus());

        byte[] ab = getLog(pir.getLogFileName());
        assertLog(".*Success response.*", ab);
        assertLog(".*Out Response: true*", ab);
    }

    @Test(timeout = 30000)
    public void testGetWithAuthUsingPassword() throws Exception {

        URI dir = HttpTaskIT.class.getResource("httpGetWithAuthUsingPassword").toURI();
        byte[] payload = archive(dir, ITConstants.DEPENDENCIES_DIR);

        Map<String, Object> input = new HashMap<>();
        input.put("archive", payload);
        input.put("arguments.user", mockHttpAuthUser);
        input.put("arguments.password", mockHttpAuthPassword);
        input.put("arguments.url", mockHttpBaseUrl+mockHttpPort+mockHttpPathPassword);
        StartProcessResponse spr = start(input);

        ProcessResource processResource = proxy(ProcessResource.class);

        ProcessEntry pir = waitForCompletion(processResource, spr.getInstanceId());
        assertEquals(ProcessStatus.FINISHED, pir.getStatus());

        byte[] ab = getLog(pir.getLogFileName());
        assertLog(".*Success response.*", ab);
        assertLog(".*Out Response: true*", ab);
    }

    @Test(timeout = 30000)
    public void testGetWithAuthUsingToken() throws Exception {
        URI dir = HttpTaskIT.class.getResource("httpGetWithAuthUsingToken").toURI();
        byte[] payload = archive(dir, ITConstants.DEPENDENCIES_DIR);

        Map<String, Object> input = new HashMap<>();
        input.put("archive", payload);
        input.put("arguments.authToken", mockHttpAuthToken);
        input.put("arguments.url", mockHttpBaseUrl+mockHttpPort+mockHttpPathToken);
        StartProcessResponse spr = start(input);

        ProcessResource processResource = proxy(ProcessResource.class);

        ProcessEntry pir = waitForCompletion(processResource, spr.getInstanceId());
        assertEquals(ProcessStatus.FINISHED, pir.getStatus());

        byte[] ab = getLog(pir.getLogFileName());
        assertLog(".*Success response.*", ab);
        assertLog(".*Out Response: true*", ab);
    }

    @Test(timeout = 30000)
    public void testPost() throws Exception {
        URI dir = HttpTaskIT.class.getResource("httpPost").toURI();
        byte[] payload = archive(dir, ITConstants.DEPENDENCIES_DIR);

        Map<String, Object> input = new HashMap<>();
        input.put("archive", payload);
        input.put("arguments.user", mockHttpAuthUser);
        input.put("arguments.password", mockHttpAuthPassword);
        input.put("arguments.url", mockHttpBaseUrl+mockHttpPort+mockHttpPathPassword);
        StartProcessResponse spr = start(input);

        ProcessResource processResource = proxy(ProcessResource.class);

        ProcessEntry pir = waitForCompletion(processResource, spr.getInstanceId());
        assertEquals(ProcessStatus.FINISHED, pir.getStatus());

        byte[] ab = getLog(pir.getLogFileName());
        assertLog(".*Success response.*", ab);
        assertLog(".*Out Response: true*", ab);
    }

    @Test(timeout = 30000)
    public void testPostWithAuthUsingToken() throws Exception {
        URI dir = HttpTaskIT.class.getResource("httpPostWithAuthUsingToken").toURI();
        byte[] payload = archive(dir, ITConstants.DEPENDENCIES_DIR);

        Map<String, Object> input = new HashMap<>();
        input.put("archive", payload);
        input.put("arguments.authToken", mockHttpAuthToken);
        input.put("arguments.url", mockHttpBaseUrl+mockHttpPort+mockHttpPathToken);
        StartProcessResponse spr = start(input);

        ProcessResource processResource = proxy(ProcessResource.class);

        ProcessEntry pir = waitForCompletion(processResource, spr.getInstanceId());
        assertEquals(ProcessStatus.FINISHED, pir.getStatus());

        byte[] ab = getLog(pir.getLogFileName());
        assertLog(".*Success response.*", ab);
        assertLog(".*Out Response: true*", ab);
    }

    @Test(timeout = 30000)
    public void testGetWithInvalidUrl() throws Exception {
        URI dir = HttpTaskIT.class.getResource("httpGetWithInvalidUrl").toURI();
        byte[] payload = archive(dir, ITConstants.DEPENDENCIES_DIR);

        ProcessResource processResource = proxy(ProcessResource.class);
        StartProcessResponse spr = processResource.start(new ByteArrayInputStream(payload), null, false, null);

        ProcessEntry pir = waitForCompletion(processResource, spr.getInstanceId());
        assertEquals(ProcessStatus.FAILED, pir.getStatus());

        byte[] ab = getLog(pir.getLogFileName());
        assertLog(".*server not exists.*", ab);

    }

    private void stubForGetSecureEndpoint(String user, String password, String url) {
        rule.stubFor(get(urlEqualTo(url))
                .withBasicAuth(user, password)
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("Authorized"))
        );
    }

    private void stubForGetSecureTokenEndpoint(String authToken, String url) {
        rule.stubFor(get(urlEqualTo(url))
                .withHeader("Authorization", equalTo("Basic "+authToken))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("Authorized"))
        );
    }

    private void stubForPostSecureEndpoint(String user, String password, String url) {
        rule.stubFor(post(urlEqualTo(url))
                .withBasicAuth(user, password)
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("Authorized"))
        );
    }

    private void stubForPostSecureTokenEndpoint(String authToken, String url) {
        rule.stubFor(post(urlEqualTo(url))
                .withHeader("Authorization", equalTo("Basic "+authToken))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("Authorized"))
        );
    }
}