/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */

package org.hibernate.orm.test.idgen;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.TableGenerator;

import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;

import org.hibernate.testing.orm.junit.DomainModel;
import org.hibernate.testing.orm.junit.SessionFactory;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Tests for scoping of generator names
 *
 * @author Andrea Boriero
 */
@DomainModel( annotatedClasses = { IdGeneratorNameScopingTest.FirstEntity.class, IdGeneratorNameScopingTest.SecondEntity.class } )
@SessionFactory
public class IdGeneratorNameScopingTest {
	//
	@Test
	public void testLocalScoping() {
		// test Hibernate's default behavior which "violates" the spec but is much
		// more sane.
		// this works properly because Hibernate simply prefers the locally defined one
		buildMetadata( false );
	}

	@Test
	public void testGlobalScoping() {
		// now test JPA's strategy of globally scoping identifiers.
		// this will fail because both
		try {
			buildMetadata( true );
			fail();
		}
		catch (Exception e) {
			assertThat( e, instanceOf( IllegalArgumentException.class ) );
			assertThat( e.getMessage(), startsWith( "Duplicate generator name" ) );
		}
	}

	public void buildMetadata(boolean jpaCompliantScoping) {
		final StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
				.applySetting( AvailableSettings.JPA_ID_GENERATOR_GLOBAL_SCOPE_COMPLIANCE, jpaCompliantScoping )
				.build();

		try {
			// this will fail with global scoping and pass with local scoping
			new MetadataSources( registry )
					.addAnnotatedClass( FirstEntity.class )
					.addAnnotatedClass( SecondEntity.class )
					.buildMetadata();
		}
		finally {
			StandardServiceRegistryBuilder.destroy( registry );
		}
	}

	@Entity(name = "FirstEntity")
	@TableGenerator(
			name = "table-generator",
			table = "table_identifier_2",
			pkColumnName = "identifier",
			valueColumnName = "value",
			allocationSize = 5,
			initialValue = 1
	)
	public static class FirstEntity {
		@Id
		@GeneratedValue(strategy = GenerationType.TABLE, generator = "table-generator")
		private Long id;

		public Long getId() {
			return id;
		}
	}

	@Entity(name = "SecondEntity")
	@TableGenerator(
			name = "table-generator",
			table = "table_identifier",
			pkColumnName = "identifier",
			valueColumnName = "value",
			allocationSize = 5,
			initialValue = 10
	)
	public static class SecondEntity {
		@Id
		@GeneratedValue(strategy = GenerationType.TABLE, generator = "table-generator")
		private Long id;

		public Long getId() {
			return id;
		}
	}
}
