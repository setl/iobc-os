// SPDX-License-Identifier: UNLICENSED

// SETL IOBC Contracts for Ethereum v0.1

pragma solidity ^0.8.0;

/**
 * @dev Interface for an ERC20 type asset that supports being minted and burnt by the asset owner.
 * @author Simon Greatrix
 */
interface IMintable {
    /**
     * @dev Event generated when an asset is minted.
     */
    event Mint(address indexed to, uint256 amount);

    /**
     * @dev Event generated when an asset is burnt.
     */
    event Burn(address indexed from, uint256 amount);


    /**
     * @dev Mint a quantity of the asset and credit them to the specified address.
     * @param to     the address to credit with the new assets
     * @param amount the amount of the asset to create
     */
    function mint(address to, uint256 amount) external;


    /**
     * @dev Remove a quantity of the asset from the specified address and burn them.
     * @param from   the address to remove the assets from.
     * @param amount the amount of the asset to burn
     */
    function burn(address from, uint256 amount) external;
}
