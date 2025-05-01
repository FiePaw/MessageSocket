const express = require('express');
const http = require('http');
const socketIo = require('socket.io');

const app = express();
const server = http.createServer(app);
const io = socketIo(server, {
  cors: {
    origin: "*",
    methods: ["GET", "POST"]
  }
});

// Database client: { [clientId]: socketId }
const clients = new Map();

io.on('connection', (socket) => {
  console.log(`💡 New connection: ${socket.id}`);

    socket.on('get_users', () => {
        socket.emit('user_list', Object.keys(activeUsers));
    });
  // Handler registrasi
  socket.on('register', (clientId) => {
    clients.set(clientId, socket.id);
    console.log(`📌 ${clientId} registered`);
    socket.emit('registration_success', { 
      status: 'success',
      clientId,
      onlineUsers: Array.from(clients.keys()) 
    });
  });
  

	const activeUsers = {};

	socket.on('register', (username) => {
		activeUsers[username] = socket.id;
		io.emit('user_list', Object.keys(activeUsers)); // Kirim update ke semua client
	});

	socket.on('disconnect', () => {
		delete activeUsers[socket.username];
		io.emit('user_list', Object.keys(activeUsers));
	});
  // Handler pengiriman pesan
  socket.on('send_to_pc', (data) => {
    const { from, to, message } = data;
    console.log(`✉️ ${from} → ${to}: ${message}`);

    const targetSocketId = clients.get(to);
    if (targetSocketId) {
      io.to(targetSocketId).emit('incoming_message', {
        from,
        message,
        timestamp: new Date().toISOString()
      });
    } else {
      socket.emit('error', `Target ${to} offline`);
    }
  });

  // Handler disconnect
  socket.on('disconnect', () => {
    clients.forEach((socketId, clientId) => {
      if (socketId === socket.id) {
        console.log(`🚫 ${clientId} disconnected`);
        clients.delete(clientId);
        io.emit('user_offline', clientId);
      }
    });
  });
});

server.listen(46058, '0.0.0.0', () => {
  console.log('🔥 Server running on port 46058');
});