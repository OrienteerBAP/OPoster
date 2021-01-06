package org.orienteer.oposter.model;

/**
 * Interface (non-DAO) for classes which can receive code as part of OAuth
 */
public interface IOAuthReciever {
	public void codeObtained(String code) throws Exception;
}
