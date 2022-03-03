// SPDX-License-Identifier: UNLICENSED

// SETL IOBC Contracts for Ethereum v0.1

pragma solidity ^0.8.0;

import "../standards/IERC20.sol";

/**
 * @dev Abstract contract providing the core of ERC-20. Generally delegates to an internal method.
 */
abstract contract AERC20 is IERC20 {

    function allowance(address from, address spender)
    external view override
    returns (uint256)
    {
        // Delegate to private method
        return _allowance(from, spender);
    }



    /**
     * @dev Internal function that implements the retrieval of an allowance.
     *
     * @param from    the owner of the funds
     * @param spender the address that can transfer the owner's funds
     *
     * @return the allowance
     */
    function _allowance(address from, address spender)
    internal virtual view
    returns (uint256);

    function approve(address spender, uint256 amount)
    external override
    {
        _approve(msg.sender, spender, _allowance(msg.sender, spender), amount);
    }


    function approveIfMatches(address spender, uint256 expected, uint256 amount)
    external override
    {
        _approve(msg.sender, spender, expected, amount);
    }


    /**
     * @dev Internal implementation of {approve} which sets the amount approved for spending by another address.
     *
     * @param owner    the asset owner
     * @param spender  the address that can spend the owner's assets
     * @param expected the expected current value of the allowance
     * @param amount   the new value for the allowance.
     */
    function _approve(address owner, address spender, uint256 expected, uint256 amount)
    internal virtual;

    function approveDecrease(address spender, uint256 amount)
    external override
    {
        _approveDecrease(msg.sender, spender, amount);
    }

    /**
     * @dev Internal implementation of {approveDecrease} which decrements the amount approved for spending by another address.
     *
     * @param owner   the asset owner
     * @param spender that address that can spend the assets
     * @param delta   the amount subtracted to the available allowance
     */
    function _approveDecrease(address owner, address spender, uint256 delta)
    internal virtual
    validAddress(owner)
    validAddress(spender)
    allowanceAvailable(owner, spender, delta)
    {
        uint256 currentAllowance = _allowance(msg.sender, spender);
        uint256 newAllowance = currentAllowance - delta;
        _approve(owner, spender, currentAllowance, newAllowance);
    }

    function approveIncrease(address spender, uint256 amount)
    external override
    {
        _approveIncrease(msg.sender, spender, amount);
    }

    /**
     * @dev Internal implementation of {approveIncrease} which increments the amount approved for spending by another address.
     *
     * @param owner   the asset owner
     * @param spender that address that can spend the assets
     * @param delta   the amount added to the available allowance
     */
    function _approveIncrease(address owner, address spender, uint256 delta)
    internal virtual
    validAddress(owner)
    validAddress(spender)
    {
        uint256 currentAllowance = _allowance(msg.sender, spender);
        uint256 newAllowance = currentAllowance + delta;
        _approve(owner, spender, currentAllowance, newAllowance);
    }

    function balanceOf(address account)
    external view override
    returns (uint256)
    {
        return _balanceOf(account);
    }

    /**
     * @dev Internal implementation of {balanceOf}.
     *
     * @param account the address to retrieve the balance of
     *
     * @return the balance
     */
    function _balanceOf(address account)
    internal view virtual
    returns (uint256);

    /**
     * @dev Decrease the balance of an account.
     *
     * @param account the account to increase the balance of
     * @param amount the amount to subtract from the account
     */
    function _decreaseBalance(address account, uint256 amount) internal virtual;


    /**
     * @dev Increase the balance of an account.
     *
     * @param account the account to increase the balance of
     * @param amount the amount to add to the account
     */
    function _increaseBalance(address account, uint256 amount) internal virtual;

    /**
     * @dev Internal implementation of transfer.
     *
     * @param from   the sender of the asset
     * @param to     the receiver of the asset
     * @param amount the amount to transfer
     */
    function _transfer(address from, address to, uint256 amount)
    internal virtual;

    /**
     * @dev Internal implementation of {transferFrom}.
     *
     * @param spender the address doing the spend
     * @param from    the owner of the assets
     * @param to      the receiver of the assets
     * @param amount  the amount to transfer
     */
    function _transferFrom(address spender, address from, address to, uint256 amount)
    internal virtual;

    function transfer(address to, uint256 amount)
    external virtual override
    validAddress(to)
    {
        _transfer(msg.sender, to, amount);
    }


    function transferFrom(address from, address to, uint256 amount)
    external override
    {
        _transferFrom(msg.sender, from, to, amount);
    }


    modifier allowanceAvailable(address from, address spender, uint256 amount) {
        require(_allowance(from, spender) >= amount, "Insufficient allowance available");
        _;
    }


    modifier validAddress(address account) virtual;

}
