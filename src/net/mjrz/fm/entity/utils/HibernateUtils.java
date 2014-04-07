/*******************************************************************************
 * Copyright  
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package net.mjrz.fm.entity.utils;

import java.io.File;

import net.mjrz.fm.Main;
import net.mjrz.fm.utils.ZProperties;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

/**
 * @author iFreeBudget ifreebudget@gmail.com
 * 
 */
public class HibernateUtils {
	private static SessionFactory sessionFactory = null;

	private static String DATA_FILE_NAME = null;
	private static Logger logger = Logger.getLogger(HibernateUtils.class
			.getName());

	public static void initialize(String uname) {
		try {
			String path = ZProperties.getProperty("FMHOME");
			DATA_FILE_NAME = "data.data";

			File file = new File(path);
			File[] flist = file.listFiles();
			boolean found = false;
			for (int i = 0; i < flist.length; i++) {
				if (flist[i].getName().equals(DATA_FILE_NAME)) {
					found = true;
					break;
				}
			}
			String URL = "jdbc:hsqldb:file:" + path + Main.PATH_SEPARATOR
					+ "data";

			Configuration cfg = new Configuration();

			if (!found) {
				cfg.setProperty("hibernate.hbm2ddl.auto", "create");
			}
			else {
				cfg.setProperty("hibernate.hbm2ddl.auto", "update");
			}

			cfg.setProperty("hibernate.connection.driver_class",
					"org.hsqldb.jdbcDriver");
			cfg.setProperty("hibernate.connection.url", URL);
			cfg.setProperty("hibernate.connection.username", "sa");
			cfg.setProperty("hibernate.connection.password", "");
			cfg.setProperty("hibernate.connection.pool_size", "10");
			cfg.setProperty("hibernate.connection.hsqldb.default_table_type",
					"cached");

			sessionFactory = cfg.configure().buildSessionFactory();
		}
		catch (Throwable ex) {
			System.err.println("Initial SessionFactory creation failed." + ex);
			throw new ExceptionInInitializerError(ex);
		}
	}

	public static void shutdownHsql() {
		try {
			if (sessionFactory == null)
				return;
			logger.info("Shutting down HSQLDB...");
			org.hibernate.Session s = HibernateUtils.getSessionFactory()
					.getCurrentSession();
			s.beginTransaction();
			s.connection().createStatement().execute("SHUTDOWN");
			s.getTransaction().commit();
			closeSession();
			sessionFactory.close();
			sessionFactory = null;
		}
		catch (Exception e) {
			logger.error(e.getMessage());
		}
	}

	public static void closeSession() {
		sessionFactory.getCurrentSession().disconnect();
		if (sessionFactory.getCurrentSession() != null)
			sessionFactory.getCurrentSession().close();
	}

	public static SessionFactory getSessionFactory() {
		return sessionFactory;
	}
}
