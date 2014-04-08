/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.androidpn.server.util;

import org.androidpn.server.service.InfrastructureException;
import org.hibernate.HibernateException;
import org.hibernate.Interceptor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

/**
 * Basic Hibernate helper class, handles SessionFactory. <p> Uses a static
 * initializer for zhe initial SessionFactory creation and holds Session in
 * thread local variables.
 * Reference:<a>http://www.koders.com/java/fid07F9A88EDEDAEDAABC6C0535D5CC07C9BB39B43F.aspx</a>
 *
 * @author dell
 */
public class HibernateUtil {

    private static Configuration configuration = null;
    private static SessionFactory sessionFactory = null;
    private static final ThreadLocal<Session> threadSession =
            new ThreadLocal<Session>();
    private static final ThreadLocal<Interceptor> threadInterceptor =
            new ThreadLocal<Interceptor>();
    private static final ThreadLocal<Transaction> threadTransaction =
            new ThreadLocal<Transaction>();

    //create the initial SessionFactory from the default configuration files
    static {
        try {
            configuration = new Configuration();
            sessionFactory = configuration.configure().buildSessionFactory();
            //we could also let Hibernate bind it to JNDI:
            //configuration.configure().buildSessionFactory();
        } catch (Throwable ex) {
            //We have to catch Throwable,otherwise we will miss 
            //NoClassDefFounError and other subclasses of Error
            //log.error("HibernateUtil building SessionFactory failed", ex);

            throw new ExceptionInInitializerError(ex);
        }
    }

    /**
     * Returns the SessionFactory used for this static class
     *
     * @return HIberante.SessionFactory
     */
    public static SessionFactory getSessionFactory() {
        /* Instead of a static variable, use JNDI:
         SessionFactory sessions = null;
         try {
         Context ctx = new InitialContext();
         String jndiName = "java:hibernate/HibernateFactory";
         sessions = (SessionFactory)ctx.lookup(jndiName);
         } catch (NamingException ex) {
         throw new InfrastructureException(ex);
         }
         return sessions;
         */
        return sessionFactory;
    }

    /**
     * Returns the original Hibernate configuration.
     *
     * @return Hiberante.cfg.Configuration
     */
    public static Configuration getConfiguration() {
        return configuration;
    }

    /**
     * Rebuild the SessionFactory with the static Configuration.
     *
     * @throws InfrastructureException
     */
    public static void rebuildSessionFactory()
            throws InfrastructureException {
        synchronized (sessionFactory) {
            try {
                sessionFactory = getConfiguration().buildSessionFactory();
            } catch (Exception ex) {
                throw new InfrastructureException(ex);
            }
        }
    }

    /**
     * Rebuild the SessionFactory with the given Hibernate Configuration and
     * assign <param>cfg<param> to the static configuration
     *
     * @param cfg
     */
    public static void rebuildSessionFactory(Configuration cfg)
            throws InfrastructureException {
        synchronized (sessionFactory) {
            try {
                sessionFactory = cfg.buildSessionFactory();
                configuration = cfg;
            } catch (Exception ex) {
                throw new InfrastructureException(ex);
            }
        }
    }

    /**
     * Retrives the current Session local to the thread.
     * <p/>
     * If no Session is open, opens a new Session for the running thread.
     *
     * @return
     * @throws InfrastructureException
     */
    public static Session getSession()
            throws InfrastructureException {
        Session s = (Session) threadSession.get();
        try {
            if (s == null) {
                if (getInterceptor() != null) {
                    s = getSessionFactory().openSession(getInterceptor());
                } else {
                    s = getSessionFactory().openSession();
                }
                threadSession.set(s);
            }
        } catch (Exception ex) {
            throw new InfrastructureException(ex);
        }
        return s;
    }

    /**
     * Closes the Sesion local to the thread.
     *
     * @throws InfrastructureException
     */
    public static void closeSession()
            throws InfrastructureException {
        try {
            Session s = (Session) threadSession.get();
            threadSession.set(null);
            if (s != null && s.isOpen()) {
                s.close();
            }
        } catch (HibernateException ex) {
            throw new InfrastructureException(ex);
        }
    }

    /**
     * Start a new database transaction.
     *
     * @throws InfrastructureException
     */
    public static void beginTransaction() throws InfrastructureException {
        Transaction tx = (Transaction) threadTransaction.get();
        try {
            if (tx == null) {
                tx = getSession().beginTransaction();
                threadTransaction.set(tx);
            }
        } catch (HibernateException ex) {
            throw new InfrastructureException(ex);
        }
    }

    /**
     * Commit the database transaction.
     *
     * @throws InfrastructureException
     */
    public static void commitTransaction() throws InfrastructureException {
        Transaction tx = (Transaction) threadTransaction.get();
        try {
            if (tx != null && !tx.wasCommitted() && !tx.wasRolledBack()) {
                tx.commit();
            }
            threadTransaction.set(null);
        } catch (HibernateException ex) {
            rollbackTransaction();
            throw new InfrastructureException(ex);
        }
    }

    /**
     * Rollback the database transaction.
     * <p/>
     * Finally close current Session local to the thread, since the commitment
     * of tracsaction associated with current Session throws an Exception.
     *
     * @throws InfrastructureException
     */
    public static void rollbackTransaction() throws InfrastructureException {
        Transaction tx = (Transaction) threadTransaction.get();
        try {
            threadTransaction.set(null);
            if (tx != null && !tx.wasCommitted() && !tx.wasRolledBack()) {
                tx.rollback();
            }
        } catch (HibernateException ex) {
            throw new InfrastructureException(ex);
        } finally {
            closeSession();
        }
    }

    /**
     * Reconnects a Hibernate Session to the current Thread.
     *
     * @param session The Hibernate Session to be reconnected.
     */
    @SuppressWarnings("deprecation")
	public static void reconnect(Session session)
            throws InfrastructureException {
        try {
            session.reconnect();
            threadSession.set(session);
        } catch (HibernateException ex) {
            throw new InfrastructureException(ex);
        }
    }

    /**
     * Disconnect and return Session from current Thread.
     *
     * @return Session the disconnected Session
     */
    public static Session disconnectSession()
            throws InfrastructureException {

        Session session = getSession();
        try {
            threadSession.set(null);
            if (session.isConnected() && session.isOpen()) {
                session.disconnect();
            }
        } catch (HibernateException ex) {
            throw new InfrastructureException(ex);
        }
        return session;
    }

    /**
     * Register a Hibernate interceptor with the current thread. <p> Every
     * Session opened is opened with this interceptor after registration. Has no
     * effect if the current Session of the thread is already open, effective on
     * next close()/getSession().
     */
    public static void registerInterceptor(Interceptor interceptor) {
        threadInterceptor.set(interceptor);
    }

    /**
     * Retrieves the current Hibernate.Interceptor local to the thread.
     *
     * @return Interceptor
     */
    private static Interceptor getInterceptor() {
        Interceptor interceptor =
                (Interceptor) threadInterceptor.get();
        return interceptor;
    }
}
