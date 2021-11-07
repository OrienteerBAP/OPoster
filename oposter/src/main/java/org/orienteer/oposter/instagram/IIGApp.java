package org.orienteer.oposter.instagram;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.orienteer.core.OClassDomain;
import org.orienteer.core.dao.ODocumentWrapperProvider;
import org.orienteer.core.dao.OrienteerOClass;
import org.orienteer.oposter.model.IChannel;
import org.orienteer.oposter.model.IContent;
import org.orienteer.oposter.model.IImageAttachment;
import org.orienteer.oposter.model.IPlatformApp;
import org.orienteer.oposter.model.IPosting;
import org.orienteer.oposter.vk.IVkApp;
import org.orienteer.transponder.annotation.EntityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.instagram4j.instagram4j.IGClient;
import com.github.instagram4j.instagram4j.actions.timeline.TimelineAction.SidecarInfo;
import com.github.instagram4j.instagram4j.actions.timeline.TimelineAction.SidecarPhoto;
import com.github.instagram4j.instagram4j.exceptions.IGLoginException;
import com.github.instagram4j.instagram4j.responses.media.MediaResponse.MediaConfigureTimelineResponse;
import com.github.instagram4j.instagram4j.utils.IGUtils;
import com.google.inject.ProvidedBy;

/**
 * {@link IPlatformApp} for Instagram: no configuration - can be single for all IG Accounts
 */
@ProvidedBy(ODocumentWrapperProvider.class)
@EntityType(value = IIGApp.CLASS_NAME, orderOffset = 100)
@OrienteerOClass(domain = OClassDomain.SPECIFICATION)
public interface IIGApp extends IPlatformApp{
	public static final String CLASS_NAME = "OPIGApp";
	public static final Logger LOG = LoggerFactory.getLogger(IIGApp.class);

	@Override
	public default IPosting send(IChannel channel, IContent content) throws Exception {
		IIGAccount igAccount = checkChannelType(channel, IIGAccount.class);
		if(!content.hasImages()) throw new IllegalStateException("Instagram require at least one photo");
		else {
			IGClient igClient = igAccount.obtainIGClient();
			List<IImageAttachment> images = content.getImages();
			CompletableFuture<? extends MediaConfigureTimelineResponse> future;
			if(images.size()==1) {
				future = igClient.actions().timeline().uploadPhoto(images.get(0).getData(), content.getContent());
			} else {
				List<SidecarInfo> photos = images.stream().map(a -> new SidecarPhoto(a.getData())).collect(Collectors.toList());
				future = igClient.actions().timeline().uploadAlbum(photos, content.getContent());
			}
			
			MediaConfigureTimelineResponse response = future.join();
			String code = response.getMedia().getCode();
			return IPosting.createFor(channel, content)
							.setExternalPostingId(code)
							.setUrl("https://www.instagram.com/p/%s/", code)
							.setMessage(response.getMessage());
		}
	}
	
	
}
