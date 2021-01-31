package org.orienteer.oposter.ok;

import static org.orienteer.core.util.CommonUtils.toMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.util.Strings;
import org.orienteer.core.component.visualizer.UIVisualizersRegistry;
import org.orienteer.core.dao.DAO;
import org.orienteer.core.dao.DAOField;
import org.orienteer.core.dao.DAOOClass;
import org.orienteer.core.dao.ODocumentWrapperProvider;
import org.orienteer.logger.OLogger;
import org.orienteer.oposter.model.IChannel;
import org.orienteer.oposter.model.IContent;
import org.orienteer.oposter.model.IImageAttachment;
import org.orienteer.oposter.model.IOAuthReciever;
import org.orienteer.oposter.model.IPlatformApp;
import org.orienteer.oposter.web.OAuthCallbackResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.openjson.JSONArray;
import com.github.openjson.JSONObject;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.httpclient.multipart.FileByteArrayBodyPartPayload;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.google.inject.ProvidedBy;
import com.orientechnologies.orient.core.record.impl.ODocument;

/**
 * {@link IPlatformApp} for Odnoklassniki 
 */
@ProvidedBy(ODocumentWrapperProvider.class)
@DAOOClass(value = IOkApp.CLASS_NAME, orderOffset = 100)
public interface IOkApp extends IPlatformApp {
	public static final Logger LOG = LoggerFactory.getLogger(IOkApp.class);
	public static final String CLASS_NAME = "OPOkApp";
	
	@DAOField(notNull = true)
	public Long getAppId();
	public void setAppId(Long value);
	
	@DAOField(notNull = true)
	public String getPublicKey();
	public void setPublicKey(String value);
	
	@DAOField(notNull = true, visualization = UIVisualizersRegistry.VISUALIZER_PASSWORD)
	public String getSecretKey();
	public void setSecretKey(String value);
	
	@Override
	public default boolean send(IChannel channel, IContent content) {
		if(channel instanceof IOkChannel) {
			IOkChannel account = (IOkChannel) channel;
			try(OAuth20Service service = getService(null)) {
				String groupId = account.getGroupId();
				JSONObject attachment = new JSONObject();
				JSONObject text = new JSONObject();
				text.put("type", "text");
				text.put("text", content.getContent());
				JSONArray media = new JSONArray();
				media.put(text);
				if(content.hasImages()) {
					List<IImageAttachment> images = content.getImages();
					// 1. Request Upload URL for images
					Map<String, Object> params = new HashMap<>();
					params.put("count", images.size());
					params.put("sizes", images.stream().map(i ->i.getData()==null?"0":Integer.toString(i.getData().length))
											.collect(Collectors.joining(",")));
					if(groupId!=null) params.put("gid", groupId);
					JSONObject uploadUrlResult = new JSONObject(invokeOKMethod(service, account.getAccessToken(), Verb.GET, "photosV2.getUploadUrl", params, null).getBody());
					LOG.info("UploadUrl Request results: "+uploadUrlResult);
					// 2. Upload images
					final OAuthRequest uploadRequest = new OAuthRequest(Verb.POST, uploadUrlResult.getString("upload_url"));
					uploadRequest.initMultipartPayload();
					for (int i=0; i<images.size(); i++) {
						IImageAttachment image = images.get(i);
						uploadRequest.addFileByteArrayBodyPartPayloadInMultipartPayload(image.getContentType(), image.getData(), "pic"+(i+1), image.getName());
					}
					JSONObject uploadResponse = new JSONObject(service.execute(uploadRequest).getBody());
					LOG.info("Upload Response: "+uploadResponse);
					// 3. Commit Uploading - not needed
					// Adding to attachment
					
					JSONObject photos = new JSONObject();
					photos.put("type", "photo");
					JSONArray photosList = new JSONArray();
					JSONObject photosTokens = uploadResponse.getJSONObject("photos");
					for (String photoId : photosTokens.keySet()) {
						String token = photosTokens.getJSONObject(photoId).getString("token");
						photosList.put(new JSONObject().put("id", token));
					}
					photos.put("list", photosList);
					media.put(photos);
				}
				attachment.put("media", media);
				
				Map<String, String> params = Strings.isEmpty(groupId)?toMap("type", "USER"):toMap("gid", groupId, "type", "GROUP_THEME");
				Response response = invokeOKMethod(service, account.getAccessToken(), Verb.POST, "mediatopic.post", params, toMap("attachment", attachment));
				response.getBody(); //Make sure that body was read
				LOG.info("Response is: "+response);
				return true;
			} catch (Exception ex) {
				OLogger.log(ex);
			}
		}
		return false;
	}
	public default OAuth20Service getService(IOAuthReciever reciever) {
		
		ODocument reciverDoc = DAO.asDocument(reciever!=null?reciever:this); 
		return new ServiceBuilder(getAppId().toString())
	                .apiSecret(getSecretKey())
	                .defaultScope("PUBLISH_TO_STREAM;VALUABLE_ACCESS;LONG_ACCESS_TOKEN;PHOTO_CONTENT;GROUP_CONTENT") // replace with desired scope
	                .callback(OAuthCallbackResource.urlFor(reciverDoc))
	                .build(FixedOdnoklassnikiApi.instance());
	}
	
	public default Response invokeOKMethod(OAuth20Service service, String accessToken, Verb verb, String method, Map<String, ?> queryParams, Map<String, ?> bodyParams) throws Exception {
		final OAuthRequest request = new OAuthRequest(verb, "https://api.ok.ru/api/"+method.replace('.', '/'));
		request.addQuerystringParameter("application_key", getPublicKey());
		
		if(queryParams!=null && !queryParams.isEmpty()) {
			for (Map.Entry<String, ?> entry : queryParams.entrySet()) {
				request.addQuerystringParameter(entry.getKey(), entry.getValue().toString());
			}
		}
		if(bodyParams!=null && !bodyParams.isEmpty()) {
			for (Map.Entry<String, ?> entry : bodyParams.entrySet()) {
				if(entry.getValue() instanceof FileByteArrayBodyPartPayload) {
					request.addFileByteArrayBodyPartPayloadInMultipartPayload((FileByteArrayBodyPartPayload)entry.getValue());
				} else if(entry.getValue() instanceof IImageAttachment) {
					IImageAttachment image = (IImageAttachment)entry.getValue();
					request.addFileByteArrayBodyPartPayloadInMultipartPayload(image.getContentType(), image.getData(), entry.getKey(), image.getName());
				} else {
					request.addBodyParameter(entry.getKey(), entry.getValue().toString());
				}
			}
		}
        service.signRequest(accessToken, request);
        
		return service.execute(request);
	}
	
}
