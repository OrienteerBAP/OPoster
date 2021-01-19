package org.orienteer.oposter.model;

import static org.orienteer.core.util.CommonUtils.localize;

import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.util.string.StringValue;
import org.orienteer.core.web.ODocumentPage;

/**
 * Interface (non-DAO) for classes which can receive code as part of OAuth
 */
public interface IOAuthReciever {
	public default void callback(WebRequest request, ODocumentPage targetPage) throws Exception {
		StringValue codeSV = request.getRequestParameters().getParameterValue("code");
		if(codeSV.isEmpty()) {
			targetPage.error(localize("error.oauthcallback.nocode"));
		}
		codeObtained(codeSV.toString());
	}
	
	public default void codeObtained(String code) throws Exception {
		throw new IllegalStateException("Either 'codeObtained' or 'callback' should be overrided");
	}
}
