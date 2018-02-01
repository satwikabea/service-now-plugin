package org.jenkinsci.plugins.servicenow.util;

import com.cloudbees.plugins.credentials.Credentials;
import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.domains.URIRequirementBuilder;
import com.datapipe.jenkins.vault.credentials.VaultAppRoleCredential;
import com.datapipe.jenkins.vault.credentials.VaultCredential;
import hudson.model.Item;
import hudson.security.ACL;
import org.jenkinsci.plugins.servicenow.model.VaultConfiguration;

import java.util.Map;

import static org.jenkinsci.plugins.servicenow.UtilsKt.readVaultData;

public class CredentialsUtil {

    public static Credentials findCredentials(String url, String credentialId, VaultConfiguration vaultConfiguration, Item project) {
        Credentials credentials = null;
        if(vaultConfiguration != null) {
            credentials = CredentialsMatchers.firstOrNull(
                    com.cloudbees.plugins.credentials.CredentialsProvider.lookupCredentials(
                            VaultAppRoleCredential.class,
                            project.getParent(), ACL.SYSTEM,
                            URIRequirementBuilder.fromUri(url).build()),
                    CredentialsMatchers.withId(credentialId));
        }
        if(credentials == null) {
            credentials = CredentialsMatchers.firstOrNull(
                    com.cloudbees.plugins.credentials.CredentialsProvider.lookupCredentials(
                            StandardUsernamePasswordCredentials.class,
                            project.getParent(), ACL.SYSTEM,
                            URIRequirementBuilder.fromUri(url).build()),
                    CredentialsMatchers.withId(credentialId));
        }
        return credentials;
    }

    public static org.apache.http.auth.Credentials readCredentials(Credentials credentials, VaultConfiguration vaultConfiguration) {
        org.apache.http.auth.Credentials creds = null;
        if(credentials instanceof StandardUsernamePasswordCredentials) {
            creds = new org.apache.http.auth.UsernamePasswordCredentials(((StandardUsernamePasswordCredentials)credentials).getUsername(), ((StandardUsernamePasswordCredentials)credentials).getPassword().getPlainText());
        }
        if(credentials instanceof VaultAppRoleCredential) {
            Map<String, String> vaultData = readVaultData(vaultConfiguration, (VaultCredential) credentials);
            creds = new org.apache.http.auth.UsernamePasswordCredentials(vaultData.get("username"), vaultData.get("password"));
        }
        return creds;
    }




}
