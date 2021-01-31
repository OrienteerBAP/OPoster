package org.orienteer.oposter.ok;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.List;

import com.github.scribejava.apis.odnoklassniki.OdnoklassnikiOAuthService;
import com.github.scribejava.core.builder.api.DefaultApi20;
import com.github.scribejava.core.httpclient.HttpClient;
import com.github.scribejava.core.httpclient.HttpClientConfig;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Parameter;
import com.github.scribejava.core.model.ParameterList;

/**
 * Temporal solution while https://github.com/scribejava/scribejava/pull/998 is awaiting to be merged
 */
public class FixedOdnoklassnikiOAuthService extends OdnoklassnikiOAuthService {

	public FixedOdnoklassnikiOAuthService(DefaultApi20 api, String apiKey, String apiSecret, String callback,
			String defaultScope, String responseType, String userAgent, HttpClientConfig httpClientConfig,
			HttpClient httpClient) {
		super(api, apiKey, apiSecret, callback, defaultScope, responseType, userAgent, httpClientConfig, httpClient);
	}
	
	@Override
	public void signRequest(String accessToken, OAuthRequest request) {
		//sig = lower(md5( sorted_request_params_composed_string + md5(access_token + application_secret_key)))
        final String tokenDigest = md5(accessToken + getApiSecret());

        final ParameterList queryParams = request.getQueryStringParams();
        queryParams.addAll(request.getBodyParams());
        final List<Parameter> allParams = queryParams.getParams();

        Collections.sort(allParams);

        final StringBuilder stringParams = new StringBuilder();
        for (Parameter param : allParams) {
            stringParams.append(param.getKey())
                    .append('=')
                    .append(param.getValue());
        }

        final String sigSource = stringParams + tokenDigest;
        request.addQuerystringParameter("sig", md5(sigSource).toLowerCase());

        super.signRequest(accessToken, request);
	}

}
