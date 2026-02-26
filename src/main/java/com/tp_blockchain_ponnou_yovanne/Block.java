package com.tp_blockchain_ponnou_yovanne;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;

public class Block {
    public final int index;
    public final String timestamp;
    public final TicketData data;
    public final String previousHash;
    public final long nonce;
    public final String consensus;
    public final String validator;
    public final String hash;

    public Block(int index, TicketData data, String previousHash) {
        this(index, Instant.now().toString(), data, previousHash, 0L, "BASIC", "N/A");
    }

    public Block(int index, TicketData data, String previousHash, long nonce, String consensus, String validator) {
        this(index, Instant.now().toString(), data, previousHash, nonce, consensus, validator);
    }

    private Block(int index, String timestamp, TicketData data, String previousHash, long nonce, String consensus, String validator) {
        this.index = index;
        this.timestamp = timestamp;
        this.data = data;
        this.previousHash = previousHash;
        this.nonce = nonce;
        this.consensus = consensus;
        this.validator = validator;
        this.hash = calculateHash();
    }

    public String calculateHash() {
        return computeHash(index, timestamp, data, previousHash, nonce, consensus, validator);
    }

    private static String computeHash(
            int index,
            String timestamp,
            TicketData data,
            String previousHash,
            long nonce,
            String consensus,
            String validator
    ) {
        try {
            String input = index + timestamp + data.canonicalValue() + previousHash + nonce + consensus + validator;
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Unable to calculate hash", e);
        }
    }

    public static Block mineProofOfWork(int index, TicketData data, String previousHash, int difficulty) {
        String timestamp = Instant.now().toString();
        String targetPrefix = "0".repeat(Math.max(0, difficulty));
        long nonce = 0L;

        while (true) {
            String candidateHash = computeHash(index, timestamp, data, previousHash, nonce, "POW", "MINER");
            if (candidateHash.startsWith(targetPrefix)) {
                return new Block(index, timestamp, data, previousHash, nonce, "POW", "MINER");
            }
            nonce++;
        }
    }
}
