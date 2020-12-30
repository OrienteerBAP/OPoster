package org.orienteer.oposter;

import org.orienteer.core.OrienteerWebApplication;
import org.orienteer.core.component.visualizer.UIVisualizersRegistry;
import org.orienteer.core.dao.DAO;
import org.orienteer.core.module.AbstractOrienteerModule;
import org.orienteer.core.module.IOrienteerModule;
import org.orienteer.core.util.OSchemaHelper;
import org.orienteer.oposter.component.attachment.AttachmentsVisualizer;
import org.orienteer.oposter.facebook.IFacebookApp;
import org.orienteer.oposter.facebook.IFacebookPage;
import org.orienteer.oposter.model.IChannel;
import org.orienteer.oposter.model.IContent;
import org.orienteer.oposter.model.IContentPlan;
import org.orienteer.oposter.model.IPlatformApp;
import org.orienteer.oposter.telegram.ITelegramBot;
import org.orienteer.oposter.telegram.ITelegramChannel;

import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.db.OrientDBInternal;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.schedule.OScheduledEvent;
import com.orientechnologies.orient.core.schedule.OScheduler;
import com.orientechnologies.orient.core.metadata.schema.OType;

/**
 * {@link IOrienteerModule} for 'oposter' module
 */
public class OPosterModule extends AbstractOrienteerModule{

	protected OPosterModule() {
		super("oposter", 3);
	}
	
	@Override
	public ODocument onInstall(OrienteerWebApplication app, ODatabaseSession db) {
		super.onInstall(app, db);
		OSchemaHelper helper = OSchemaHelper.bind(db);
		DAO.describe(helper, IContentPlan.class,
							 IContent.class,
							 IChannel.class,
							 IPlatformApp.class);
		DAO.describe(helper, ITelegramChannel.class, 
							 ITelegramBot.class);
		DAO.describe(helper, IFacebookPage.class,
							 IFacebookApp.class);
		
		helper.oClass("OFunction")
					.oDocument("name", "Scheduler")
					.field("language", "nashorn")
					.field("code", "org.orienteer.oposter.OPScheduler.getInstance().tick()")
					.saveDocument();
		ODocument schedulerFunc = helper.getODocument();
		helper.oClass("OSchedule")
					.oDocument("name", "OPoster")
					.field("rule", "0 * * * * ?")
					.field("function", schedulerFunc)
					.saveDocument();
		
		return null;
	}
	
	@Override
	public void onUpdate(OrienteerWebApplication app, ODatabaseSession db, int oldVersion, int newVersion) {
		onInstall(app, db);
	}
	
	@Override
	public void onInitialize(OrienteerWebApplication app, ODatabaseSession db) {
		super.onInitialize(app, db);
		app.mountPackage("org.orienteer.oposter.web");
		app.getUIVisualizersRegistry().registerUIComponentFactory(new AttachmentsVisualizer());
		
		
		//Kicking-off scheduled events.
		//TODO: remove after fixing issue in OrientDB: https://github.com/orientechnologies/orientdb/issues/9500
		String dbName = db.getName();
		OrientDBInternal orientDbInternal = OrientDBInternal.extract(app.getServer().getContext());
		db.browseClass(OScheduledEvent.CLASS_NAME)
								.forEach(d -> {
									new OScheduledEvent(d).schedule(dbName, "admin", orientDbInternal);
								});
	}
	
	@Override
	public void onDestroy(OrienteerWebApplication app, ODatabaseSession db) {
		super.onDestroy(app, db);
		app.unmountPackage("org.orienteer.oposter.web");
	}
	
}
