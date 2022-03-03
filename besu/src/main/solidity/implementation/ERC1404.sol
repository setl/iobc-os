// SPDX-License-Identifier: UNLICENSED

// SETL IOBC Contracts for Ethereum v0.1

pragma solidity ^0.8.0;

import "../standards/IERC20Metadata.sol";
import "../standards/IERC1404.sol";
import "./AERC20.sol";
import "./ATokenExtensions.sol";
import "./DVP.sol";

/**
 * @dev The basic ERC-1404 implementation.
 *
 * @author Simon Greatrix
 */
contract ERC1404 is IERC20Metadata, IERC1404, ATokenExtensions, AERC20 {

    /* Holds the address's index into the holdings array. */
    mapping(address => uint256) private _indexes;

    /** Balances for every address. */
    Balance[] private _holders;

    /** Allowances for approved transfers. Owner and then Spender. */
    mapping(address => mapping(address => uint256)) private _allowances;

    /** The owner of this contract. */
    address private immutable _contractOwner;

    /** This token's name. */
    string private _name;

    /** This token's symbol. */
    string private _symbol;

    /** The total supply of this token. */
    uint256 internal _totalSupply;

    /** The scale factor for this token. */
    uint8 private immutable _decimals;

    constructor(string memory name_, string memory symbol_, uint8 decimals_, uint256 initialSupply_) {
        _name = name_;
        _symbol = symbol_;
        _totalSupply = initialSupply_;
        _decimals = decimals_;
        _contractOwner = msg.sender;

        if (initialSupply_ != 0) {
            _holders.push(Balance(msg.sender, initialSupply_));
            _indexes[msg.sender] = 1;
        }
    }

    /**
     * @dev Internal function that implements the retrieval of an allowance.
     *
     * @param owner   the owner of the funds
     * @param spender the address that can transfer the owner's funds
     *
     * @return the allowance
     */
    function _allowance(address owner, address spender)
    internal view virtual override
    validAddress(owner)
    validAddress(spender)
    returns (uint256)
    {
        return _allowances[owner][spender];
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
    internal virtual override
    validAddress(owner)
    validAddress(spender)
    {
        require(_allowances[owner][spender] == expected, "ERC20 - Existing allowance does not match expected value");
        _allowances[owner][spender] = amount;
        emit Approval(owner, spender, amount);
    }

    /**
     * @dev Internal implementation of {approveDecrease} which decrements the amount approved for spending by another address.
     *
     * @param owner   the asset owner
     * @param spender that address that can spend the assets
     * @param delta   the amount subtracted to the available allowance
     */
    function _approveDecrease(address owner, address spender, uint256 delta)
    internal virtual override
    validAddress(owner)
    validAddress(spender)
    allowanceAvailable(owner, spender, delta)
    {
        uint256 currentAllowance = _allowance(owner, spender);
        uint256 newAllowance = currentAllowance - delta;
        _approve(owner, spender, currentAllowance, newAllowance);
    }


    /**
     * @dev Internal implementation of {approveIncrease} which increments the amount approved for spending by another address.
     *
     * @param owner   the asset owner
     * @param spender that address that can spend the assets
     * @param delta   the amount added to the available allowance
     */
    function _approveIncrease(address owner, address spender, uint256 delta)
    internal virtual override
    validAddress(owner)
    validAddress(spender)
    {
        uint256 currentAllowance = _allowance(owner, spender);
        uint256 newAllowance = currentAllowance + delta;
        _approve(owner, spender, currentAllowance, newAllowance);
    }


    /**
     * @dev Internal implementation of {balanceOf}.
     *
     * @param account the address to retrieve the balance of
     *
     * @return the balance
     */
    function _balanceOf(address account)
    internal view virtual override
    validAddress(account)
    returns (uint256)
    {
        uint256 index = _indexes[account];
        if (index == 0) {
            // not a _holder
            return 0;
        }
        return _holders[index - 1]._amount;
    }

    function _controller() internal virtual override view returns (address) {
        return _contractOwner;
    }


    function _controllerTransfer(address sender, address from, address to, uint256 amount)
    internal virtual override
    validAddress(sender)
    validAddress(from)
    validAddress(to)
    fundsAvailable(from, amount)
    onlyController
    {
        _transfer(from, to, amount);
    }

    function decimals() external view override virtual returns (uint8) {
        return _decimals;
    }

    /**
     * @dev Create a DVP transfer using the controller's authority
     *
     * @param dvp the DVP instance
     * @param dvpId the unique ID of the DVP trade
     * @param party1 the first party to the trade
     * @param party2 the second party to the trade
     * @param autoCommit if true, commit immediately
     */
    function controllerDVPCreate(IDVP dvp, uint256 dvpId, IDVP.Party calldata party1, IDVP.Party calldata party2, bool autoCommit)
    external
    onlyController
    {
        require(party1.token == address(this) || party2.token == address(this), "This token is not a party to the trade");
        IDVP.Party calldata party = (party1.token == address(this)) ? party1 : party2;
        _approveIncrease(party.id, address(dvp), party.amount);
        dvp.controllerCreate(dvpId, party1, party2);
        if (autoCommit) {
            dvp.controllerCommit(dvpId);
        }
    }

    /**
     * @dev Create a DVP transfer using the controller's authority
     *
     * @param dvp the DVP instance
     * @param dvpId the unique ID of the DVP trade
     */
    function controllerDVPCommit(IDVP dvp, uint256 dvpId)
    external
    onlyController
    {
        IDVP.Party memory party = dvp.party(dvpId);
        _approveIncrease(party.id, address(dvp), party.amount);
        dvp.controllerCommit(dvpId);
    }


    /**
     * @dev Cancel a DVP transfer using the controller's authority
     *
     * @param dvp the DVP instance
     * @param dvpId the unique ID of the DVP trade
     */
    function controllerDVPCancel(IDVP dvp, uint256 dvpId)
    external
    onlyController
    {
        dvp.controllerCancel(dvpId);
    }

    /**
     * @dev Create a DVP transfer.
     *
     * @param dvp the DVP instance
     * @param dvpId the unique ID of the DVP trade
     * @param party1 the first party to the trade
     * @param party2 the second party to the trade
     * @param autoCommit if true, commit immediately
     */
    function dvpCreate(IDVP dvp, uint256 dvpId, IDVP.Party calldata party1, IDVP.Party calldata party2, bool autoCommit)
    external
    {
        require(party1.id == address(this) || party2.id == address(this), "The caller is not a party to the trade");
        IDVP.Party calldata party = (party1.token == address(this)) ? party1 : party2;
        _approveIncrease(party.id, address(dvp), party.amount);
        dvp.create(dvpId, party1, party2, autoCommit);
    }


    /**
     * @dev Commit to a DVP transfer.
     *
     * @param dvp the DVP instance
     * @param dvpId the unique ID of the DVP trade
     */
    function dvpCommit(IDVP dvp, uint256 dvpId)
    external
    {
        IDVP.Party memory party = dvp.party(dvpId);
        require(msg.sender == party.id, "Incorrect token for party to the trade");
        _approveIncrease(party.id, address(dvp), party.amount);
        dvp.controllerCommit(dvpId);
    }

    /**
     * @dev Internal implementation of the {holdings} call.
     */
    function _holdings(uint256 start, uint256 end)
    internal view override
    onlyController
    returns (Balance[] memory, uint256, uint256, uint256)
    {
        uint256 length = _holders.length;
        uint256 actualEnd = (end <= length) ? end : length;
        uint256 actualStart = (start <= actualEnd) ? start : actualEnd;

        Balance[] memory array = new Balance[](actualEnd - actualStart);
        for (uint256 index = actualStart; index < actualEnd; index++) {
            array[index - actualStart] = _holders[index];
        }

        return (array, actualStart, actualEnd, length);
    }

    function name() external view override virtual returns (string memory) {
        return _name;
    }

    function symbol() external view override virtual returns (string memory) {
        return _symbol;
    }

    function totalSupply() external view override virtual returns (uint256) {
        return _totalSupply;
    }


    /**
     * @dev Internal implementation of transfer.
     *
     * @param from   the sender of the asset
     * @param to     the receiver of the asset
     * @param amount the amount to transfer
     */
    function _transfer(address from, address to, uint256 amount)
    internal virtual override
    validAddress(from)
    validAddress(to)
    {
        _decreaseBalance(from, amount);
        _increaseBalance(to, amount);
        emit Transfer(from, to, amount);
    }

    /**
     * @dev Internal implementation of {transferFrom}.
     *
     * @param spender the address doing the spend
     * @param from    the owner of the assets
     * @param to      the receiver of the assets
     * @param amount  the amount to transfer
     */
    function _transferFrom(address spender, address from, address to, uint256 amount)
    internal virtual override
    validAddress(spender)
    validAddress(from)
    validAddress(to)
    fundsAvailable(from, amount)
    allowanceAvailable(from, spender, amount)
    {
        _transfer(from, to, amount);
        _approveDecrease(from, spender, amount);
    }


    /**
     * @dev Gets the contract owner, which is the only address that can perform certain actions.
     *
     * @return the contract owner
     */
    function contractOwner() internal virtual view returns (address) {
        return _contractOwner;
    }


    /**
     * @dev Increase the balance of an account.
     *
     * @param account the account to increase the balance of
     * @param amount the amount to add to the account
     */
    function _increaseBalance(address account, uint256 amount) internal virtual override {
        uint256 index = _indexes[account];
        if (index == 0) {
            Balance memory newBalance = Balance(account, amount);
            _holders.push(newBalance);
            _indexes[account] = _holders.length;
        } else {
            _holders[index - 1]._amount += amount;
        }
    }


    /**
     * @dev Decrease the balance of an account.
     *
     * @param account the account to increase the balance of
     * @param amount the amount to subtract from the account
     */
    function _decreaseBalance(address account, uint256 amount) internal virtual override {
        uint256 index = _indexes[account];
        if (index == 0) {
            // Only OK if the amount is zero.
            require(amount == 0, "Attempted to decrease a balance below zero.");
            return;
        }

        uint256 indexM1 = index - 1;
        uint256 newAmount = _holders[indexM1]._amount - amount;
        if (newAmount != 0) {
            // just update the holding
            _holders[indexM1]._amount = newAmount;
        } else {
            // need to remove the holder
            delete _indexes[account];
            if (index == _holders.length) {
                // Simple case, just delete the last entry
                _holders.pop();
            } else {
                // Normal case, where we have to move the last entry into the gap
                Balance storage lastEntry = _holders[_holders.length - 1];
                _holders[indexM1] = lastEntry;
                _indexes[lastEntry._account] = index;
                _holders.pop();
            }
        }
    }

    function _detectTransferRestriction(address from, address to, uint256 value) public virtual view returns (uint8) {
        if (from == address(0)) {
            return 1;
        }
        if (to == address(0)) {
            return 2;
        }
        if (_balanceOf(from) < value) {
            return 3;
        }
        return 0;
    }

    function detectTransferRestriction(address from, address to, uint256 value) external virtual view returns (uint8) {
        return _detectTransferRestriction(from, to, value);
    }

    function messageForTransferRestriction(uint8 restrictionCode) external virtual view returns (string memory) {
        if (restrictionCode == 0) {
            return "No restrictions";
        }
        if (restrictionCode == 1) {
            return "'From' address cannot be the zero address";
        }
        if (restrictionCode == 2) {
            return "'To' address cannot be the zero address";
        }
        if (restrictionCode == 3) {
            return "Insufficient balance";
        }
        if (restrictionCode == 4) {
            return "Insufficient movable balance";
        }
        return "Unknown restriction";
    }

    modifier validAddress(address account) virtual override {
        require(account != address(0), "Zero address is not allowed");
        _;
    }

    modifier onlyController() virtual override {
        require(msg.sender == _controller(), "Only contract owner is allowed to perform this action");
        _;
    }

    modifier fundsAvailable(address account, uint256 amount) virtual {
        require(_balanceOf(account) >= amount, "Insufficient transferrable assets available");
        _;
    }
}