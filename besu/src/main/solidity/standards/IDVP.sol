// SPDX-License-Identifier: UNLICENSED

// SETL IOBC Contracts for Ethereum v0.1

pragma solidity ^0.8.0;

import "../standards/ITokenExtensions.sol";
import "../standards/IERC20Metadata.sol";

interface IDVP {
    /**
     * @dev A one of the two parties to a DVP trade
     */
    struct Party {
        /** @dev This party's address. */
        address id;

        /** @dev The party's external ID. */
        string externalId;

        /** @dev The type of token currently owned by this party which will be transferred to the other party. */
        address token;

        /** @dev The amount of the token to transfer. */
        uint256 amount;

    }

    /**
     * @dev A trade involving two parties
     */
    struct Trade {
        /** @dev the first party. */
        Party party1;

        /** @dev the second party. */
        Party party2;

        /** @dev True if this party1 has committed. The trade executes as soon as both parties commit. */
        bool isCommitted1;

        /** @dev True if this party2 has committed. The trade executes as soon as both parties commit. */
        bool isCommitted2;
    }

    struct PartyDetails {
        address partyId;
        string externalId;
        string symbol;
        uint256 amount;
        bool isCommitted;
    }

    struct TradeDetails {
        bool exists;
        PartyDetails party1;
        PartyDetails party2;
    }


    function cancel(uint256 externalId) external;

    function controllerCancel(uint256 externalId) external;

    /**
     * @dev Create a new trade
     *
     * @param externalId the unique external identifier for the trade
     * @param party1     the first party to the trade
     * @param party2     the second party to the trade
     * @param autoCommit if true, the sender immediately commits to the trade
     */
    function create(uint256 externalId, Party calldata party1, Party calldata party2, bool autoCommit) external;

    /**
     * @dev Create a new trade as a token controller.
     *
     * @param externalId the unique external identifier for the trade
     * @param party1     the first party to the trade
     * @param party2     the second party to the trade
     */
    function controllerCreate(uint256 externalId, Party calldata party1, Party calldata party2) external;


    /**
     * @dev Commit to the trade
     *
     * @param externalId the trade's ID
     */
    function commit(uint256 externalId) external;

    /**
     * @dev Commit to the trade as a token controller
     *
     * @param externalId the trade's ID
     */
    function controllerCommit(uint256 externalId) external;


    /**
     * @dev Check if the sender is a party to this trade.
     */
    function isPartyTo(uint256 externalId) external view returns (bool);

    /**
     * Check if the sender has committed to this trade
     */
    function isCommitted(uint256 externalId) external view returns (bool);

    /**
     * Check if the sender is a party to this trade, and returns the party details.
     */
    function party(uint256 externalId) external view returns (Party memory);

    /**
     * Get the description of the specified trade.
     */
    function getTrade(uint256 externalId) external view returns (TradeDetails memory);

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // This is a workaround for https://github.com/web3j/web3j/issues/1503 which prevents structs within structs from being correctly decoded by Web3j.
    //
    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    struct TradeDetailsWorkaround {
        bool exists;

        address partyId1;
        string externalId1;
        string symbol1;
        uint256 amount1;
        bool isCommitted1;

        address partyId2;
        string externalId2;
        string symbol2;
        uint256 amount2;
        bool isCommitted2;
    }

    /**
     * Get the description of the specified trade.
     * This is a workaround for https://github.com/web3j/web3j/issues/1503 which prevents structs within structs from being correctly decoded by Web3j.
     */
    function getTradeWorkaround(uint256 externalId) external view returns (TradeDetailsWorkaround memory);
}
