var io = require('socket.io-client');
var socket;
socket = io.connect("http://127.0.0.1:3000");


// socket.on('cpu_stats', function (data) {
//     console.log("Data received from server:" + data);
// });

socket.connect();
// setTimeout(function () {}, 1000);
// socket.emit('start_container', '1');
// setTimeout(function () {}, 3000);
