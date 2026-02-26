package com.tp_blockchain_ponnou_yovanne;

public class ValidatorNode {
    public final String id;
    public final int stake;
    public final boolean authority;
    public final boolean honest;

    public ValidatorNode(String id, int stake, boolean authority, boolean honest) {
        this.id = id;
        this.stake = stake;
        this.authority = authority;
        this.honest = honest;
    }
}
