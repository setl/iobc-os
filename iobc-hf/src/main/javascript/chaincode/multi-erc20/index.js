/*
 * Copyright IBM Corp. All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
*/

'use strict';

const setlERC20Contract = require('./lib/setlERC20.js');

module.exports.SetlERC20Contract = setlERC20Contract;
module.exports.contracts = [setlERC20Contract];
