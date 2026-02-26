package com.tp_blockchain_ponnou_yovanne;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Blockchain {
    private final List<Block> chain;
    private final List<ValidatorNode> validators;
    private final Random random;
    private final int powDifficulty;

    public Blockchain() {
        chain = new ArrayList<>();
        validators = new ArrayList<>();
        random = new Random();
        powDifficulty = 4;

        validators.add(new ValidatorNode("Validator-A", 50, true, true));
        validators.add(new ValidatorNode("Validator-B", 30, true, true));
        validators.add(new ValidatorNode("Validator-C", 15, false, true));
        validators.add(new ValidatorNode("Validator-D", 5, false, false));

        chain.add(new Block(
                0,
                new TicketData("GENESIS", "N/A", "INIT", "PLATFORM"),
                "0",
                0L,
                "GENESIS",
                "SYSTEM"
        ));
    }

    public void addBlock(TicketData data) {
        Block lastBlock = chain.get(chain.size() - 1);
        Block newBlock = new Block(chain.size(), data, lastBlock.hash);
        chain.add(newBlock);
    }

    public void addBlockWithProofOfWork(TicketData data) {
        Block lastBlock = chain.get(chain.size() - 1);
        Block minedBlock = Block.mineProofOfWork(chain.size(), data, lastBlock.hash, powDifficulty);
        chain.add(minedBlock);
    }

    public void addBlockWithProofOfStake(TicketData data) {
        Block lastBlock = chain.get(chain.size() - 1);
        ValidatorNode selectedValidator = selectValidatorByStake();
        Block newBlock = new Block(
                chain.size(),
                data,
                lastBlock.hash,
                0L,
                "POS",
                selectedValidator.id
        );
        chain.add(newBlock);
    }

    public void addBlockWithProofOfAuthority(TicketData data) {
        Block lastBlock = chain.get(chain.size() - 1);
        List<ValidatorNode> authorities = validators.stream().filter(v -> v.authority).toList();
        if (authorities.isEmpty()) {
            throw new IllegalStateException("No authority validator configured for PoA");
        }

        ValidatorNode selectedValidator = authorities.get(chain.size() % authorities.size());
        Block newBlock = new Block(
                chain.size(),
                data,
                lastBlock.hash,
                0L,
                "POA",
                selectedValidator.id
        );
        chain.add(newBlock);
    }

    public void addBlockWithPbft(TicketData data) {
        if (!hasPbftConsensus()) {
            throw new IllegalStateException("PBFT consensus rejected the block");
        }

        Block lastBlock = chain.get(chain.size() - 1);
        Block newBlock = new Block(
                chain.size(),
                data,
                lastBlock.hash,
                0L,
                "PBFT",
                "PBFT-CLUSTER"
        );
        chain.add(newBlock);
    }

    public boolean isChainValid() {
        for (int i = 1; i < chain.size(); i++) {
            Block current = chain.get(i);
            Block previous = chain.get(i - 1);

            if (!current.hash.equals(current.calculateHash())) {
                return false;
            }
            if (!current.previousHash.equals(previous.hash)) {
                return false;
            }

            if ("POW".equals(current.consensus)) {
                String targetPrefix = "0".repeat(powDifficulty);
                if (!current.hash.startsWith(targetPrefix)) {
                    return false;
                }
            }
        }

        Block genesis = chain.get(0);
        return genesis.hash.equals(genesis.calculateHash());
    }

    public void displayChain() {
        for (Block block : chain) {
            System.out.println("Index : " + block.index);
            System.out.println("Horodatage : " + block.timestamp);
            System.out.println("Donnees : " + block.data);
            System.out.println("Consensus : " + block.consensus);
            System.out.println("Validateur : " + block.validator);
            System.out.println("Nonce : " + block.nonce);
            System.out.println("Hash prec. : " + block.previousHash);
            System.out.println("Hash : " + block.hash);
            System.out.println("-------------------------------------");
        }
    }

    public void exportAsJson(String filePath) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(chain);
        try {
            Files.writeString(Path.of(filePath), json, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Unable to export blockchain to JSON", e);
        }
    }

    public List<Block> getChain() {
        return List.copyOf(chain);
    }

    public int getPowDifficulty() {
        return powDifficulty;
    }

    private ValidatorNode selectValidatorByStake() {
        int totalStake = validators.stream().mapToInt(v -> v.stake).sum();
        int pick = random.nextInt(totalStake);
        int cumulative = 0;

        for (ValidatorNode validator : validators) {
            cumulative += validator.stake;
            if (pick < cumulative) {
                return validator;
            }
        }

        return validators.get(validators.size() - 1);
    }

    private boolean hasPbftConsensus() {
        int n = validators.size();
        int f = (n - 1) / 3;
        int requiredVotes = (2 * f) + 1;
        int yesVotes = 0;

        for (ValidatorNode validator : validators) {
            if (validator.honest) {
                yesVotes++;
            }
        }

        return yesVotes >= requiredVotes;
    }
}
