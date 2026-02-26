package com.tp_blockchain_ponnou_yovanne;

public class ApplicationBlockChain {
    public static void main(String[] args) {
        Blockchain blockchain = new Blockchain();

        blockchain.addBlockWithProofOfWork(new TicketData("EVT-2026-001", "Coldplay", "PURCHASED", "Alice"));
        blockchain.addBlockWithProofOfStake(new TicketData("EVT-2026-001", "Coldplay", "RESOLD", "Bob"));
        blockchain.addBlockWithPbft(new TicketData("EVT-2026-001", "Coldplay", "RESOLD", "Charlie"));
        blockchain.addBlockWithProofOfAuthority(new TicketData("EVT-2026-001", "Coldplay", "USED", "Charlie"));
        blockchain.addBlockWithProofOfWork(new TicketData("EVT-2026-001", "Coldplay", "INVALID", "Charlie"));

        blockchain.displayChain();
        System.out.println("Difficulte PoW: " + blockchain.getPowDifficulty());
        System.out.println("Integrite de la chaine: " + blockchain.isChainValid());

        blockchain.exportAsJson("blockchain.json");
        System.out.println("Export JSON termine: blockchain.json");
    }
}
