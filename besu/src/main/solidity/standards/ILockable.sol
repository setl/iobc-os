// SPDX-License-Identifier: UNLICENSED

// SETL IOBC Contracts for Ethereum v0.1

pragma solidity ^0.8.0;

import "../implementation/DVP.sol";

/**
 * @dev A contract that supports locking of an amount of assets to prevent them being transferred or burnt.
 *
 * @author Simon Greatrix
 */
interface ILockable {
    /**
     * @dev Event generated when as asset is locked.
     */
    event Lock(address indexed account, uint256 amount);

    /**
     * @dev Event generated when an asset is unlocked.
     */
    event Unlock(address indexed account, uint256 amount);

    /**
     * @dev Moves `amount` tokens from the caller's account to `recipient` using the controller's authority. Can only be invoked by the contract owner.
     *
     * @param from        the source of the tokens
     * @param fromLocked if true, the amount is taken from the portion of the sender's assets that are currently locked.
     * @param to         the receiver of the tokens
     * @param toLocked   if true, the amount is added to the portion of the recipient's assets that are locked.
     * @param amount     the amount moved.
     *
     * Emits a {ControllerTransfer} event.
     */
    function controllerTransfer(
        address from,
        bool fromLocked,
        address to,
        bool toLocked,
        uint256 amount
    ) external;

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
    function controllerDVPCreate(DVP dvp, uint256 dvpId, DVP.Party calldata party1, DVP.Party calldata party2, bool autoCommit, bool fromLocked) external;

    /**
     * @dev Commit to a DVP transfer using the controller's authority
     *
     * @param dvp the DVP instance
     * @param dvpId the unique ID of the DVP trade
     * @param fromLocked if true, retrieve this token from locked
     */
    function controllerDVPCommit(DVP dvp, uint256 dvpId, bool fromLocked) external;

    /**
     * @dev Get the amount of an address's balance that is currently locked.
     *
     * @return the current amount that is locked.
     */
    function locked(address account) external view returns (uint256);

    /**
     * @dev Lock an amount of an address's balance. The address must have at least this amount available to be locked. This function can only be invoked by the
     * asset owner.
     *
     * @param account the account whose assets should be locked
     * @param amount  the amount of the asset to lock
     */
    function lock(address account, uint256 amount) external;

    /**
     * @dev Unlock an amount of an address's balance. The address must have at least this amount already locked. This function can only be invoked by the
     * asset owner.
     *
     * @param account the account whose assets should be unlocked
     * @param amount  the amount of the asset to unlock
     */
    function unlock(address account, uint256 amount) external;

}
