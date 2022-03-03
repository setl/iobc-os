// SPDX-License-Identifier: UNLICENSED

// SETL IOBC Contracts for Ethereum v0.1

pragma solidity ^0.8.0;

import "../standards/ITokenExtensions.sol";
import "../standards/IERC20Metadata.sol";
import "../standards/IDVP.sol";

contract DVP is IDVP {

    mapping(uint256 => Trade) private _trades;

    constructor(){
        // Nothing to construct
    }

    function cancel(uint256 externalId) external {
        Trade storage trade = _trades[externalId];
        if (trade.party1.id == address(0)) {
            // OK to cancel a non-existing contract
            return;
        }

        require(
            msg.sender == trade.party1.id ||
            msg.sender == trade.party2.id ||
            ITokenExtensions(trade.party1.token).controller() == msg.sender ||
            ITokenExtensions(trade.party2.token).controller() == msg.sender,
            "Trade can only be cancelled by one of the two parties or the token controller");

        _cancel(externalId, trade);
    }

    function controllerCancel(uint256 externalId) external {
        Trade storage trade = _trades[externalId];
        if (trade.party1.id == address(0)) {
            // OK to cancel a non-existing contract
            return;
        }

        require(msg.sender == address(trade.party1.token) || msg.sender == address(trade.party2.token),
            "Trade can only be controller-cancelled by one of the two tokens");
        _cancel(externalId, trade);
    }

    function _cancel(uint256 externalId, Trade storage trade) internal {
        if (trade.isCommitted1) {
            // return the tokens held in escrow
            IERC20(trade.party1.token).transfer(trade.party1.id, trade.party1.amount);
        }
        if (trade.isCommitted2) {
            // return the tokens held in escrow
            IERC20(trade.party2.token).transfer(trade.party2.id, trade.party2.amount);
        }

        // delete the trade from storage
        delete _trades[externalId];
    }

    /**
     * @dev Create a new trade
     *
     * @param externalId the unique external identifier for the trade
     * @param party1     the first party to the trade
     * @param party2     the second party to the trade
     * @param autoCommit if true, the sender immediately commits to the trade
     */
    function create(uint256 externalId, Party calldata party1, Party calldata party2, bool autoCommit) external {
        require(msg.sender == party1.id || msg.sender == party2.id, "Trade can only be created by one of the two parties");
        _create(externalId, party1, party2);
        if (autoCommit) {
            _commit(msg.sender, externalId, false);
        }
    }

    /**
     * @dev Create a new trade as a token controller.
     *
     * @param externalId the unique external identifier for the trade
     * @param party1     the first party to the trade
     * @param party2     the second party to the trade
     */
    function controllerCreate(uint256 externalId, Party calldata party1, Party calldata party2) external {
        require(msg.sender == address(party1.token) || msg.sender == address(party2.token),
            "Controller can only create trade if they are one of the two tokens");
        _create(externalId, party1, party2);
    }

    /**
     * @dev Create a new trade. Note is it OK to create the same trade multiple times.
     *
     * @param externalId the unique external identifier for the trade
     * @param party1     the first party to the trade
     * @param party2     the second party to the trade
     */
    function _create(uint256 externalId, Party calldata party1, Party calldata party2) internal {
        require(externalId != 0, "External ID for trade must not be zero");
        require(party1.id != address(0), "Party 1's address must not be zero");
        require(party1.token != address(0), "Party 1's token must not be zero");
        require(party2.id != address(0), "Party 2's address must not be zero");
        require(party2.token != address(0), "Party 2's token must not be zero");
        require(party1.token != party2.token, "Token types must differ for trade");
        require(party1.id != party2.id, "Parties must differ for trade");

        Trade storage existing = _trades[externalId];
        if (existing.party1.id == address(0)) {
            // no existing trade
            _trades[externalId] = Trade({party1 : party1, party2 : party2, isCommitted1 : false, isCommitted2 : false});
        } else {
            // Only OK if it is an exact match
            require(
                (
                existing.party1.id == party1.id && existing.party1.token == party1.token && existing.party1.amount == party1.amount
                && existing.party2.id == party2.id && existing.party2.token == party2.token && existing.party2.amount == party2.amount
                ) || (
            existing.party1.id == party2.id && existing.party1.token == party2.token && existing.party1.amount == party2.amount
            && existing.party2.id == party1.id && existing.party2.token == party1.token && existing.party2.amount == party1.amount
            ),
                "DVP Trade already exists and does not match proposal"
            );
        }
    }

    /**
     * @dev Commit to the trade
     *
     * @param externalId the trade's ID
     */
    function commit(uint256 externalId) external {
        _commit(msg.sender, externalId, false);
    }

    /**
     * @dev Commit to the trade as a token controller
     *
     * @param externalId the trade's ID
     */
    function controllerCommit(uint256 externalId) external {
        _commit(msg.sender, externalId, true);
    }

    /**
     * @dev Commit to the trade
     *
     * @param sender      the party doing the commit
     * @param externalId  the trade's ID
     * param isController if true, the committer is one of the token controllers
     */
    function _commit(address sender, uint256 externalId, bool isController) internal {
        require(externalId != 0, "External ID for trade must not be zero");
        require(sender != address(0), "Committer must not be zero");
        Trade storage trade = _trades[externalId];
        require(trade.party1.id != address(0), "Trade does not exist");

        Party storage party_;
        address id1;
        address id2;
        if (isController) {
            // Match the token contracts
            id1 = trade.party1.token;
            id2 = trade.party2.token;
        } else {
            // Match the addresses
            id1 = trade.party1.id;
            id2 = trade.party2.id;
        }

        // sender must own one of the tokens
        if (sender == id1) {
            party_ = trade.party1;
            require(!trade.isCommitted1, "Party has already committed to the trade");
            trade.isCommitted1 = true;
        } else {
            require(sender == id2, "Committer is not a party to the trade");
            require(!trade.isCommitted2, "Party has already committed to the trade");
            party_ = trade.party2;
            trade.isCommitted2 = true;
        }

        // Move the tokens into escrow
        IERC20(party_.token).transferFrom(party_.id, address(this), party_.amount);

        // Check for DVP complete
        if (trade.isCommitted1 && trade.isCommitted2) {
            // Contract is ready to complete
            IERC20(trade.party1.token).transfer(trade.party2.id, trade.party1.amount);
            IERC20(trade.party2.token).transfer(trade.party1.id, trade.party2.amount);
            delete _trades[externalId];
        }
    }

    /**
     * @dev Check if the sender is a party to this trade.
     */
    function isPartyTo(uint256 externalId) external view returns (bool) {
        Trade storage trade = _trades[externalId];
        return (trade.party1.token == msg.sender || trade.party1.id == msg.sender)
        || (trade.party2.token == msg.sender || trade.party2.id == msg.sender);
    }

    /**
     * Check if the sender has committed to this trade
     */
    function isCommitted(uint256 externalId) external view returns (bool) {
        Trade storage trade = _trades[externalId];
        return ((trade.party1.token == msg.sender || trade.party1.id == msg.sender) && trade.isCommitted1)
        || ((trade.party2.token == msg.sender || trade.party2.id == msg.sender) && trade.isCommitted2);
    }

    /**
     * Check if the sender is a party to this trade, and returns the party details.
     */
    function party(uint256 externalId) external view returns (Party memory) {
        Trade storage trade = _trades[externalId];
        if (trade.party1.token == msg.sender || trade.party1.id == msg.sender) {
            return trade.party1;
        }
        if (trade.party2.token == msg.sender || trade.party2.id == msg.sender) {
            return trade.party2;
        }
        return Party(address(0), "", address(0), 0);
    }

    /**
     * Get the description of the specified trade.
     */
    function getTrade(uint256 externalId) external view returns (TradeDetails memory) {
        Trade storage trade_ = _trades[externalId];
        if (!(
        address(trade_.party1.token) == msg.sender ||
        address(trade_.party1.id) == msg.sender ||
        address(trade_.party2.token) == msg.sender ||
        address(trade_.party2.id) == msg.sender
        )) {
            // no such trade, or no access to trade
            return TradeDetails(false, PartyDetails(address(0), "", "", 0, false), PartyDetails(address(0), "", "", 0, false));
        }

        string memory symbol1 = IERC20Metadata(trade_.party1.token).symbol();
        string memory symbol2 = IERC20Metadata(trade_.party2.token).symbol();
        return TradeDetails(true,
            PartyDetails(trade_.party1.id, trade_.party1.externalId, symbol1, trade_.party1.amount, trade_.isCommitted1),
            PartyDetails(trade_.party2.id, trade_.party2.externalId, symbol2, trade_.party2.amount, trade_.isCommitted2));
    }


    /**
     * Get the description of the specified trade.
     * This is a workaround for https://github.com/web3j/web3j/issues/1503 which prevents structs within structs from being correctly decoded by Web3j.
     */
    function getTradeWorkaround(uint256 externalId) external view returns (TradeDetailsWorkaround memory) {
        Trade storage trade_ = _trades[externalId];
        if (trade_.party1.id == address(0)) {
            // Does not exist
            return TradeDetailsWorkaround(false, address(0), "", "", 0, false, address(0), "", "", 0, false);
        }

        // Does caller have access to trade?
        if (!(
        // Could be one of the parties
        address(trade_.party1.id) == msg.sender ||
        address(trade_.party2.id) == msg.sender ||

        // Could be a token contract
        address(trade_.party1.token) == msg.sender ||
        address(trade_.party2.token) == msg.sender ||

        // Coule be a token controller
        ITokenExtensions(trade_.party1.token).controller() == msg.sender ||
        ITokenExtensions(trade_.party2.token).controller() == msg.sender
        )) {
            // No access
            return TradeDetailsWorkaround(false, address(0), "", "", 0, false, address(0), "", "", 0, false);
        }

        string memory symbol1 = IERC20Metadata(trade_.party1.token).symbol();
        string memory symbol2 = IERC20Metadata(trade_.party2.token).symbol();
        return TradeDetailsWorkaround(true,
            trade_.party1.id, trade_.party1.externalId, symbol1, trade_.party1.amount, trade_.isCommitted1,
            trade_.party2.id, trade_.party2.externalId, symbol2, trade_.party2.amount, trade_.isCommitted2);
    }
}
