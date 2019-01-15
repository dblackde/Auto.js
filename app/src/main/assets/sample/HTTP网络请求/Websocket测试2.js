var url = "ws://echo.websocket.org";
webSocket.start(url).on("change", (cmd, data)=>{
    toast("收到："+cmd+" "+data);
});
var text="";
setInterval(function(){
   text="send msg"+new Date();
   webSocket.sendMessage(text);
}, 5000);