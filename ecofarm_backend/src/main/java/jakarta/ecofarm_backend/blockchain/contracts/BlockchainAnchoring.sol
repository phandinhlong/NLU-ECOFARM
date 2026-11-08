// SPDX-License-Identifier: MIT
pragma solidity ^0.8.0;

contract BlockchainAnchoring {
    // Owner can anchor, anyone can verify
    address public owner;
    
    // Batch ID -> Merkle root mapping
    mapping(bytes32 => bool) public anchoredBatches;
    mapping(bytes32 => bytes32) public merkleRoots;
    
    // Event for anchoring
    event AnchorStored(bytes32 indexed batchId, bytes32 merkleRoot, uint256 timestamp);
    
    modifier onlyOwner() {
        require(msg.sender == owner, "Only owner can call this");
        _;
    }
    
    constructor() {
        owner = msg.sender;
    }
    
    // Owner stores a Merkle root for a batch
    function anchorBatch(bytes32 batchId, bytes32 merkleRoot) external onlyOwner {
        require(!anchoredBatches[batchId], "Batch already anchored");
        anchoredBatches[batchId] = true;
        merkleRoots[batchId] = merkleRoot;
        emit AnchorStored(batchId, merkleRoot, block.timestamp);
    }
    
    // Anyone can query if a batch is anchored
    function isAnchored(bytes32 batchId) external view returns (bool) {
        return anchoredBatches[batchId];
    }
    
    // Anyone can get the Merkle root for a batch
    function getMerkleRoot(bytes32 batchId) external view returns (bytes32) {
        return merkleRoots[batchId];
    }
}
