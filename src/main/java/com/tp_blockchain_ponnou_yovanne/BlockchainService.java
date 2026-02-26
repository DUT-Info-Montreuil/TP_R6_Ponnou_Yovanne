package com.tp_blockchain_ponnou_yovanne;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BlockchainService {

    private final Blockchain blockchain = new Blockchain();

    public List<Block> getChain() {
        return blockchain.getChain();
    }

    public boolean isChainValid() {
        return blockchain.isChainValid();
    }

    public void addBlock(TicketData data, String consensus) {
        switch (consensus.toUpperCase()) {
            case "POW" -> blockchain.addBlockWithProofOfWork(data);
            case "POS" -> blockchain.addBlockWithProofOfStake(data);
            case "PBFT" -> blockchain.addBlockWithPbft(data);
            case "POA" -> blockchain.addBlockWithProofOfAuthority(data);
            default -> blockchain.addBlock(data);
        }
    }
}
