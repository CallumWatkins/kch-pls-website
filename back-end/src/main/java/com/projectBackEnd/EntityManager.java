package main.java.com.projectBackEnd;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import java.io.Serializable;
import java.util.List;

public class EntityManager <T extends TableEntity> { //TODO Try with statics to see which is cleaner
    private Class<T> subclass;
    public void setSubclass(Class<T> subclass) {
        this.subclass = subclass;
    }

    public List<T> getAll() {
        //https://stackoverflow.com/questions/43037814/how-to-get-all-data-in-the-table-with-hibernate/43067399
        List<T> results = null;
        try (Session session = HibernateUtility.getSessionFactory().openSession()) {
            results = getAll(session);
        }
        //HibernateUtility.getSessionFactory().close();
        return results;
        //Doesn't close its own factory, will leak until factory is properly implemented.
    }
    private List<T> getAll(Session session) throws HibernateException  {
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<T>  criteria = builder.createQuery(subclass);
        criteria.from(subclass);
        return session.createQuery(criteria).getResultList();
    }
    public void deleteAll() {
        SessionFactory sf = HibernateUtility.getSessionFactory();
        Session session = sf.openSession();
        try {
            deleteAll(session);
        } catch(HibernateException ex) {
            if (session.getTransaction() != null) session.getTransaction().rollback();
        } finally {
            session.close();
            //sf.close(); //No longer closes the factory
        }
    }
    private void deleteAll(Session session) throws  HibernateException {
        session.beginTransaction();
        for (Object tuple : getAll()) { //Deleting one by one is recommended to deal with cascading.
            session.delete(tuple);
        }
        session.getTransaction().commit();
    }

    public T insertTuple(T newObject) {
        //if (!extendsTableEntity(newObject.getClass())) return newObject;
        SessionFactory sf = HibernateUtility.getSessionFactory();
        Session session = sf.openSession();
        try {
            insertTuple(newObject, session);
        } catch(HibernateException ex) {
            if (session.getTransaction() != null) session.getTransaction().rollback();
        } finally {
            session.close();
            //sf.close(); //No longer closes its own factory
        }
        return newObject;
    }
    //TODO (Wasif, delete all mass commented out code)
    private void insertTuple(T newObject, Session session) throws HibernateException {
        session.beginTransaction();
        session.save(newObject);
        session.getTransaction().commit();
    }

    public T getByPrimaryKey(Serializable pk) {
        SessionFactory sf = HibernateUtility.getSessionFactory();
        Session session = sf.openSession();
        T found = null;
        try {
            found = getByPrimaryKey(pk, session);
        } catch(HibernateException ex) {
            if (session.getTransaction() != null) session.getTransaction().rollback(); //VIOLATES
        } finally {
            session.close();
            //sf.close();
        }
        return found;
    }
    private T getByPrimaryKey(Serializable pk, Session session) throws HibernateException {
        session.beginTransaction(); //TODO Demeter Violation with Implicit Transaction object
        T found = session.get(subclass, pk);
        session.getTransaction().commit(); //Violation
        return found;
    }

    public void delete(T object) {
        SessionFactory sf = HibernateUtility.getSessionFactory();
        Session session = sf.openSession();
        try {
            delete(object, session);
        } catch(HibernateException ex) {
            if (session.getTransaction() != null) session.getTransaction().rollback();
        } finally {
            session.close();
            //sf.close();
        }
    }
    private void delete(T object, Session session) throws HibernateException {
        session.beginTransaction();
        T entityToDelete = getByPrimaryKey(object.getPrimaryKey());
        session.delete(entityToDelete);
        session.getTransaction().commit();
    }

    public void delete(Serializable pk) {
        SessionFactory sf = HibernateUtility.getSessionFactory();
        Session session = sf.openSession();
        try {
            delete(pk, session);
        } catch(HibernateException ex) {
            if (session.getTransaction() != null) session.getTransaction().rollback();
        } finally {
            session.close();
            //sf.close();
        }
    }

    private void delete(Serializable pk, Session session) throws HibernateException {
        session.beginTransaction();
        T entityToDelete = getByPrimaryKey(pk);
        session.delete(entityToDelete);
        session.getTransaction().commit();
    }

    //TODO Might need to return back down if frontend send strings etc. I presume they will json and send the (page) back
    //Methods are commented out already in the PageManager if they send a String primary key.
    public T update(T updatedCopy) {
        SessionFactory sf = HibernateUtility.getSessionFactory(); //Gets sf
        Session session = sf.openSession();
        T fromDatabase = null;
        try {
            fromDatabase = update(updatedCopy, session);
        } catch(HibernateException ex) {
            if (session.getTransaction() != null) session.getTransaction().rollback();
        } finally {
            session.close();
            //sf.close();
        }
        return fromDatabase;
    }

    private T update(T updatedCopy, Session session) throws HibernateException {
        session.beginTransaction(); //TODO Demeter Violation with Implicit Transaction object
        T fromDatabase = (T) session.load(updatedCopy.getClass(), updatedCopy.getPrimaryKey());
        //TODO: If not found?
        if (fromDatabase != null) fromDatabase.copy(updatedCopy);
        else insertTuple(updatedCopy);
        session.getTransaction().commit(); //Violation
        return fromDatabase;
    }


}
/*public static void removeAllInstances(final Class<?> clazz) {
    SessionFactory sessionFactory = HibernateUtility.buildSessionFactory();
    Session session = sessionFactory.getCurrentSession();
    session.beginTransaction();
    final List<?> instances = session.createCriteria(clazz).list();
    for (Object obj : instances) {
        session.delete(obj);
    }
    session.getTransaction().commit();
}*/ //https://stackoverflow.com/questions/25097385/query-to-delete-all-rows-in-a-table-hibernate/25097482
