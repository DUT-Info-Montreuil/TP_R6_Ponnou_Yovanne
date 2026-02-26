package com.tp_blockchain_ponnou_yovanne;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp()
    {
        Blockchain blockchain = new Blockchain();
        blockchain.addBlockWithProofOfWork(new TicketData("EVT-TEST", "Artist", "PURCHASED", "Alice"));
        blockchain.addBlockWithProofOfStake(new TicketData("EVT-TEST", "Artist", "RESOLD", "Bob"));
        blockchain.addBlockWithPbft(new TicketData("EVT-TEST", "Artist", "USED", "Bob"));
        blockchain.addBlockWithProofOfAuthority(new TicketData("EVT-TEST", "Artist", "INVALID", "Bob"));

        assertTrue(blockchain.isChainValid());
        assertEquals(5, blockchain.getChain().size());
        assertTrue(blockchain.getChain().get(1).hash.startsWith("0".repeat(blockchain.getPowDifficulty())));
        assertEquals("POS", blockchain.getChain().get(2).consensus);
        assertEquals("PBFT", blockchain.getChain().get(3).consensus);
        assertEquals("POA", blockchain.getChain().get(4).consensus);
    }
}
