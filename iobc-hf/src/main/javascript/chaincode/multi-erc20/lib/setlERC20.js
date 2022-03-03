/*
 * Copyright IBM Corp. All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
*/

'use strict';

const { Contract } = require('fabric-contract-api');

// Define objectType names for prefix
const balancePrefix = 'balance';
const allowancePrefix = 'allowance';
const namePrefix = 'name';
const symbolPrefix = 'symbol';
const decimalsPrefix = 'decimals';
const totalSupplyPrefix = 'totalSupply';
const contractOrgMSPPrefix = 'ownerMspId';
const initializedPrefix = 'contractInitialized'

class SetlERC20Contract extends Contract {

    /**
     * Return the name of the token - e.g. "MyToken".
     * The original function name is `name` in ERC20 specification.
     * However, 'name' conflicts with a parameter `name` in `Contract` class.
     * As a work around, we use `TokenName` as an alternative function name.
     *
     * @param {Context} ctx the transaction context
     * @param {String} symbol the token's symbol
     * @returns {String} Returns the name of the token
    */
    async TokenName(ctx, symbol) {
        const nameKey = ctx.stub.createCompositeKey(namePrefix,[symbol]);
        const nameBytes = await ctx.stub.getState(nameKey);
        return nameBytes.toString();
    }

    /**
     * Return the symbol of the token. E.g. “HIX”.
     *
     * @param {Context} ctx the transaction context
     * @param {String} symbol the token's symbol
     * @returns {String} Returns the symbol of the token
    */
    async Symbol(ctx, symbol) {
        const symbolKey = ctx.stub.createCompositeKey(symbolPrefix,[symbol]);
        const symbolBytes = await ctx.stub.getState(symbolKey);
        return symbolBytes.toString();
    }

    /**
     * Return the number of decimals the token uses
     * e.g. 8, means to divide the token amount by 100000000 to get its user representation.
     *
     * @param {Context} ctx the transaction context
     * @param {String} symbol the token's symbol
     * @returns {Number} Returns the number of decimals
    */
    async Decimals(ctx, symbol) {
        const decimalsKey = ctx.stub.createCompositeKey(decimalsPrefix,[symbol]);
        const decimalsBytes = await ctx.stub.getState(decimalsKey);
        const decimals = parseInt(decimalsBytes.toString());
        return decimals;
    }

    /**
     * Return the total token supply.
     *
     * @param {Context} ctx the transaction context
     * @param {String} symbol the token's symbol
     * @returns {Number} Returns the total token supply
    */
    async TotalSupply(ctx, symbol) {
        const totalSupplyKey = ctx.stub.createCompositeKey(totalSupplyPrefix,[symbol]);
        const totalSupplyBytes = await ctx.stub.getState(totalSupplyKey);
        const totalSupply = parseInt(totalSupplyBytes.toString());
        return totalSupply;
    }

    /**
     * BalanceOf returns the balance of the given account.
     *
     * @param {Context} ctx the transaction context
     * @param {String} symbol the token's symbol
     * @param {String} owner The owner from which the balance will be retrieved
     * @returns {Number} Returns the account balance
     */
    async BalanceOf(ctx, symbol, owner) {
        const balanceKey = ctx.stub.createCompositeKey(balancePrefix, [symbol, owner]);

        const balanceBytes = await ctx.stub.getState(balanceKey);
        if (!balanceBytes || balanceBytes.length === 0) {
            return 0;
        }
        const balance = parseInt(balanceBytes.toString());

        return balance;
    }

    /**
     *  Transfer transfers tokens from client account to recipient account.
     *  recipient account must be a valid clientID as returned by the ClientAccountID() function.
     *
     * @param {Context} ctx the transaction context
     * @param {String} symbol the token's symbol
     * @param {String} to The recipient
     * @param {Integer} value The amount of token to be transferred
     * @returns {Boolean} Return whether the transfer was successful or not
     */
    async Transfer(ctx, symbol, to, value) {
        const from = ctx.clientIdentity.getID();

        const transferResp = await this._transfer(ctx, symbol, from, to, value);
        if (!transferResp) {
            throw new Error('Failed to transfer');
        }

        // Emit the Transfer event
        const transferEvent = { symbol, from, to, value: parseInt(value) };
        ctx.stub.setEvent('Transfer', Buffer.from(JSON.stringify(transferEvent)));

        return true;
    }

