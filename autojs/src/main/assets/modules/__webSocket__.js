module.exports = function(runtime, global){
    var webSocket = Object.create(runtime.webSocket);
    return webSocket;
};