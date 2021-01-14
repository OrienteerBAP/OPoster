package org.orienteer.oposter;

import org.apache.wicket.model.ResourceModel;
import org.orienteer.core.OrienteerWebApplication;
import org.orienteer.core.component.FAIconType;
import org.orienteer.core.component.visualizer.UIVisualizersRegistry;
import org.orienteer.core.dao.DAO;
import org.orienteer.core.method.OMethodsManager;
import org.orienteer.core.module.AbstractOrienteerModule;
import org.orienteer.core.module.IOrienteerModule;
import org.orienteer.core.module.PerspectivesModule;
import org.orienteer.core.module.PerspectivesModule.OPerspective;
import org.orienteer.core.module.PerspectivesModule.OPerspectiveItem;
import org.orienteer.core.util.CommonUtils;
import org.orienteer.core.util.OSchemaHelper;
import org.orienteer.mail.OMailModule;
import org.orienteer.oposter.component.attachment.AttachmentsVisualizer;
import org.orienteer.oposter.facebook.IFacebookApp;
import org.orienteer.oposter.facebook.IFacebookConnection;
import org.orienteer.oposter.model.IChannel;
import org.orienteer.oposter.model.IContent;
import org.orienteer.oposter.model.IContentPlan;
import org.orienteer.oposter.model.IPlatformApp;
import org.orienteer.oposter.telegram.ITelegramBot;
import org.orienteer.oposter.telegram.ITelegramChannel;
import org.orienteer.oposter.vk.IVkApp;
import org.orienteer.oposter.vk.IVkWall;

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
		super("oposter", 9, PerspectivesModule.NAME, OMailModule.NAME);
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
		DAO.describe(helper, IFacebookConnection.class,
							 IFacebookApp.class);
		DAO.describe(helper, IVkWall.class,
							 IVkApp.class);
		
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
		helper.oClass(OPerspective.CLASS_NAME)
				.oDocument(OPerspective.PROP_ALIAS, "oposter")
					.field(OPerspective.PROP_NAME, CommonUtils.toMap("en", "OPoster"))
					.field(OPerspective.PROP_ICON, FAIconType.bolt.name())
					.field(OPerspective.PROP_HOME_URL, "/browse/"+IContentPlan.CLASS_NAME)
					.saveDocument();
		ODocument perspective = helper.getODocument();
		
		helper.oClass(OPerspectiveItem.CLASS_NAME);
		
		helper.oDocument(OPerspectiveItem.PROP_ALIAS, "content_plans")
					.field(OPerspectiveItem.PROP_NAME, CommonUtils.toMap("en", "Content Plans"))
					.field(OPerspectiveItem.PROP_ICON, FAIconType.bolt.name())
					.field(OPerspectiveItem.PROP_URL, "/browse/"+IContentPlan.CLASS_NAME)
					.field(OPerspectiveItem.PROP_PERSPECTIVE, perspective)
					.saveDocument();
		
		helper.oDocument(OPerspectiveItem.PROP_ALIAS, "contens")
					.field(OPerspectiveItem.PROP_NAME, CommonUtils.toMap("en", "Content"))
					.field(OPerspectiveItem.PROP_ICON, FAIconType.envelope.name())
					.field(OPerspectiveItem.PROP_URL, "/browse/"+IContent.CLASS_NAME)
					.field(OPerspectiveItem.PROP_PERSPECTIVE, perspective)
					.saveDocument();
		
		helper.oDocument(OPerspectiveItem.PROP_ALIAS, "platform_appss")
					.field(OPerspectiveItem.PROP_NAME, CommonUtils.toMap("en", "Platform Apps"))
					.field(OPerspectiveItem.PROP_ICON, FAIconType.rocket.name())
					.field(OPerspectiveItem.PROP_URL, "/browse/"+IPlatformApp.CLASS_NAME)
					.field(OPerspectiveItem.PROP_PERSPECTIVE, perspective)
					.saveDocument();
		
		helper.oDocument(OPerspectiveItem.PROP_ALIAS, "channels")
					.field(OPerspectiveItem.PROP_NAME, CommonUtils.toMap("en", "Channels"))
					.field(OPerspectiveItem.PROP_ICON, FAIconType.link.name())
					.field(OPerspectiveItem.PROP_URL, "/browse/"+IChannel.CLASS_NAME)
					.field(OPerspectiveItem.PROP_PERSPECTIVE, perspective)
					.saveDocument();
	}
	
	@Override
	public void onInitialize(OrienteerWebApplication app, ODatabaseSession db) {
		super.onInitialize(app, db);
		app.mountPackage("org.orienteer.oposter.web");
		app.getUIVisualizersRegistry().registerUIComponentFactory(new AttachmentsVisualizer());
		
		OMethodsManager.get().addModule(OPosterModule.class);
		OMethodsManager.get().reload();
		
		
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
		OMethodsManager.get().removeModule(OPosterModule.class);
		OMethodsManager.get().reload();
	}
	
}
