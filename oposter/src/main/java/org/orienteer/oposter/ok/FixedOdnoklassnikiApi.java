package org.orienteer.oposter.ok;

import com.github.scribejava.apis.OdnoklassnikiApi;
import com.github.scribejava.apis.odnoklassniki.OdnoklassnikiOAuthService;
import com.github.scribejava.core.httpclient.HttpClient;
import com.github.scribejava.core.httpclient.HttpClientConfig;

/**
 * Temporal solution while https://github.com/scribejava/scribejava/pull/998 is awaiting to be merged
 */
public class FixedOdnoklassnikiApi extends OdnoklassnikiApi {
	
	private static class InstanceHolder {
        private static final FixedOdnoklassnikiApi INSTANCE = new FixedOdnoklassnikiApi();
    }

    public static FixedOdnoklassnikiApi instance() {
        return InstanceHolder.INSTANCE;
    }

    @Override
    public OdnoklassnikiOAuthService createService(String apiKey, String apiSecret, String callback,
            String defaultScope, String responseType, String userAgent, HttpClientConfig httpClientConfig,
            HttpClient httpClient) {
        return new FixedOdnoklassnikiOAuthService(this, apiKey, apiSecret, callback, defaultScope, responseType, userAgent,
                httpClientConfig, httpClient);
    }
}
