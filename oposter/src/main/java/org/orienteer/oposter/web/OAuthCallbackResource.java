package org.orienteer.oposter.web;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.AbstractResource;
import org.apache.wicket.request.resource.SharedResourceReference;
import org.apache.wicket.util.string.StringValue;
import org.orienteer.core.MountPath;
import org.orienteer.core.OrienteerWebApplication;
import org.orienteer.core.dao.DAO;
import static org.orienteer.core.util.CommonUtils.localize;
import org.orienteer.core.web.ODocumentPage;
import org.orienteer.logger.OLogger;
import org.orienteer.oposter.model.IOAuthReciever;

import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.record.impl.ODocument;

/**
 * Resource for recieving OAuth callbacks
 */
@MountPath("/op/callback")
public class OAuthCallbackResource extends AbstractResource {

	@Override
    protected ResourceResponse newResourceResponse(Attributes attributes) {
        ResourceResponse response = new ResourceResponse();
        if (response.dataNeedsToBeWritten(attributes)) {
        	OrienteerWebApplication app = OrienteerWebApplication.get();
        	String rid = "#"+attributes.getParameters().get("rid").toString();
        	if(!ORecordId.isA(rid)) {
        		response.setError(HttpServletResponse.SC_BAD_REQUEST, "Rid of requestor document is not specified");
        	} else {
        		ODocument doc = new ORecordId(rid).getRecord();
        		ODocumentPage targetPage = new ODocumentPage(doc);
        		WebRequest request = (WebRequest) attributes.getRequest();
        		StringValue errorSV = request.getRequestParameters().getParameterValue("error");
        		StringValue errorDescriptionSV = request.getRequestParameters().getParameterValue("error_description");
        		if(!errorSV.isEmpty() || !errorDescriptionSV.isEmpty()) {
        			targetPage.error(localize("error.oauthcallback", "error", errorSV.toOptionalString(),
												   					 "errorDescription", errorDescriptionSV.toOptionalString()));
        		} else {
					try {
						IOAuthReciever reciever = DAO.provide(IOAuthReciever.class, doc);
						reciever.callback(request, targetPage);
					} catch (Exception e) {
						targetPage.error(e.getMessage());
						OLogger.log(e, doc.getIdentity().toString());
					}
        		}
//        		RequestCycle.get().setResponsePage(targetPage);
        		response.setWriteCallback(createWriteCallback(targetPage));
        	}
        }
        return response;
    }

    private WriteCallback createWriteCallback(final WebPage targetPage) {
        return new WriteCallback() {
            @Override
            public void writeData(Attributes attributes) throws IOException {
            	RequestCycle.get().setResponsePage(targetPage);
//                throw new RedirectToUrlException("/browse/" + OfferRequest.CLASS_NAME);
            }
        };
    }
    
    public static String urlFor(IOAuthReciever dao) {
    	return urlFor(DAO.asDocument(dao));
    }
    
    public static String urlFor(ODocument document) {
		return RequestCycle.get().getUrlRenderer()
				.renderFullUrl(Url.parse(
						RequestCycle.get().urlFor(
								new SharedResourceReference(OAuthCallbackResource.class.getName()),
								new PageParameters().add("rid", document.getIdentity().toString().substring(1)))))
							.toString();
    }

}
