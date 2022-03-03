// SPDX-License-Identifier: UNLICENSED

// SETL IOBC Contracts for Ethereum v0.1

pragma solidity ^0.8.0;

import "../standards/ITokenExtensions.sol";

/**
 * @dev Abstract contract implementing the SETL extensions
 */
abstract contract ATokenExtensions is ITokenExtensions {
    function controller() external view returns (address) {
        return _controller();
    }

    function _controller() internal virtual view returns (address);


    function controllerTransfer(address from, address to, uint256 amount)
    external override
    {
        _controllerTransfer(msg.sender, from, to, amount);
    }

    /**
     * @dev Internal implementation of {controllerTransfer}
     */
    function _controllerTransfer(address sender, address from, address to, uint256 amount)
    internal virtual;


    function holdings(
        uint256 start,
        uint256 end
    ) external view returns (Balance[] memory, uint256, uint256, uint256) {
        return _holdings(start, end);
    }

    /**
     * @dev Permanently delete this token from state.
     */
    function deleteToken()
    external virtual override
    onlyController
    {
        selfdestruct(payable(_controller()));
    }

    /**
     * @dev Terminate this contract, preventing all future trades. Not all contracts will support this.
     */
    function terminate() external virtual override onlyController {
        revert("This token type does not support the 'terminate' operation");
    }

    /**
     * @dev Internal implementation of the {holdings} call.
     */
    function _holdings(uint256 start, uint256 end)
    internal virtual view
    returns (Balance[] memory, uint256, uint256, uint256);


    modifier onlyController() virtual;
}
