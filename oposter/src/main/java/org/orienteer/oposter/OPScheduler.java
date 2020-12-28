package org.orienteer.oposter;

import java.util.List;

import org.apache.wicket.ThreadContext;
import org.orienteer.core.OrienteerWebApplication;
import org.orienteer.core.dao.DAO;
import org.orienteer.oposter.model.IChannel;
import org.orienteer.oposter.model.IContent;
import org.orienteer.oposter.model.IOPosterDAO;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.sql.executor.OResultSet;

import ru.ydn.wicket.wicketorientdb.utils.DBClosure;

@Singleton
public class OPScheduler {
	
	@Inject
	private Provider<IOPosterDAO> posterDAOProvider;
	
	public static synchronized final OPScheduler getInstance() {
		return OrienteerWebApplication.lookupApplication().getServiceInstance(OPScheduler.class);
	}
	
	public void tick() {
		new DBClosure<Boolean>() {

			@Override
			protected Boolean execute(ODatabaseSession db) {
				ThreadContext.setApplication(OrienteerWebApplication.lookupApplication());
				try {
					tick(db);
					return true;
				} finally {
					ThreadContext.detach();
				}
			}
		}.execute();
	}
	
	protected void tick(ODatabaseSession db) {
		List<IContent> contentToSend  = posterDAOProvider.get().findContentToSend();
		if(contentToSend!=null && !contentToSend.isEmpty()) {
			for (IContent content : contentToSend) {
				List<IChannel> channels = content.getChannels();
				if(channels!=null && !channels.isEmpty()) {
					for (IChannel channel : channels) {
						channel.send(content);
					}
					content.published();
					DAO.save(content);
				}
			}
		}
	}
}
