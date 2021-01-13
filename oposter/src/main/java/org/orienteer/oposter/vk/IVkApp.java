package org.orienteer.oposter.vk;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.MetaDataKey;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.flow.RedirectToUrlException;
import org.orienteer.core.OrienteerWebApplication;
import org.orienteer.core.component.BootstrapType;
import org.orienteer.core.component.FAIconType;
import org.orienteer.core.dao.DAO;
import org.orienteer.core.dao.DAOField;
import org.orienteer.core.dao.DAOOClass;
import org.orienteer.core.dao.ODocumentWrapperProvider;
import org.orienteer.core.method.IMethodContext;
import org.orienteer.core.method.OFilter;
import org.orienteer.core.method.OMethod;
import org.orienteer.core.method.filters.PlaceFilter;
import org.orienteer.core.method.filters.WidgetTypeFilter;
import org.orienteer.logger.OLogger;
import org.orienteer.oposter.model.IChannel;
import org.orienteer.oposter.model.IContent;
import org.orienteer.oposter.model.IImageAttachment;
import org.orienteer.oposter.model.IOAuthReciever;
import org.orienteer.oposter.model.IPlatformApp;
import org.orienteer.oposter.web.OAuthCallbackResource;

import com.github.scribejava.apis.VkontakteApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.google.inject.ProvidedBy;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.photos.responses.GetWallUploadServerResponse;
import com.vk.api.sdk.objects.photos.responses.SaveWallPhotoResponse;
import com.vk.api.sdk.objects.photos.responses.WallUploadResponse;
import com.vk.api.sdk.objects.wall.responses.PostResponse;
import com.vk.api.sdk.queries.photos.PhotosGetWallUploadServerQuery;
import com.vk.api.sdk.queries.photos.PhotosSaveWallPhotoQuery;
import com.vk.api.sdk.queries.wall.WallPostQuery;

/**
 * {@link IPlatformApp} for VKontakte 
 */
@ProvidedBy(ODocumentWrapperProvider.class)
@DAOOClass(value = IVkApp.CLASS_NAME, orderOffset = 100)
public interface IVkApp extends IPlatformApp, IOAuthReciever {
	
	public static final MetaDataKey<VkApiClient> VK_APP_KEY = new MetaDataKey<VkApiClient>() {};
	
	public static final String CLASS_NAME = "OPVkApp";
	
	@DAOField(notNull = true)
	public Integer getAppId();
	public void setAppId(Integer value);
	
	
	@DAOField(notNull = true)
	public String getAppSecret();
	public void setAppSecret(String value);
	
	@DAOField(notNull = true)
	public String getServiceToken();
	public void setServiceToken(String value);
	
	@DAOField(notNull = true)
	public Long getDefaultUserId();
	public void setDefaultUserId(Long value);
	
	@DAOField(notNull = true)
	public String getDefaultUserAccessKey();
	public void setDefaultUserAccessKey(String value);
	
	@Override
	public default boolean send (IChannel channel, IContent content) {
		if(channel instanceof IVkWall) {
			IVkWall wall = (IVkWall) channel;
			VkApiClient vk = getVkApiClient();
			UserActor userActor = new UserActor(wall.getEffectiveUserId().intValue(), wall.getEffectiveAccessKey());
			try {
				WallPostQuery post = vk.wall().post(userActor);
				if(wall.getOwnerId()!=null) post.ownerId(wall.getAdjustedOwnerId().intValue());
				post.message(content.getContent());
				if(content.hasImages()) {
					PhotosGetWallUploadServerQuery getWallServer = vk.photos().getWallUploadServer(userActor);
					if(wall.getOwnerId()!=null) getWallServer.groupId(wall.getOwnerId().intValue());
					GetWallUploadServerResponse uploadServer = getWallServer.execute();
					
					List<String> attachments = new ArrayList<>();
					for(IImageAttachment image : content.getImages()) {
						WallUploadResponse upload = vk.upload()
														.photoWall(uploadServer.getUploadUrl().toString(), image.asFile())
														.execute();
						PhotosSaveWallPhotoQuery savePhotoWall = vk.photos().saveWallPhoto(userActor, upload.getPhoto())
																	.server(upload.getServer())
																	.hash(upload.getHash());
						if(wall.getOwnerId()!=null) savePhotoWall.groupId(wall.getOwnerId().intValue());
						List<SaveWallPhotoResponse> photoList = savePhotoWall.execute();
						attachments.add("photo" + photoList.get(0).getOwnerId() + "_" + photoList.get(0).getId());
					}
					post.attachments(attachments);
				}
				post.execute();
				return true;
			} catch (Exception e) {
				OLogger.log(e, DAO.asDocument(channel).getIdentity().toString());
			}
		}
		return false;
	}
	
	public default OAuth20Service getService(IOAuthReciever reciever) {
		
		ODocument reciverDoc = DAO.asDocument(reciever!=null?reciever:this); 
		 return new ServiceBuilder(getAppId().toString())
	                .apiSecret(getAppSecret())
	                .defaultScope("offline,wall,groups,video,photos") // replace with desired scope
	                .callback(OAuthCallbackResource.urlFor(reciverDoc).toString())
	                .build(VkontakteApi.instance());
	}
	
	public default VkApiClient getVkApiClient() {
		VkApiClient ret = OrienteerWebApplication.lookupApplication().getMetaData(VK_APP_KEY);
		if(ret==null) {
			TransportClient transportClient = new HttpTransportClient();
			ret = new VkApiClient(transportClient);
			OrienteerWebApplication.lookupApplication().setMetaData(VK_APP_KEY, ret);
		}
		return ret;
	}
	
	@OMethod(
			titleKey = "command.connectoauth", 
			order=10,bootstrap=BootstrapType.SUCCESS,icon = FAIconType.play,
			filters={
					@OFilter(fClass = PlaceFilter.class, fData = "STRUCTURE_TABLE"),
					@OFilter(fClass = WidgetTypeFilter.class, fData = "parameters"),
			}
	)
	public default void connectOAuth(IMethodContext ctx) {
		try(OAuth20Service service = getService(this)) {
			String redirectTo = service.getAuthorizationUrl();
			throw new RedirectToUrlException(redirectTo);
		} catch (IOException e) {
			ctx.showFeedback(FeedbackMessage.ERROR, "error.oauthrequest", Model.of(e.getMessage()));
			OLogger.log(e, DAO.asDocument(this).getIdentity().toString());
		}
	}
	
	@Override
	public default void codeObtained(String code) throws Exception {
		try(OAuth20Service service = getService(this)) {
			setDefaultUserAccessKey(service.getAccessToken(code).getAccessToken());
			setDefaultUserId(getVkApiClient().users().get(
							new UserActor(null, getDefaultUserAccessKey())).execute().get(0).getId().longValue());
			DAO.save(this);
		}
	}
}
