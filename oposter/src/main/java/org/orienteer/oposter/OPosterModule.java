package org.orienteer.oposter;

import org.orienteer.core.OrienteerWebApplication;
import org.orienteer.core.component.FAIconType;
import org.orienteer.core.dao.DAO;
import org.orienteer.core.method.OMethodsManager;
import org.orienteer.core.module.AbstractOrienteerModule;
import org.orienteer.core.module.IOrienteerModule;
import org.orienteer.core.module.PerspectivesModule;
import org.orienteer.core.module.PerspectivesModule.IOPerspective;
import org.orienteer.core.util.OSchemaHelper;
import org.orienteer.mail.OMailModule;
import org.orienteer.oposter.component.attachment.AttachmentsVisualizer;
import org.orienteer.oposter.component.widget.AbstractCalendarContentWidget;
import org.orienteer.oposter.facebook.IFacebookApp;
import org.orienteer.oposter.facebook.IFacebookConnection;
import org.orienteer.oposter.instagram.IIGAccount;
import org.orienteer.oposter.instagram.IIGApp;
import org.orienteer.oposter.model.IChannel;
import org.orienteer.oposter.model.IContent;
import org.orienteer.oposter.model.IContentPlan;
import org.orienteer.oposter.model.IPlatformApp;
import org.orienteer.oposter.model.IPosting;
import org.orienteer.oposter.ok.IOkApp;
import org.orienteer.oposter.ok.IOkChannel;
import org.orienteer.oposter.telegram.ITelegramBot;
import org.orienteer.oposter.telegram.ITelegramChannel;
import org.orienteer.oposter.twitter.ITwitterAccount;
import org.orienteer.oposter.twitter.ITwitterApp;
import org.orienteer.oposter.vk.IVkApp;
import org.orienteer.oposter.vk.IVkWall;

import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.db.OrientDBInternal;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.schedule.OScheduledEvent;

/**
 * {@link IOrienteerModule} for 'oposter' module
 */
public class OPosterModule extends AbstractOrienteerModule{
	
	public static final String NAME = "oposter";
	public static final String PERSPECTIVE_ALIAS = "oposter";

	protected OPosterModule() {
		super(NAME, 15, PerspectivesModule.NAME, OMailModule.NAME);
	}
	
	@Override
	public ODocument onInstall(OrienteerWebApplication app, ODatabaseSession db) {
		super.onInstall(app, db);
		OSchemaHelper helper = OSchemaHelper.bind(db);
		DAO.define(IContentPlan.class,
							 IContent.class,
							 IChannel.class,
							 IPlatformApp.class,
							 // Telegram
							 ITelegramChannel.class, 
							 ITelegramBot.class,
							 //Facebook
							 IFacebookConnection.class,
							 IFacebookApp.class,
							 //VKontakte
							 IVkWall.class,
							 IVkApp.class,
							 //Instagram
							 IIGApp.class,
							 IIGAccount.class,
							 //Twitter
							 ITwitterApp.class,
							 ITwitterAccount.class,
							 //Odnoklassniki
							 IOkApp.class,
				 			 IOkChannel.class);
		
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
		installPerspective(helper);
		
		return null;
	}
	
	@Override
	public void onUpdate(OrienteerWebApplication app, ODatabaseSession db, int oldVersion, int newVersion) {
		onInstall(app, db);
	}
	
	protected void installPerspective(OSchemaHelper helper) {
		
		IOPerspective perspective = IOPerspective.getOrCreateByAlias("oposter", 
																	 "perspective.oposter", 
																	 FAIconType.bolt.name(), 
																	 "/browse/"+IContentPlan.CLASS_NAME);
		
		perspective.getOrCreatePerspectiveItem("content_plans", 
											   "perspective.oposter.contentplans",
											   FAIconType.bolt.name(),
											   "/browse/"+IContentPlan.CLASS_NAME);
		
		perspective.getOrCreatePerspectiveItem("contens", 
											   "perspective.oposter.contents",
											   FAIconType.envelope.name(),
											   "/browse/"+IContent.CLASS_NAME);
		
		perspective.getOrCreatePerspectiveItem("platform_appss", 
											   "perspective.oposter.platformapps",
											   FAIconType.rocket.name(),
											   "/browse/"+IPlatformApp.CLASS_NAME);
		
		perspective.getOrCreatePerspectiveItem("channels", 
											   "perspective.oposter.channels",
											   FAIconType.link.name(),
											   "/browse/"+IChannel.CLASS_NAME);

		perspective.getOrCreatePerspectiveItem("postings", 
											   "perspective.oposter.postings",
											   FAIconType.share_alt.name(),
											   "/browse/"+IPosting.CLASS_NAME);

	}
	
	@Override
	public void onInitialize(OrienteerWebApplication app, ODatabaseSession db) {
		super.onInitialize(app, db);
		app.mountPackage("org.orienteer.oposter.web");
		app.getUIVisualizersRegistry().registerUIComponentFactory(new AttachmentsVisualizer());
		
		OMethodsManager.get().addModule(OPosterModule.class);
		OMethodsManager.get().reload();
		app.registerWidgets(AbstractCalendarContentWidget.class.getPackage().getName());
		
		
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
		app.unregisterWidgets(AbstractCalendarContentWidget.class.getPackage().getName());
		OMethodsManager.get().removeModule(OPosterModule.class);
		OMethodsManager.get().reload();
	}
	
}
