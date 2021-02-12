package org.orienteer.oposter.instagram;

import org.joor.Reflect;
import org.orienteer.core.OClassDomain;
import org.orienteer.core.component.visualizer.UIVisualizersRegistry;
import org.orienteer.core.dao.DAO;
import org.orienteer.core.dao.DAOField;
import org.orienteer.core.dao.DAOOClass;
import org.orienteer.core.dao.ODocumentWrapperProvider;
import org.orienteer.oposter.model.IChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.instagram4j.instagram4j.IGClient;
import com.github.instagram4j.instagram4j.exceptions.IGLoginException;
import com.github.instagram4j.instagram4j.utils.IGUtils;
import com.google.inject.ProvidedBy;
import com.orientechnologies.orient.core.metadata.schema.OType;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * {@link IChannel} which represents Account in Instagram
 */
@ProvidedBy(ODocumentWrapperProvider.class)
@DAOOClass(value = IIGAccount.CLASS_NAME, domain = OClassDomain.SPECIFICATION, orderOffset = 100)
public interface IIGAccount extends IChannel {
	public static final String CLASS_NAME = "OPIGAccount";
	public static final Logger LOG = LoggerFactory.getLogger(IIGAccount.class);
	
	@DAOField(notNull = true)
	public String getUsername();
	public void setUsername(String value);
	
	@DAOField(notNull = true, visualization = UIVisualizersRegistry.VISUALIZER_PASSWORD)
	public String getPassword();
	public void setPassword(String value);
	
	@DAOField(type = OType.CUSTOM, hidden = true)
	public IGClient getIGClient();
	public void setIGClient(IGClient value);
	
	@DAOField(type = OType.CUSTOM, hidden = true)
	public SerializableCookieJar getCookieJar();
	public void setCookieJar(SerializableCookieJar value);
	
	
	public default IGClient obtainIGClient() throws IGLoginException {
		IGClient igClient = getIGClient();
		SerializableCookieJar jar = getCookieJar();
		if(igClient==null || jar==null || !igClient.isLoggedIn()) {
			jar = new SerializableCookieJar();
			OkHttpClient okHttpClient = createOkHttpClient(jar);
			igClient = IGClient.builder()
								.client(okHttpClient)
								.username(getUsername())
								.password(getPassword())
								.login();
			setCookieJar(jar);
			setIGClient(igClient);
			DAO.save(this);
		} else {
			OkHttpClient okHttpClient = createOkHttpClient(jar);
			Reflect.on(igClient).set("httpClient", okHttpClient);
		}
		return igClient;
	}
	
	public static OkHttpClient createOkHttpClient(SerializableCookieJar jar) {
		HttpLoggingInterceptor logging = new HttpLoggingInterceptor((m) -> LOG.info(m));
		return new OkHttpClient.Builder()
							.cookieJar(jar)
							.addNetworkInterceptor(logging)
							.build();
	}
	
}
