import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.cfg.Configuration;
import org.junit.Test;
import org.hibernate.testing.junit4.BaseCoreFunctionalTestCase;


public class TestCase extends BaseCoreFunctionalTestCase {

    @Override
    protected Class[] getAnnotatedClasses() {
        return new Class[]{
                Entit.class,
                Property.class
        };
    }

    @Override
    protected void configure(Configuration configuration) {
        super.configure(configuration);

        configuration.setProperty(AvailableSettings.SHOW_SQL, Boolean.TRUE.toString());
        configuration.setProperty(AvailableSettings.GENERATE_STATISTICS, "true");
    }


    @Test
    public void test() throws Exception {
        // First session, inserting entities
        Session s = openSession();
        Transaction tx = s.beginTransaction();
        Property property = new Property(1, 1, 1);
        s.persist(property);
        s.persist(new Entit(1, property));
        s.persist(new Entit(2, property));
        tx.commit();
        s.close();

        assertThat(sessionFactory()
                        .getStatistics()
                        .getEntityInsertCount(),
                is(3L));
        // OK, one Property and two Entits inserted

        sessionFactory().getStatistics().clear();

        // Retrieving Entities
        s = openSession();
        tx = s.beginTransaction();
        s.byId(Entit.class).load(1);
        s.byId(Entit.class).load(2);
        tx.commit();
        s.close();

        assertThat(sessionFactory()
                        .getStatistics()
                        .getEntityLoadCount(),
                is(3L));
        //OK, two Entits and one Property loaded

        assertThat(sessionFactory()
                        .getStatistics()
                        .getPrepareStatementCount(),
                is(3L));
        /*
        NOT OK, four queries are fired to load the entities:
           one for Entit 1;
           one for Property 1;
           one for Entit 2;
           and then (what i believe is wrong) another one for Property 1.
        Shouldn't Property 1 be on the persistenceContext's entitiesByUniqueKey after the first select, so that it would
        be loaded from the context when Entit 2 is loaded, instead of shooting another (equal) query ?
        */

    }
}