// SPDX-License-Identifier: UNLICENSED

// SETL IOBC Contracts for Ethereum v0.1

pragma solidity ^0.8.0;

import "./ATokenExtensions.sol";
import "../standards/IMintable.sol";

abstract contract AMintable is IMintable, ATokenExtensions {

    function burn(address from, uint256 amount) external {
        _burn(from, amount);
    }

    /**
     * @dev Internal implementation of {burn}.
     *
     * @param from the address from which to burn the assets
     * @param value the value to burn
     */
    function _burn(address from, uint256 value)
    internal virtual
    validAddress(from)
    burnableAvailable(from, value)
    {
        _decreaseBalance(from, value);
        _decreaseTotalSupply(value);
        emit Burn(from, value);
    }

    /**
     * @dev Decrease the balance of an account.
     *
     * @param account the account to increase the balance of
     * @param amount the amount to subtract from the account
     */
    function _decreaseBalance(address account, uint256 amount) internal virtual;

    /**
     * @dev Decrease the total asset supply.
     *
     * @param amount the amount to decrease the supply by
     */
    function _decreaseTotalSupply(uint256 amount) internal virtual;

    /**
     * @dev Increase the balance of an account.
     *
     * @param account the account to increase the balance of
     * @param amount the amount to add to the account
     */
    function _increaseBalance(address account, uint256 amount) internal virtual;

    /**
     * @dev Increase the total asset supply.
     *
     * @param amount the amount to increase the supply by
     */
    function _increaseTotalSupply(uint256 amount) internal virtual;


    function mint(address to, uint256 amount) external {
        _mint(to, amount);
    }

    /**
     * @dev Internal implementation of {mint}.
     *
     * @param to    the address to receive the newly minted assets.
     * @param value the amount to mint
     */
    function _mint(address to, uint256 value)
    internal virtual
    validAddress(to)
    {
        _increaseTotalSupply(value);
        _increaseBalance(to, value);
        emit Mint(to, value);
    }


    modifier validAddress(address account) virtual;

    modifier burnableAvailable(address from, uint256) virtual;
}
