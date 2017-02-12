package com.walmartlabs.concord.it.server;

import com.walmartlabs.concord.server.api.process.ProcessResource;
import com.walmartlabs.concord.server.api.process.ProcessStatusResponse;
import com.walmartlabs.concord.server.api.process.StartProcessResponse;
import com.walmartlabs.concord.server.api.project.CreateProjectRequest;
import com.walmartlabs.concord.server.api.project.CreateProjectResponse;
import com.walmartlabs.concord.server.api.project.CreateRepositoryRequest;
import com.walmartlabs.concord.server.api.project.ProjectResource;
import com.walmartlabs.concord.server.api.security.secret.SecretResource;
import com.walmartlabs.concord.server.api.security.secret.UsernamePasswordRequest;
import com.walmartlabs.concord.server.api.template.TemplateResource;
import org.junit.Ignore;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Ignore
public class AnsibleGitProjectIT extends AbstractServerIT {

    @Test
    public void test() throws Exception {
        String templateName = "template#" + System.currentTimeMillis();
        String projectName = "project#" + System.currentTimeMillis();
        String secretName = "secret#" + System.currentTimeMillis();
        String repoName = "repo#" + System.currentTimeMillis();
        String repoUrl = "https://gecgithub01.walmart.com/devtools/concord-ansible-example.git";
        String repoBranch = "it";
        String entryPoint = URLEncoder.encode(projectName + ":" + repoName, "UTF-8");

        // ---

        TemplateResource templateResource = proxy(TemplateResource.class);
        templateResource.create(templateName, fsResource(ITConstants.TEMPLATES_DIR + "/ansible-template.zip"));

        // ---

        ProjectResource projectResource = proxy(ProjectResource.class);
        CreateProjectResponse cpr = projectResource.create(new CreateProjectRequest(projectName, Collections.singleton(templateName), null));

        // ---

        SecretResource secretResource = proxy(SecretResource.class);
        secretResource.addUsernamePassword(secretName, new UsernamePasswordRequest("username", "password".toCharArray()));

        // ---

        projectResource.createRepository(projectName, new CreateRepositoryRequest(repoName, repoUrl, repoBranch, secretName));

        // ---

        Map<String, InputStream> input = new HashMap<>();
        input.put("request", resource("ansiblegitproject/request.json"));
        input.put("inventory", resource("ansiblegitproject/inventory.ini"));
        StartProcessResponse spr = start(entryPoint, input);

        // ---

        ProcessResource processResource = proxy(ProcessResource.class);
        ProcessStatusResponse psr = waitForCompletion(processResource, spr.getInstanceId());

        // ---

        byte[] ab = getLog(psr);
        assertLog(".*Hello, world.*", ab);
    }

    private static InputStream resource(String path) {
        return AnsibleGitProjectIT.class.getResourceAsStream(path);
    }

    private static InputStream fsResource(String path) throws FileNotFoundException {
        return new FileInputStream(path);
    }
}
