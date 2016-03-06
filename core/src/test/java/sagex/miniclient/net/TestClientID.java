package sagex.miniclient.net;

import org.junit.Test;

import sagex.miniclient.util.ClientIDGenerator;

import static org.junit.Assert.assertEquals;

/**
 * Created by seans on 05/03/16.
 */
public class TestClientID {
    @Test
    public void testId() {
        ClientIDGenerator gen = new ClientIDGenerator();
        System.out.println(gen.generateId());
        System.out.println(gen.generateId("stuckless"));

        assertEquals("73:74:75:63:6b:6c", gen.generateId("stuckless"));
        assertEquals("73:74:75:00:00:00", gen.generateId("stu"));

        assertEquals("stuckless", gen.id2string("73:74:75:63:6b:6c:65:73:73"));
    }
}