    /**
    * Transfer `value` amount of tokens from `from` to `to`.
    *
    * @param {Context} ctx the transaction context
    * @param {String} symbol the token's symbol
    * @param {String} from The sender
    * @param {String} to The recipient
    * @param {Integer} value The amount of token to be transferred
    * @returns {Boolean} Return whether the transfer was successful or not
    */
    async TransferFrom(ctx, symbol, from, to, value) {
        const spender = ctx.clientIdentity.getID();

        // Retrieve the allowance of the spender
        const allowanceKey = ctx.stub.createCompositeKey(allowancePrefix, [symbol, from, spender]);
        const currentAllowanceBytes = await ctx.stub.getState(allowanceKey);

        if (!currentAllowanceBytes || currentAllowanceBytes.length === 0) {
            throw new Error(`spender ${spender} has no allowance from ${from}`);
        }

        const currentAllowance = parseInt(currentAllowanceBytes.toString());

        // Convert value from string to int
        const valueInt = parseInt(value);

        // Check if the transferred value is less than the allowance
        if (currentAllowance < valueInt) {
            throw new Error('The spender does not have enough allowance to spend.');
        }

        const transferResp = await this._transfer(ctx, from, to, value);
        if (!transferResp) {
            throw new Error('Failed to transfer');
        }

        // Decrease the allowance
        const updatedAllowance = currentAllowance - valueInt;
        await ctx.stub.putState(allowanceKey, Buffer.from(updatedAllowance.toString()));
        console.log(`${symbol} : spender ${spender} allowance updated from ${currentAllowance} to ${updatedAllowance}`);

        // Emit the Transfer event
        const transferEvent = { from, to, value: valueInt };
        ctx.stub.setEvent('Transfer', Buffer.from(JSON.stringify(transferEvent)));

        console.log('transferFrom ended successfully');
        return true;
    }

    async _transfer(ctx, symbol, from, to, value) {

        if (from === to) {
            throw new Error('cannot transfer to and from same client account');
        }

        // Convert value from string to int
        const valueInt = parseInt(value);

        if (valueInt <= 0) { // transfer of 0 is allowed in ERC20, so just validate against negative amounts
            throw new Error('transfer amount cannot be negative or zero');
        }

        // Retrieve the current balance of the sender
        const fromBalanceKey = ctx.stub.createCompositeKey(balancePrefix, [symbol, from]);
        const fromCurrentBalanceBytes = await ctx.stub.getState(fromBalanceKey);

        if (!fromCurrentBalanceBytes || fromCurrentBalanceBytes.length === 0) {
            throw new Error(`client account ${from} has no balance`);
        }

        const fromCurrentBalance = parseInt(fromCurrentBalanceBytes.toString());

        // Check if the sender has enough tokens to spend.
        if (fromCurrentBalance < valueInt) {
            throw new Error(`client account ${from} has insufficient funds.`);
        }

        // Retrieve the current balance of the recipient
        const toBalanceKey = ctx.stub.createCompositeKey(balancePrefix, [symbol, to]);
        const toCurrentBalanceBytes = await ctx.stub.getState(toBalanceKey);

        let toCurrentBalance;
        // If recipient current balance doesn't yet exist, we'll create it with a current balance of 0
        if (!toCurrentBalanceBytes || toCurrentBalanceBytes.length === 0) {
            toCurrentBalance = 0;
        } else {
            toCurrentBalance = parseInt(toCurrentBalanceBytes.toString());
        }

        // Update the balance
        const fromUpdatedBalance = fromCurrentBalance - valueInt;
        const toUpdatedBalance = toCurrentBalance + valueInt;

        await ctx.stub.putState(fromBalanceKey, Buffer.from(fromUpdatedBalance.toString()));
        await ctx.stub.putState(toBalanceKey, Buffer.from(toUpdatedBalance.toString()));

        console.log(`${symbol} : client ${from} balance updated from ${fromCurrentBalance} to ${fromUpdatedBalance}`);
        console.log(`${symbol} : recipient ${to} balance updated from ${toCurrentBalance} to ${toUpdatedBalance}`);

        return true;
    }

    /**
     * Allows `spender` to spend `value` amount of tokens from the owner.
     *
     * @param {Context} ctx the transaction context
     * @param {String} symbol the token's symbol
     * @param {String} spender The spender
     * @param {Integer} value The amount of tokens to be approved for transfer
     * @returns {Boolean} Return whether the approval was successful or not
     */
    async Approve(ctx, symbol, spender, value) {
        const owner = ctx.clientIdentity.getID();

        const allowanceKey = ctx.stub.createCompositeKey(allowancePrefix, [symbol, owner, spender]);

        let valueInt = parseInt(value);
        await ctx.stub.putState(allowanceKey, Buffer.from(valueInt.toString()));

        // Emit the Approval event
        const approvalEvent = { symbol, owner, spender, value: valueInt };
        ctx.stub.setEvent('Approval', Buffer.from(JSON.stringify(approvalEvent)));

        console.log('approve ended successfully');
        return true;
    }

