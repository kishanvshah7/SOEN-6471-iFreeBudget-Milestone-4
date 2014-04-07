package net.mjrz.fm.entity;

import java.util.List;

import net.mjrz.fm.entity.beans.IndexedEntityBean;
import net.mjrz.fm.entity.utils.HibernateUtils;
import net.mjrz.fm.utils.indexer.IndexedEntity;
import net.mjrz.fm.utils.indexer.Indexer.IndexType;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;

public class IndexEntityManager {
	private static Logger logger = Logger.getLogger(IndexEntityManager.class
			.getName());

	public void addIndexedEntity(IndexType indexType, String token,
			IndexedEntity iEntity) throws Exception {
		Session s = null;
		try {
			IndexedEntityBean ieb = new IndexedEntityBean();

			if (iEntity.getId() > 0) {
				ieb.setId(iEntity.getId());
			}
			ieb.setToken(token);
			ieb.setOccuranceCount(iEntity.getCount());
			ieb.setName(iEntity.getName());
			ieb.setType(iEntity.getType());
			ieb.setIndexType(indexType.getType());

			s = HibernateUtils.getSessionFactory().getCurrentSession();
			s.beginTransaction();
			s.saveOrUpdate(ieb);
			s.getTransaction().commit();
			// logger.info("Added index. " + token + ":" + ieb.getId() + ":" +
			// ieb.getName());
		}
		catch (Exception e) {
			logger.error(e);
			throw e;
		}
		finally {
			if (s != null)
				HibernateUtils.closeSession();
		}
	}

	@SuppressWarnings("unchecked")
	public List<IndexedEntityBean> getIndex(IndexType type) throws Exception {
		Session s = null;
		try {
			s = HibernateUtils.getSessionFactory().getCurrentSession();

			s.beginTransaction();

			String query = "select R from IndexedEntityBean R where R.indexType=?";

			Query q = s.createQuery(query);

			q.setInteger(0, type.getType());

			List<IndexedEntityBean> a = q.list();

			s.getTransaction().commit();

			logger.info("Index " + type + " , size = " + a.size());
			return a;
		}
		catch (Exception e) {
			logger.error(e);
			throw e;
		}
		finally {
			if (s != null)
				HibernateUtils.closeSession();
		}
	}
}
