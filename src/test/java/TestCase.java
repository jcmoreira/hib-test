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
        return new Class[] {
				Entit.class,
				Property.class
        };
    }

    @Override
    protected void configure(Configuration configuration) {
        super.configure( configuration );

        configuration.setProperty( AvailableSettings.SHOW_SQL, Boolean.TRUE.toString() );
        configuration.setProperty( AvailableSettings.GENERATE_STATISTICS, "true" );
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

        assertThat(sessionFactory().getStatistics().getEntityInsertCount(), is(3L)); // OK, 1 insert for child + 2 insert for the parents

        sessionFactory().getStatistics().clear();

        // Retrieving Entities
        s = openSession();
        tx = s.beginTransaction();
        s.byId(Entit.class).load(1);
        s.byId(Entit.class).load(2);
        tx.commit();
        s.close();
    }
}