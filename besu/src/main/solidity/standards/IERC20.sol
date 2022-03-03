// SPDX-License-Identifier: UNLICENSED

// SETL IOBC Contracts for Ethereum v0.1

pragma solidity ^0.8.0;

/**
 * @dev Interface of the ERC20 standard as defined in the EIP.
 * @author Simon Greatrix
 */
interface IERC20 {

    /**
     * @dev Returns the remaining number of tokens that `spender` will be
     * allowed to spend on behalf of `from` through {transferFrom}. This is
     * zero by default.
     *
     * This value changes when {approve} or {transferFrom} are called.
     *
     * @param from    the owner of the asset
     * @param spender the address approved to spend
     */
    function allowance(address from, address spender) external view returns (uint256);

    /**
     * @dev Sets `amount` as the allowance of `spender` over the caller's tokens.
     *
     * IMPORTANT: Beware that changing an allowance with this method brings the risk
     * that someone may use both the old and the new allowance by unfortunate
     * transaction ordering. One possible solution to mitigate this race
     * condition is to first reduce the spender's allowance to 0 and set the
     * desired value afterwards:
     * https://github.com/ethereum/EIPs/issues/20#issuecomment-263524729
     *
     * Emits an {Approval} event.
     *
     * @param spender the address that can spend the caller's assets
     * @param amount  the amount approved to spend.
     */
    function approve(address spender, uint256 amount) external;

    /**
     * @dev Sets `amount` as the allowance of `spender` over the caller's tokens, provided the current allowance matches the `expected` value.
     *
     * Emits an {Approval} event.
     *
     * @param spender  the address that can spend the caller's assets
     * @param expected the expected amount of the current allowance
     * @param amount   the new amount for the allowance
     */
    function approveIfMatches(address spender, uint256 expected, uint256 amount) external;

    /**
     * @dev Decreases the approved allowance for `spender` by the `delta` amount.
     *
     * Emits an {Approval} event.
     *
     * @param spender the address that can spend the caller's assets
     * @param delta   the amount to decrease the allowance by
     */
    function approveDecrease(address spender, uint256 delta) external;

    /**
     * @dev Increases the approved allowance for `spender` by the `delta` amount.
     *
     * Emits an {Approval} event.
     *
     * @param spender the address that can the spend the caller's assets
     * @param delta   the amount to increase the allowance by
     */
    function approveIncrease(address spender, uint256 delta) external;

    /**
     * @dev Returns the amount of tokens owned by `account`.
     *
     * @param account the account to check the balance of
     *
     * @return the balance
     */
    function balanceOf(address account) external view returns (uint256);

    /**
     * @dev Returns the amount of tokens in existence.
     */
    function totalSupply() external view returns (uint256);

    /**
     * @dev Moves `amount` tokens from the caller's account to `recipient`.
     *
     * @param to     the receiver of the tokens
     * @param amount the amount moved.
     *
     * Emits a {Transfer} event.
     */
    function transfer(address to, uint256 amount) external;

    /**
     * @dev Moves `amount` tokens from `from` to `to` using the
     * allowance mechanism. `amount` is then deducted from the caller's
     * allowance.
     *
     * @param from   the owner of the tokens
     * @param to     the receiver of the tokens
     * @param amount the amount moved.
     *
     * Emits a {Transfer} event.
     */
    function transferFrom(
        address from,
        address to,
        uint256 amount
    ) external;

    /**
     * @dev Emitted when `value` tokens are moved from one account (`from`) to
     * another (`to`).
     *
     * Note that `value` may be zero.
     */
    event Transfer(address indexed from, address indexed to, uint256 value);

    /**
     * @dev Emitted when the allowance of a `spender` for an `owner` is set by
     * a call to {approve}. `value` is the new allowance.
     */
    event Approval(address indexed owner, address indexed spender, uint256 value);

}
