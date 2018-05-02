var express = require('express');
var app = express();
var socketio = require('socket.io');
var server = app.listen(3000, function () {
    console.log('server started on port 3000');
});
var io = socketio(server);
io.on('connection', function (socket) {
    var data = {'1':'b'};
    socket.emit('cpu_stats', data);
});


// io.on('connection', function (socket) {
//     socket.on('start_container', function (data) {
//         console.log('start: ' + data);
//     });
//
//     socket.on('stop_container', function (data) {
//         console.log('stop: ' + data);
//     });
// });