    /**
     * Returns the amount of tokens which `spender` is allowed to withdraw from `owner`.
     *
     * @param {Context} ctx the transaction context
     * @param {String} symbol the token's symbol
     * @param {String} owner The owner of tokens
     * @param {String} spender The spender who are able to transfer the tokens
     * @returns {Number} Return the amount of remaining tokens allowed to spent
     */
    async Allowance(ctx, owner, spender) {
        const allowanceKey = ctx.stub.createCompositeKey(allowancePrefix, [symbol, owner, spender]);

        const allowanceBytes = await ctx.stub.getState(allowanceKey);
        if (!allowanceBytes || allowanceBytes.length === 0) {
            throw new Error(`spender ${spender} has no allowance from ${owner}`);
        }

        const allowance = parseInt(allowanceBytes.toString());
        return allowance;
    }

    // ================== Extended Functions ==========================

    /**
     * Initialize the Token.
     *
     * @param {Context} ctx the transaction context
     * @param {String} name The name of the token
     * @param {String} symbol The symbol of the token
     * @param {String} decimals The decimals of the token
     * @param {String} totalSupply The totalSupply of the token
     */
    async InitContract(ctx, name, symbol, decimals) {
        const initializedKey = ctx.stub.createCompositeKey(initializedPrefix,[symbol]);
        const nameKey = ctx.stub.createCompositeKey(namePrefix,[symbol]);
        const symbolKey = ctx.stub.createCompositeKey(symbolPrefix,[symbol]);
        const decimalsKey = ctx.stub.createCompositeKey(decimalsPrefix,[symbol]);
        const contractOrgMSPKey = ctx.stub.createCompositeKey(contractOrgMSPPrefix,[symbol]);

        const initializedBytes = await ctx.stub.getState(initializedKey);
        const initialized = Boolean(initializedBytes.toString());
        if(initialized){
            console.log('Contract is already initialized');
            return true;
        }
        const clientMSPID = ctx.clientIdentity.getMSPID();
        await ctx.stub.putState(nameKey, Buffer.from(name));
        await ctx.stub.putState(symbolKey, Buffer.from(symbol));
        await ctx.stub.putState(decimalsKey, Buffer.from(decimals));
        await ctx.stub.putState(contractOrgMSPKey, Buffer.from(clientMSPID));
        await ctx.stub.putState(initializedKey, Buffer.from('true'));

        console.log(`name: ${name}, symbol: ${symbol}, decimals: ${decimals}, owningOrgMSP: ${clientMSPID}`);
        return true;
    }

    /**
     * Mint creates new tokens and adds them to minter's account balance
     *
     * @param {Context} ctx the transaction context
     * @param {String} symbol the token's symbol
     * @param {Integer} amount amount of tokens to be minted
     * @returns {Object} The balance
     */
    async Mint(ctx, symbol, amount) {

        // Check minter authorization - this sample assumes Org1 is the central banker with privilege to mint new tokens
        const clientMSPID = ctx.clientIdentity.getMSPID();
        const contractOrgMSPKey = ctx.stub.createCompositeKey(contractOrgMSPPrefix,[symbol]);
        const ownerMSPIDBytes = await ctx.stub.getState(contractOrgMSPKey);
        const ownerMSPID = ownerMSPIDBytes.toString();
        if (clientMSPID !== ownerMSPID) {
            throw new Error('client is not authorized to mint new tokens');
        }

        // Get ID of submitting client identity
        const minter = ctx.clientIdentity.getID();

        const amountInt = parseInt(amount);
        if (amountInt <= 0) {
            throw new Error('mint amount must be a positive integer');
        }

        const balanceKey = ctx.stub.createCompositeKey(balancePrefix, [symbol, minter]);

        const currentBalanceBytes = await ctx.stub.getState(balanceKey);
        // If minter current balance doesn't yet exist, we'll create it with a current balance of 0
        let currentBalance;
        if (!currentBalanceBytes || currentBalanceBytes.length === 0) {
            currentBalance = 0;
        } else {
            currentBalance = parseInt(currentBalanceBytes.toString());
        }
        const updatedBalance = currentBalance + amountInt;

        await ctx.stub.putState(balanceKey, Buffer.from(updatedBalance.toString()));

        // Increase totalSupply
        const totalSupplyKey = ctx.stub.createCompositeKey(totalSupplyPrefix,[symbol]);
        const totalSupplyBytes = await ctx.stub.getState(totalSupplyKey);
        let totalSupply;
        if (!totalSupplyBytes || totalSupplyBytes.length === 0) {
            console.log('Initialize the tokenSupply');
            totalSupply = 0;
        } else {
            totalSupply = parseInt(totalSupplyBytes.toString());
        }
        totalSupply = totalSupply + amountInt;
        await ctx.stub.putState(totalSupplyKey, Buffer.from(totalSupply.toString()));

        // Emit the Transfer event
        const transferEvent = { from: '0x0', to: minter, value: amountInt };
        ctx.stub.setEvent('Transfer', Buffer.from(JSON.stringify(transferEvent)));

        console.log(`minter account ${minter} balance updated from ${currentBalance} to ${updatedBalance}`);
        return true;
    }

