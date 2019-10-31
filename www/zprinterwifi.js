var exec = require('cordova/exec');

exports.find = function(successCallback, errorCallback) {
  cordova.exec(successCallback, errorCallback, 'zprinterwifi', 'find', []);
};

exports.print = function(ip, port, str, successCallback, errorCallback) {
  cordova.exec(successCallback, errorCallback, 'zprinterwifi', 'print', [ip, port, str]);
};
