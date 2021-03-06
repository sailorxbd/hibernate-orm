/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.type;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.usertype.LoggableUserType;
import org.hibernate.usertype.UserCollectionType;

/**
 * A custom type for mapping user-written classes that implement <tt>PersistentCollection</tt>
 * 
 * @see org.hibernate.collection.spi.PersistentCollection
 * @see org.hibernate.usertype.UserCollectionType
 * @author Gavin King
 */
public class CustomCollectionType extends CollectionType {

	private final UserCollectionType userType;
	private final boolean customLogging;

	/**
	 * @deprecated Use {@link #CustomCollectionType(TypeFactory.TypeScope, Class, String, String )} instead.
	 * See Jira issue: <a href="https://hibernate.onjira.com/browse/HHH-7771">HHH-7771</a>
	 */
	@Deprecated
	public CustomCollectionType(
			TypeFactory.TypeScope typeScope,
			Class userTypeClass,
			String role,
			String foreignKeyPropertyName,
			boolean isEmbeddedInXML) {
		super( typeScope, role, foreignKeyPropertyName, isEmbeddedInXML );
		userType = createUserCollectionType( userTypeClass );
		customLogging = LoggableUserType.class.isAssignableFrom( userTypeClass );
	}

	public CustomCollectionType(
			TypeFactory.TypeScope typeScope,
			Class userTypeClass,
			String role,
			String foreignKeyPropertyName) {
		super( typeScope, role, foreignKeyPropertyName );
		userType = createUserCollectionType( userTypeClass );
		customLogging = LoggableUserType.class.isAssignableFrom( userTypeClass );
	}

	private static UserCollectionType createUserCollectionType(Class userTypeClass) {
		if ( !UserCollectionType.class.isAssignableFrom( userTypeClass ) ) {
			throw new MappingException( "Custom type does not implement UserCollectionType: " + userTypeClass.getName() );
		}

		try {
			return ( UserCollectionType ) userTypeClass.newInstance();
		}
		catch ( InstantiationException ie ) {
			throw new MappingException( "Cannot instantiate custom type: " + userTypeClass.getName() );
		}
		catch ( IllegalAccessException iae ) {
			throw new MappingException( "IllegalAccessException trying to instantiate custom type: " + userTypeClass.getName() );
		}
	}

	public PersistentCollection instantiate(SessionImplementor session, CollectionPersister persister, Serializable key)
	throws HibernateException {
		return userType.instantiate(session, persister);
	}

	public PersistentCollection wrap(SessionImplementor session, Object collection) {
		return userType.wrap(session, collection);
	}

	public Class getReturnedClass() {
		return userType.instantiate( -1 ).getClass();
	}

	public Object instantiate(int anticipatedType) {
		return userType.instantiate( anticipatedType );
	}

	public Iterator getElementsIterator(Object collection) {
		return userType.getElementsIterator(collection);
	}
	public boolean contains(Object collection, Object entity, SessionImplementor session) {
		return userType.contains(collection, entity);
	}
	public Object indexOf(Object collection, Object entity) {
		return userType.indexOf(collection, entity);
	}

	public Object replaceElements(Object original, Object target, Object owner, Map copyCache, SessionImplementor session)
	throws HibernateException {
		CollectionPersister cp = session.getFactory().getCollectionPersister( getRole() );
		return userType.replaceElements(original, target, cp, owner, copyCache, session);
	}

	protected String renderLoggableString(Object value, SessionFactoryImplementor factory) throws HibernateException {
		if ( customLogging ) {
			return ( ( LoggableUserType ) userType ).toLoggableString( value, factory );
		}
		else {
			return super.renderLoggableString( value, factory );
		}
	}

	public UserCollectionType getUserType() {
		return userType;
	}
}