    /**
     * Burn redeem tokens from minter's account balance
     *
     * @param {Context} ctx the transaction context
     * @param {String} symbol the token's symbol
     * @param {Integer} amount amount of tokens to be burned
     * @returns {Object} The balance
     */
    async Burn(ctx, symbol, amount) {

        // Check minter authorization - this sample assumes Org1 is the central banker with privilege to burn tokens
        const clientMSPID = ctx.clientIdentity.getMSPID();
        const contractOrgMSPKey = ctx.stub.createCompositeKey(contractOrgMSPPrefix,[symbol]);
        const ownerMSPIDBytes = await ctx.stub.getState(contractOrgMSPKey);
        const ownerMSPID = ownerMSPIDBytes.toString();

        if (clientMSPID !== ownerMSPID) {
            throw new Error('client is not authorized to mint new tokens');
        }

        const minter = ctx.clientIdentity.getID();

        const amountInt = parseInt(amount);

        const balanceKey = ctx.stub.createCompositeKey(balancePrefix, [symbol, minter]);

        const currentBalanceBytes = await ctx.stub.getState(balanceKey);
        if (!currentBalanceBytes || currentBalanceBytes.length === 0) {
            throw new Error('The balance does not exist');
        }
        const currentBalance = parseInt(currentBalanceBytes.toString());
        const updatedBalance = currentBalance - amountInt;

        await ctx.stub.putState(balanceKey, Buffer.from(updatedBalance.toString()));

        // Decrease totalSupply
        const totalSupplyKey = ctx.stub.createCompositeKey(totalSupplyPrefix,[symbol]);
        const totalSupplyBytes = await ctx.stub.getState(totalSupplyKey);
        if (!totalSupplyBytes || totalSupplyBytes.length === 0) {
            throw new Error('totalSupply does not exist.');
        }
        const totalSupply = parseInt(totalSupplyBytes.toString()) - amountInt;
        await ctx.stub.putState(totalSupplyKey, Buffer.from(totalSupply.toString()));

        // Emit the Transfer event
        const transferEvent = { from: minter, to: '0x0', value: amountInt };
        ctx.stub.setEvent('Transfer', Buffer.from(JSON.stringify(transferEvent)));

        console.log(`minter account ${minter} balance updated from ${currentBalance} to ${updatedBalance}`);
        return true;
    }

    /**
     * ClientAccountBalance returns the balance of the requesting client's account.
     *
     * @param {Context} ctx the transaction context
     * @param {String} symbol the token's symbol
     * @returns {Number} Returns the account balance
     */
    async ClientAccountBalance(ctx, symbol) {
        // Get ID of submitting client identity
        const clientAccountID = ctx.clientIdentity.getID();

        const balanceKey = ctx.stub.createCompositeKey(balancePrefix, [symbol, clientAccountID]);
        const balanceBytes = await ctx.stub.getState(balanceKey);
        if (!balanceBytes || balanceBytes.length === 0) {
            return 0;
        }
        const balance = parseInt(balanceBytes.toString());

        return balance;
    }

    // ClientAccountID returns the id of the requesting client's account.
    // In this implementation, the client account ID is the clientId itself.
    // Users can use this function to get their own account id, which they can then give to others as the payment address
    async ClientAccountID(ctx) {
        // Get ID of submitting client identity
        const clientAccountID = ctx.clientIdentity.getID();
        return clientAccountID;
    }

}

module.exports = SetlERC20Contract;
