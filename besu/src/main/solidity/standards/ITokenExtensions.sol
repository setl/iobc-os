// SPDX-License-Identifier: UNLICENSED

// SETL IOBC Contracts for Ethereum v0.1

pragma solidity ^0.8.0;

import "../standards/IDVP.sol";

/**
 * @dev SETL Contract extensions
 * @author Simon Greatrix
 */
interface ITokenExtensions {

    struct Balance {
        address _account;
        uint256 _amount;
    }

    /**
     * @dev Get the controller of this token
     *
     * @return the controller's address
     */
    function controller() external view returns (address);

    /**
     * @dev Cancel a DVP transfer using the controller's authority
     *
     * @param dvp the DVP instance
     * @param dvpId the unique ID of the DVP trade
     */
    function controllerDVPCancel(IDVP dvp, uint256 dvpId) external;

    /**
     * @dev Create a DVP transfer using the controller's authority
     *
     * @param dvp the DVP instance
     * @param dvpId the unique ID of the DVP trade
     */
    function controllerDVPCommit(IDVP dvp, uint256 dvpId) external;

    /**
     * @dev Create a DVP transfer using the controller's authority
     *
     * @param dvp the DVP instance
     * @param dvpId the unique ID of the DVP trade
     * @param party1 the first party to the trade
     * @param party2 the second party to the trade
     * @param autoCommit if true, commit immediately
     */
    function controllerDVPCreate(IDVP dvp, uint256 dvpId, IDVP.Party calldata party1, IDVP.Party calldata party2, bool autoCommit) external;

    /**
     * @dev Moves `amount` tokens from the `from` account to the `to` account using the controller's authority. Can only be invoked by the contract owner.
     *
     * @param from   the source of the tokens
     * @param to     the receiver of the tokens
     * @param amount the amount moved.
     *
     * Emits a {ControllerTransfer} event.
     */
    function controllerTransfer(
        address from,
        address to,
        uint256 amount
    ) external;


    /**
     * @dev Create a DVP transfer.
     *
     * @param dvp the DVP instance
     * @param dvpId the unique ID of the DVP trade
     * @param party1 the first party to the trade
     * @param party2 the second party to the trade
     * @param autoCommit if true, commit immediately
     */
    function dvpCreate(IDVP dvp, uint256 dvpId, IDVP.Party calldata party1, IDVP.Party calldata party2, bool autoCommit) external;

    /**
     * @dev Commit to a DVP transfer.
     *
     * @param dvp the DVP instance
     * @param dvpId the unique ID of the DVP trade
     */
    function dvpCommit(IDVP dvp, uint256 dvpId) external;


    /**
     * @dev Gets some of the holdings for this asset.
     *
     * @param start the index of the first holding to return
     * @param end the index of the first holding above start that is not returned
     *
     * @return ( The holdings, the actual start, the actual end, the total number of holdings )
     */
    function holdings(
        uint256 start,
        uint256 end
    ) external view returns (Balance[] memory, uint256, uint256, uint256);

    /**
     * @dev Permanently delete this token contract from state
     */
    function deleteToken() external;

    /**
     * @dev Terminate this contract, preventing all future trades. Not all contracts will support this.
     */
    function terminate() external;

    /**
     * @dev Emitted when `value` tokens are moved from one account (`from`) to
     * another (`to`) using the controller's authority.
     *
     * Note that `value` may be zero.
     */
    event ControllerTransfer(address indexed from, address indexed to, uint256 amount);

}
