// SPDX-License-Identifier: UNLICENSED

// SETL BNYM Contracts for Ethereum v0.1
// Produced for the Bank of New York Mellon Proof of Concept, 2021
//
// Disclaimer: this code (and its supporting library) are prepared to a Proof of Concept standard, and may not be suitable for Production use.

pragma solidity ^0.8.0;

import "../standards/ILockableMintable.sol";
import "./ALockable.sol";
import "./AMintable.sol";
import "./ERC1404.sol";

abstract contract ALockableMintable is ILockableMintable, ALockable, AMintable {
    /**
     * @dev Mint a quantity of the asset and credit them to the specified address. Note: only the asset owner can invoke this.
     *
     * @param to       the address to credit with the new assets
     * @param amount   the amount of the asset to create
     * @param toLocked if true, the amount minted will be immediately locked
     */
    function mint(address to, uint256 amount, bool toLocked) external {
        _mint(to, amount);
        if (toLocked) {
            _lock(to, amount);
        }
    }


    /**
     * @dev Remove a quantity of the asset from the specified address and burn them. Note: only the asset owner can invoke this.
     *
     * @param from       the address to remove the assets from.
     * @param amount     the amount of the asset to burn
     * @param fromLocked if true, the amount burnt will be taken from the locked portion of the address's balance
     */
    function burn(address from, uint256 amount, bool fromLocked) external {
        if (fromLocked) {
            _unlock(from, amount);
        }
        _burn(from, amount);
    }

    modifier onlyController() override (ALockable, ATokenExtensions) virtual;
    modifier validAddress(address account) override(ALockable, AMintable) virtual;

}
