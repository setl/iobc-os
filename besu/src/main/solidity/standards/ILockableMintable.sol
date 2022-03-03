// SPDX-License-Identifier: UNLICENSED

// SETL IOBC Contracts for Ethereum v0.1

pragma solidity ^0.8.0;

import "./ILockable.sol";
import "./IMintable.sol";

/**
 * @dev Additional functionality that should be supported if a contract is both lockable and mintable.
 *
 * @author Simon Greatrix
 */
interface ILockableMintable is ILockable, IMintable {

    /**
     * @dev Mint a quantity of the asset and credit them to the specified address. Note: only the asset owner can invoke this.
     *
     * @param to       the address to credit with the new assets
     * @param amount   the amount of the asset to create
     * @param toLocked if true, the amount minted will be immediately locked
     */
    function mint(address to, uint256 amount, bool toLocked) external;


    /**
     * @dev Remove a quantity of the asset from the specified address and burn them. Note: only the asset owner can invoke this.
     *
     * @param from       the address to remove the assets from.
     * @param amount     the amount of the asset to burn
     * @param fromLocked if true, the amount burnt will be taken from the locked portion of the address's balance
     */
    function burn(address from, uint256 amount, bool fromLocked) external;
}
