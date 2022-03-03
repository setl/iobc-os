// SPDX-License-Identifier: UNLICENSED

// SETL BNYM Contracts for Ethereum v0.1
// Produced for the Bank of New York Mellon Proof of Concept, 2021
//
// Disclaimer: this code (and its supporting library) are prepared to a Proof of Concept standard, and may not be suitable for Production use.

pragma solidity ^0.8.0;

import "./implementation/ERC1404.sol";
import "./implementation/ALockable.sol";

contract BNYBond is ALockable, ERC1404 {
    constructor(string memory name_, string memory symbol_, uint8 decimals_, uint256 supply_)
    ERC1404(name_, symbol_, decimals_, supply_)
    {
        require(supply_ > 0, "Bond contract must be created with a non-zero supply");
    }

    function _approveIncrease(address owner, address spender, uint256 delta)
    internal virtual override(ALockable, ERC1404) {
        ERC1404._approveIncrease(owner, spender, delta);
    }

    function _decreaseBalance(address account, uint256 amount) internal virtual override {
        ERC1404._decreaseBalance(account, amount);
    }

    function _increaseBalance(address account, uint256 amount) internal virtual override {
        ERC1404._increaseBalance(account, amount);
    }

    function _transfer(address from, address to, uint256 amount) internal virtual override(ALockable, ERC1404) {
        ERC1404._transfer(from, to, amount);
    }

    function _unlockedAvailable(address account) internal virtual override view returns (uint256) {
        return _balanceOf(account) - _locked(account);
    }

    function terminate() external override onlyController {
        require(_totalSupply != 0, "Supply has already been destroyed");
        address c = _controller();
        require(_totalSupply == _balanceOf(c), "Asset controller must have reclaimed all tokens prior to terminating this contract");
        _decreaseBalance(c, _totalSupply);
        _totalSupply = 0;
    }

    modifier onlyController() override (ALockable, ERC1404) {
        require(msg.sender == _controller(), "Only contract owner is allowed to perform this action");
        _;
    }

    modifier validAddress(address account) override(ALockable, ERC1404) {
        require(account != address(0), "Use of the zero address is not allowed");
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
