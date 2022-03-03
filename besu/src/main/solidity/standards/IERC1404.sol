// SPDX-License-Identifier: UNLICENSED

// SETL IOBC Contracts for Ethereum v0.1

pragma solidity ^0.8.0;

import "./IERC20.sol";

/**
 * @dev This is the interface for the ERC1404 standard
 */
interface IERC1404 is IERC20 {
    /**
     * @dev Detect if there is a reason the a transfer of `value` from `from` to `to` should be forbidden.
     *
     * @param from  the source of the transfer
     * @param to    the destination of the transfer
     * @param value the amount to transfer
     *
     * @return a value from 1 to 255 if there is a restriction, or 0 if there is no restriction
     */
    function detectTransferRestriction(address from, address to, uint256 value) external view returns (uint8);

    /**
     * @dev Get the message for the restriction code.
     *
     * @param restrictionCode the code
     * @return the message
     */
    function messageForTransferRestriction(uint8 restrictionCode) external view returns (string memory);
}