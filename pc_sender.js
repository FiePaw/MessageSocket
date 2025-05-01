import socketio
import threading
import time
from datetime import datetime

# Configuration
SERVER_URL = "http://64.235.61.125:46058"
CLIENT_ID = "pc_client_" + str(int(time.time()))[-4:]
TARGET_CLIENT = ""  # Will be set during runtime

# Initialize Socket.IO client
sio = socketio.Client()
connected = False

def send_message():
    while True:
        if not TARGET_CLIENT:
            print("\n⚠️ First select a target user (type 'list' to see online users)")
        
        message = input("\nEnter message (or 'exit' to quit): ")
        if message.lower() == 'exit':
            break
            
        if message.lower() == 'list':
            sio.emit('request_user_list')
            continue
            
        if not TARGET_CLIENT:
            continue
            
        try:
            sio.emit('send_to_pc', {
                'from': CLIENT_ID,
                'to': TARGET_CLIENT,
                'message': message,
                'timestamp': datetime.now().isoformat()
            })
            print(f"✉️ [You → {TARGET_CLIENT}]: {message}")
        except Exception as e:
            print(f"❌ Error sending message: {str(e)}")

@sio.event
def connect():
    global connected
    connected = True
    print(f"✅ Connected to server as {CLIENT_ID}")
    sio.emit('register', CLIENT_ID)

@sio.event
def user_list(users):
    print("\n🟢 Online Users (Raw):", users)  # Debug raw data
    
    filtered_users = [user for user in users 
                     if user != CLIENT_ID and not user.startswith('android_')]  # Hapus filter android
    
    print("\n🟢 Available Users:")
    for i, user in enumerate(filtered_users):
        print(f"{i+1}. {user}")
    
    if filtered_users:
        select_user(filtered_users)
    else:
        print("⚠️ No other users available")

def select_user(users):
    global TARGET_CLIENT
    while True:
        try:
            choice = input("\nSelect user number (or 'refresh'): ")
            if choice.lower() == 'refresh':
                sio.emit('request_user_list')
                return
                
            choice_idx = int(choice) - 1
            if 0 <= choice_idx < len(users) and users[choice_idx] != CLIENT_ID:
                TARGET_CLIENT = users[choice_idx]
                print(f"\n🎯 Selected target: {TARGET_CLIENT}")
                break
            else:
                print("⚠️ Invalid selection")
        except ValueError:
            print("⚠️ Please enter a number")

@sio.event
def incoming_message(data):
    print(f"\n📩 [{data['from']} → You]: {data['message']}")
    print(f"   ⌚ {data['timestamp']}", end='')
    if TARGET_CLIENT != data['from']:
        print(" (⚠️ Not current target)", end='')
    print("\n> ", end='', flush=True)

@sio.event
def disconnect():
    global connected
    connected = False
    print("\n❌ Disconnected from server")

if __name__ == '__main__':
    try:
        # Connect to server
        sio.connect(SERVER_URL)
        
        # Start message input thread
        input_thread = threading.Thread(target=send_message, daemon=True)
        input_thread.start()
        
        # Keep main thread alive
        while connected:
            time.sleep(1)
            
    except KeyboardInterrupt:
        print("\n👋 Closing client...")
    except Exception as e:
        print(f"🔥 Connection error: {str(e)}")
    finally:
        sio.disconnect()