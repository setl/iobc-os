// SPDX-License-Identifier: UNLICENSED

// SETL BNYM Contracts for Ethereum v0.1
// Produced for the Bank of New York Mellon Proof of Concept, 2021
//
// Disclaimer: this code (and its supporting library) are prepared to a Proof of Concept standard, and may not be suitable for Production use.

pragma solidity ^0.8.0;

import "./implementation/ERC1404.sol";
import "./implementation/ALockableMintable.sol";

contract BNYCash is ALockableMintable, ERC1404 {
    constructor(string memory name_, string memory symbol_, uint8 decimals_)
    ERC1404(name_, symbol_, decimals_, 0)
    {}

    function _approveIncrease(address owner, address spender, uint256 delta)
    internal virtual override(ALockable, ERC1404) {
        ERC1404._approveIncrease(owner, spender, delta);
    }

    function _decreaseTotalSupply(uint256 amount) internal virtual override {
        _totalSupply -= amount;
    }

    function _increaseTotalSupply(uint256 amount) internal virtual override {
        _totalSupply += amount;
    }

    function _decreaseBalance(address account, uint256 amount) internal virtual override (AMintable, ERC1404) {
        ERC1404._decreaseBalance(account, amount);
    }

    function _increaseBalance(address account, uint256 amount) internal virtual override (AMintable, ERC1404) {
        ERC1404._increaseBalance(account, amount);
    }

    function _transfer(address from, address to, uint256 amount) internal virtual override (ALockable, ERC1404) {
        ERC1404._transfer(from, to, amount);
    }

    function _unlockedAvailable(address account) internal virtual override view returns (uint256) {
        return _balanceOf(account) - _locked(account);
    }

    modifier onlyController() override (ALockableMintable, ERC1404) {
        require(msg.sender == _controller(), "Only contract owner is allowed to perform this action");
        _;
    }

    modifier validAddress(address account) override(ALockableMintable, ERC1404) {
        require(account != address(0), "Use of the zero address is not allowed");
        _;
    }

    modifier burnableAvailable(address account, uint256 amount) override {
        require(_unlockedAvailable(account) >= amount, "Insufficient assets available to burn");
        _;
    }

    function _detectTransferRestriction(address from, address to, uint256 value) public virtual override view returns (uint8) {
        uint8 code = super._detectTransferRestriction(from, to, value);
        if (code == 0) {
            if (_unlockedAvailable(from) < value) {
                return 4;
            }
        }
        return code;
    }
}
