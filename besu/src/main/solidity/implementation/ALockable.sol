// SPDX-License-Identifier: UNLICENSED

// SETL IOBC Contracts for Ethereum v0.1

pragma solidity ^0.8.0;

import "../standards/ILockable.sol";
import "./DVP.sol";

abstract contract ALockable is ILockable {
    /** Locked balances */
    mapping(address => uint256) private _lockedBalance;

    function controllerTransfer(
        address from,
        bool fromLocked,
        address to,
        bool toLocked,
        uint256 amount
    ) external override {
        _controllerTransfer(from, fromLocked, to, toLocked, amount);
    }

    /**
     * Internal implementation of {controllerTransferLocked}
     *
     * @param from       the source address
     * @param fromLocked if true, unlock prior to transfer
     * @param to the     destination address
     * @param toLocked   if true, lock after transfer
     * @param amount     the amount to transfer
     */
    function _controllerTransfer(
        address from,
        bool fromLocked,
        address to,
        bool toLocked,
        uint256 amount
    ) internal
    validAddress(from)
    validAddress(to)
    onlyController
    {
        if (fromLocked) {
            _unlock(from, amount);
        } else {
            require(_unlockedAvailable(from) >= amount, "Insufficient unlocked assets");
        }

        _transfer(from, to, amount);

        if (toLocked) {
            _lock(to, amount);
        }
    }

    /**
     * @dev Create a DVP transfer using the controller's authority
     *
     * @param dvp the DVP instance
     * @param dvpId the unique ID of the DVP trade
     * @param party1 the first party to the trade
     * @param party2 the second party to the trade
     * @param autoCommit if true, commit immediately
     * @param fromLocked if true, retrieve this token from locked
     */
    function controllerDVPCreate(DVP dvp, uint256 dvpId, DVP.Party calldata party1, DVP.Party calldata party2, bool autoCommit, bool fromLocked)
    external
    onlyController
    {
        require(party1.token == address(this) || party2.token == address(this), "This token is not a party to the trade");
        DVP.Party calldata party_ = (party1.token == address(this)) ? party1 : party2;
        if (fromLocked) {
            _unlock(party_.id, party_.amount);
        }
        _approveIncrease(party_.id, address(dvp), party_.amount);
        dvp.controllerCreate(dvpId, party1, party2);
        if (autoCommit) {
            dvp.controllerCommit(dvpId);
        }
    }


    /**
     * @dev Commit to a DVP transfer using the controller's authority
     *
     * @param dvp the DVP instance
     * @param dvpId the unique ID of the DVP trade
     * @param fromLocked if true, retrieve this token from locked
     */
    function controllerDVPCommit(DVP dvp, uint256 dvpId, bool fromLocked)
    external
    onlyController
    {
        DVP.Party memory party = dvp.party(dvpId);
        if (fromLocked) {
            _unlock(party.id, party.amount);
        }
        _approveIncrease(party.id, address(dvp), party.amount);
        dvp.controllerCommit(dvpId);
    }


    /**
     * @dev Internal implementation of {approveIncrease} which increments the amount approved for spending by another address.
     *
     * @param owner   the asset owner
     * @param spender that address that can spend the assets
     * @param delta   the amount added to the available allowance
     */
    function _approveIncrease(address owner, address spender, uint256 delta)
    internal virtual;

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
     * @dev The current funds available for transfer. The available funds do not include the amount that is locked
     *
     * @param account the address to check
     *
     * @return the fund available for transfer.
     */
    function _unlockedAvailable(address account) internal virtual view returns (uint256);

    function locked(address account)
    external override
    view returns (uint256)
    {
        return _locked(account);
    }

    /**
     * @dev Internal implementation of {locked}
     *
     * @param account the account to get the locked amount for
     *
     * @return the amount that is locked
     */
    function _locked(address account)
    internal virtual
    validAddress(account)
    view returns (uint256)
    {
        return _lockedBalance[account];
    }

    function lock(address account, uint256 amount)
    external override {
        _lock(account, amount);
    }

    /**
     * @dev Internal implementation of {lock}
     *
     * @param account the account whose assets to lock
     * @param amount  the amount to lock
     */
    function _lock(address account, uint256 amount)
    internal virtual
    validAddress(account)
    unlockedAvailable(account, amount)
    {
        _lockedBalance[account] += amount;
        emit Lock(account, amount);
    }

    function unlock(address account, uint256 amount)
    external override {
        _unlock(account, amount);
    }

    /**
     * @dev Internal implementation of {unlock}
     *
     * @param account the account whose assets to unlock
     * @param amount  the amount to unlock
     */
    function _unlock(address account, uint256 amount)
    internal virtual
    validAddress(account)
    lockedAvailable(account, amount)
    {
        _lockedBalance[account] -= amount;
        emit Unlock(account, amount);
    }

    modifier validAddress(address account) virtual;

modifier onlyController() virtual;

modifier lockedAvailable(address account, uint256 amount) {
require(_locked(account) >= amount, "Insufficient locked assets");
_;
}

modifier unlockedAvailable(address account, uint256 amount) {
require(_unlockedAvailable(account) >= amount, "Insufficient unlocked assets");
_;
}
}
