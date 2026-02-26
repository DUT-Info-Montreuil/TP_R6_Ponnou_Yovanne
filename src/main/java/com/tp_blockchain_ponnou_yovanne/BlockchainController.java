package com.tp_blockchain_ponnou_yovanne;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/blockchain")
@Tag(name = "Blockchain", description = "Operations de consultation et d'ajout de blocs")
public class BlockchainController {

    private final BlockchainService blockchainService;

    public BlockchainController(BlockchainService blockchainService) {
        this.blockchainService = blockchainService;
    }

    @GetMapping("/chain")
    @Operation(summary = "Recuperer la chaine complete")
    public List<Block> getChain() {
        return blockchainService.getChain();
    }

    @GetMapping("/validate")
    @Operation(summary = "Verifier l'integrite de la chaine")
    public Map<String, Boolean> validate() {
        return Map.of("valid", blockchainService.isChainValid());
    }

    @PostMapping("/block")
    @Operation(summary = "Ajouter un bloc avec un consensus au choix")
    public ResponseEntity<Block> addBlock(@RequestBody AddBlockRequest request) {
        TicketData data = new TicketData(
                request.eventId(),
                request.artist(),
                request.status(),
                request.owner()
        );
        blockchainService.addBlock(data, request.consensus() != null ? request.consensus() : "BASIC");
        List<Block> chain = blockchainService.getChain();
        return ResponseEntity.ok(chain.get(chain.size() - 1));
    }

    public record AddBlockRequest(
            @Schema(example = "EVT-2026-001")
            String eventId,
            @Schema(example = "Coldplay")
            String artist,
            @Schema(example = "PURCHASED")
            String status,
            @Schema(example = "Alice")
            String owner,
            @Schema(example = "POW", description = "Valeurs supportees: BASIC, POW, POS, PBFT, POA")
            String consensus
    ) {}
}
